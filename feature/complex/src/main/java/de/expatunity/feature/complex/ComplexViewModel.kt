package de.expatunity.feature.complex

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ComplexRepository
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

class ComplexViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ComplexRepository,
    private val locale: String,
) : ViewModel() {
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

    companion object {
        fun factory(repository: ComplexRepository, locale: String) = viewModelFactory {
            initializer { ComplexViewModel(createSavedStateHandle(), repository, locale) }
        }
    }
}