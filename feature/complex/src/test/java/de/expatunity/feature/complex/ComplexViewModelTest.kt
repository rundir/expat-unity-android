package de.expatunity.feature.complex

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import de.expatunity.core.domain.AppException
import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ErrorType
import de.expatunity.core.domain.ListingStatus
import de.expatunity.core.testing.FakeComplexRepository
import de.expatunity.core.testing.FakeLocaleProvider
import de.expatunity.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ComplexViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()
    private val repo = FakeComplexRepository()

    private val localeProvider: FakeLocaleProvider = FakeLocaleProvider().apply { locale = "de" }

    private fun vm() = ComplexViewModel(SavedStateHandle(mapOf("id" to "42")), repo,
        localeProvider)

    @Test
    fun `success shows content`() = runTest {
        repo.detail = sampleDetail()
        vm().state.test {
            assertTrue(awaitItem() is ComplexUiState.Content)
        }
    }

    private fun sampleDetail() = ComplexDetail(
        id = "id",
        name = "name",
        city = "city",
        address = "address",
        status = ListingStatus.UNKNOWN,
        description = "description",
        images = emptyList(),
        apartments = emptyList(),
        fallbackUsed = false
    )

    @Test
    fun `network error without cache shows error`() = runTest {
        repo.error = AppException(ErrorType.Network)
        val vm = vm()
        vm.state.test {
            advanceUntilIdle()
            assertEquals(
                ComplexUiState.Error(ErrorType.Network),
                expectMostRecentItem(),
            )
        }
        assertEquals(1, repo.detailRefreshCalls)
    }
    private fun complexDetail() = ComplexDetail(
        id = "1",
        name = "Wohnpark Am See",
        city = "Leipzig",
        address = null,
        status = ListingStatus.ACTIVE,
        description = "Описание комплекса",
        images = emptyList(),
        apartments = emptyList(),
        fallbackUsed = false,
    )

    @Test
    fun `successful refresh shows downloaded detail`() = runTest {
        val detail = complexDetail()
        repo.detail = detail
        val vm = vm()

        vm.state.test {
            advanceUntilIdle()
            assertEquals(
                ComplexUiState.Content(detail),
                expectMostRecentItem(),
            )
        }
        assertEquals(detail, repo.cachedDetail.value)
        assertEquals(1, repo.detailRefreshCalls)
    }

    @Test
    fun `network error with cache keeps content`() = runTest {
        val detail = complexDetail()
        repo.cachedDetail.value = detail
        repo.error = AppException(ErrorType.Network)
        val vm = vm()

        vm.state.test {
            advanceUntilIdle()
            assertEquals(
                ComplexUiState.Content(detail),
                expectMostRecentItem(),
            )
        }
        assertEquals(detail, repo.cachedDetail.value)
        assertEquals(1, repo.detailRefreshCalls)
    }
}