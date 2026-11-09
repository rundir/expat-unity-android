package de.expatunity.feature.catalog

import app.cash.turbine.test
import de.expatunity.core.domain.LocaleProvider
import de.expatunity.core.domain.ToggleFavoriteUseCase
import de.expatunity.core.testing.FakeComplexRepository
import de.expatunity.core.testing.FakeFavoritesRepository
import de.expatunity.core.testing.MainDispatcherRule
import de.expatunity.core.testing.sampleSummary
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogSearchTest {

    @get:Rule
    val main = MainDispatcherRule()
    private val complexRepository = FakeComplexRepository()
    private val favRepository = FakeFavoritesRepository()
    private val locale = object : LocaleProvider { override fun apiLocale() = "de" }

    private fun vm() = CatalogViewModel(
        complexRepository,
        locale,
        favRepository,
        ToggleFavoriteUseCase(favRepository)
    )

    @Test
    fun `query filters items by name`() = runTest {
        complexRepository.complexes.value = listOf(
            sampleSummary(id = "1", name = "Isar Park"),
            sampleSummary(id = "2", name = "Berg Residenz"),
        )
        val vm = vm()

        vm.uiState.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem() as CatalogUiState.Content
            assertEquals(2, initial.items.size)

            vm.onQueryChange("isar")
            advanceTimeBy(300.milliseconds)
            runCurrent()

            val filtered = expectMostRecentItem() as CatalogUiState.Content
            assertEquals(listOf("1"), filtered.items.map { it.id })
        }
    }

    @Test
    fun `blank query shows all items`() = runTest {
        complexRepository.complexes.value = listOf(
            sampleSummary(id = "1", name = "Isar Park"),
            sampleSummary(id = "2", name = "Berg Residenz"),
        )
        val vm = vm()

        vm.uiState.test {
            advanceUntilIdle()
            expectMostRecentItem()

            vm.onQueryChange("isar")
            advanceTimeBy(300.milliseconds)
            runCurrent()
            val filtered = expectMostRecentItem() as CatalogUiState.Content
            assertEquals(listOf("1"), filtered.items.map { it.id })

            vm.onQueryChange("")
            runCurrent()

            val all = expectMostRecentItem() as CatalogUiState.Content
            assertEquals(listOf("1", "2"), all.items.map { it.id })
        }
    }

    @Test
    fun `favorites only shows favorite items`() = runTest {
        complexRepository.complexes.value = listOf(
            sampleSummary(id = "1", name = "Isar Park"),
            sampleSummary(id = "2", name = "Berg Residenz"),
        )
        favRepository.favoriteIds.value = setOf("2")
        val vm = vm()

        vm.uiState.test {
            advanceUntilIdle()
            expectMostRecentItem()

            vm.onFavoritesOnlyChange(true)
            runCurrent()

            val state = expectMostRecentItem() as CatalogUiState.Content
            assertEquals(listOf("2"), state.items.map { it.id })
        }
    }

    @Test
    fun `favorites only combines with search`() = runTest {
        complexRepository.complexes.value = listOf(
            sampleSummary(id = "1", name = "Isar Park"),
            sampleSummary(id = "2", name = "Isar Residenz"),
            sampleSummary(id = "3", name = "Berg Haus"),
        )
        favRepository.favoriteIds.value = setOf("2", "3")
        val vm = vm()

        vm.uiState.test {
            advanceUntilIdle()
            expectMostRecentItem()

            vm.onFavoritesOnlyChange(true)
            vm.onQueryChange("isar")
            advanceTimeBy(300.milliseconds)
            runCurrent()

            val state = expectMostRecentItem() as CatalogUiState.Content
            assertEquals(listOf("2"), state.items.map { it.id })
        }
    }

    @Test
    fun `removing last favorite empties filtered list`() = runTest {
        complexRepository.complexes.value = listOf(
            sampleSummary(id = "1", name = "Isar Park"),
        )
        favRepository.favoriteIds.value = setOf("1")
        val vm = vm()

        vm.uiState.test {
            advanceUntilIdle()
            vm.onFavoritesOnlyChange(true)
            runCurrent()
            assertEquals(
                listOf("1"),
                (expectMostRecentItem() as CatalogUiState.Content).items.map { it.id },
            )

            vm.onToggleFavorite("1")
            advanceUntilIdle()

            val state = expectMostRecentItem() as CatalogUiState.Content
            assertEquals(emptyList<String>(), state.items.map { it.id })
            assertEquals(emptySet<String>(), state.favoriteIds)
        }
    }
    
}