package de.expatunity.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.expatunity.core.domain.AppException
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ErrorType
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
    data class Error(val type: ErrorType) : CatalogUiState
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
            } catch (e: AppException) {
                CatalogUiState.Error(e.type)
            } catch (e: Exception) {
                CatalogUiState.Error(ErrorType.Unknown)
            }
        }
    }
    private val _refreshing = MutableStateFlow(false)
    val refreshing = _refreshing.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            try {
                _state.value = CatalogUiState.Content(repository.getComplexes(localeProvider.apiLocale()))
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { /* оставляем старый контент; позже можно показать Snackbar */ }
            finally { _refreshing.value = false }
        }
    }

}
