package de.expatunity.calculator

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


class CalculatorViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private fun vm() = CalculatorViewModel(SavedStateHandle(mapOf("id" to "42")))

    @Test
    fun `success shows content`() = runTest {
        vm().state.test {
            assertEquals(CalculatorUiState(), awaitItem())
        }
    }

    @Test fun `error shows error state`() = runTest {
        vm().state.test {
            assertEquals(CalculatorUiState(), awaitItem())
        }
    }

}