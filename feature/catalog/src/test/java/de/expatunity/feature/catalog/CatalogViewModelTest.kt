package de.expatunity.feature.catalog

import app.cash.turbine.test
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ListingStatus
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test


class CatalogViewModelTest {
    private val repo: FakeComplexRepository = FakeComplexRepository()

    @get:Rule
    val main = MainDispatcherRule()

    private fun vm() = CatalogViewModel(repo)

    @Test
    fun `success shows content`() = runTest {
        repo.listOfSummary = sampleDetail()
        vm().state.test {
            assertTrue(awaitItem() is CatalogUiState.Content)
        }
    }

    @Test fun `error shows error state`() = runTest {
        repo.error = IOException("test")
        vm().state.test {
            assertTrue(awaitItem() is CatalogUiState.Error)
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
            7
        )
    }

}