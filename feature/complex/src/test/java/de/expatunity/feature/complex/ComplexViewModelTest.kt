package de.expatunity.feature.complex

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ListingStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ComplexViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()
    private val repo = FakeComplexRepository()
    private fun vm() = ComplexViewModel(SavedStateHandle(mapOf("id" to "42")), repo)

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

    @Test fun `error shows error state`() = runTest {
        repo.error = IOException("offline")
        vm().state.test {
            assertEquals(ComplexUiState.Error("offline"), awaitItem())
        }
    }
}