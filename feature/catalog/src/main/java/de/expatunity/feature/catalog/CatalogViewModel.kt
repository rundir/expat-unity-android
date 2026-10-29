package de.expatunity.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.LocaleProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Content(val items: List<ComplexSummary>) : CatalogUiState
    data class Error(val message: String?) : CatalogUiState
}

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repository: ComplexRepository,
    private val localeProvider: LocaleProvider
) : ViewModel() {

    private val _state = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = CatalogUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                CatalogUiState.Content(repository.getComplexes(localeProvider.apiLocale()))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                CatalogUiState.Error(e.message)
            }
        }
    }
}