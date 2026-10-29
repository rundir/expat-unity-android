package de.expatunity.feature.complex

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ComplexRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class ComplexDestination(val id: String)

sealed interface ComplexUiState {
    data object Loading : ComplexUiState
    data class Content(val complex: ComplexDetail) : ComplexUiState
    data class Error(val message: String) : ComplexUiState
}

@HiltViewModel
class ComplexViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ComplexRepository,
) : ViewModel() {
    private val locale: String = "de"
    private val id = savedStateHandle.toRoute<ComplexDestination>().id
    private val _state = MutableStateFlow<ComplexUiState>(ComplexUiState.Loading)
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = ComplexUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                ComplexUiState.Content(repository.getComplex(id, locale))
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { ComplexUiState.Error(e.message ?: "Error") }
        }
    }
}