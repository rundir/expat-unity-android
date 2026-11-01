package de.expatunity.feature.complex

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.expatunity.core.domain.AppException
import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ErrorType
import de.expatunity.core.domain.LocaleProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class ComplexDestination(val id: String)

sealed interface ComplexUiState {
    data object Loading : ComplexUiState
    data class Content(val complex: ComplexDetail) : ComplexUiState
    data class Error(val type: ErrorType) : ComplexUiState
}

@HiltViewModel
class ComplexViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ComplexRepository,
    private val localeProvider: LocaleProvider,
    ) : ViewModel() {
    private val id: String = checkNotNull(savedStateHandle["id"])
    private val _state = MutableStateFlow<ComplexUiState>(ComplexUiState.Loading)
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = ComplexUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                ComplexUiState.Content(repository.getComplex(id, localeProvider.apiLocale()))
            } catch (e: AppException) {
                ComplexUiState.Error(e.type)
            } catch (e: Exception) {
                ComplexUiState.Error(ErrorType.Unknown)
            }        }
    }
}