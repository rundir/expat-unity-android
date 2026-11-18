package de.expatunity.feature.catalog

import app.cash.turbine.test
import de.expatunity.core.domain.AppException
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ErrorType
import de.expatunity.core.domain.ListingStatus
import de.expatunity.core.domain.LocaleProvider
import de.expatunity.core.domain.usecase.ObserveCatalogUseCase
import de.expatunity.core.domain.usecase.ObserveComplexesUseCase
import de.expatunity.core.domain.usecase.ObserveFavoriteIdsUseCase
import de.expatunity.core.domain.usecase.RefreshComplexesUseCase
import de.expatunity.core.domain.usecase.ToggleFavoriteUseCase
import de.expatunity.core.testing.FakeComplexRepository
import de.expatunity.core.testing.FakeFavoritesRepository
import de.expatunity.core.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test


class CatalogViewModelTest {
    private val complexRepository: FakeComplexRepository = FakeComplexRepository()

    @get:Rule
    val main = MainDispatcherRule()

    private val favRepository = FakeFavoritesRepository()

    private fun vm(): CatalogViewModel {
        val localeProvider = object : LocaleProvider {
            override fun apiLocale() = "de"
        }
        return CatalogViewModel(
            observeCatalog = ObserveCatalogUseCase(
                ObserveComplexesUseCase(complexRepository),
                ObserveFavoriteIdsUseCase(favRepository),
            ),
            refreshComplexes = RefreshComplexesUseCase(complexRepository),
            toggleFavorite = ToggleFavoriteUseCase(favRepository),
            localeProvider = localeProvider,        )
    }

    @Test
    fun `success shows content`() = runTest {
        complexRepository.complexes.value = sampleDetail()
        vm().uiState.test {
            advanceUntilIdle()
            assertTrue(expectMostRecentItem() is CatalogUiState.Content)
        }
    }

    @Test fun `error shows error state`() = runTest {
        complexRepository.error = IOException("test")
        vm().uiState.test {
            advanceUntilIdle()
            assertTrue(expectMostRecentItem() is CatalogUiState.Error)
        }
    }

    private fun sampleDetail(): List<ComplexSummary>  {
        return listOf(complexSummary())
    }

    private fun complexSummary(): ComplexSummary {
        return ComplexSummary(
            "id",
            "name",
            "city",
            ListingStatus.UNKNOWN,
            100.0,
            "coverImageUrl",
            7,
            null
        )
    }

    @Test
    fun `content plus error emits ShowError and keeps content`() = runTest {
        complexRepository.complexes.value = listOf(complexSummary())
        val vm = vm()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle() // init-refresh прошёл, на экране Content
        complexRepository.error = AppException(ErrorType.Network)
        vm.events.test {
            vm.refresh()
            assertEquals(CatalogViewModel.CatalogEvent.ShowError(ErrorType.Network), awaitItem())
        }
        assertTrue(vm.uiState.value is CatalogUiState.Content)
    }

    @Test
    fun `empty plus error shows Error state`() = runTest {
        complexRepository.error = AppException(ErrorType.Network)
        val vm = vm()

        vm.uiState.test {
            advanceUntilIdle()
            assertEquals(CatalogUiState.Error(ErrorType.Network), expectMostRecentItem())
        }
    }

    @Test
    fun `refresh is called once on init`() = runTest {
        vm()
        advanceUntilIdle()
        assertEquals(1, complexRepository.refreshCalls)
    }

    @Test
    fun `toggle favorite adds then removes id from content`() = runTest {
        complexRepository.complexes.value = sampleDetail()
        val vm = vm()

        vm.uiState.test {
            advanceUntilIdle()
            assertEquals(
                emptySet<String>(),
                (expectMostRecentItem() as CatalogUiState.Content).favoriteIds,
            )

            vm.onToggleFavorite("id")
            advanceUntilIdle()
            assertEquals(
                setOf("id"),
                (expectMostRecentItem() as CatalogUiState.Content).favoriteIds,
            )

            vm.onToggleFavorite("id")
            advanceUntilIdle()
            assertEquals(
                emptySet<String>(),
                (expectMostRecentItem() as CatalogUiState.Content).favoriteIds,
            )
        }
    }

    @Test
    fun `favorite write failure emits event and keeps favorites`() = runTest {
        favRepository.favoriteIds.value = setOf("id")
        favRepository.error = IOException("write failed")
        val vm = vm()
        advanceUntilIdle()

        vm.events.test {
            vm.onToggleFavorite("id")
            assertEquals(
                CatalogViewModel.CatalogEvent.FavoriteUpdateFailed,
                awaitItem(),
            )
        }
        assertEquals(setOf("id"), favRepository.favoriteIds.value)
    }
}