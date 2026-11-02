package de.expatunity.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.expatunity.core.domain.AppException
import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ErrorType
import de.expatunity.core.domain.LocaleProvider
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

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
    private val _query = MutableStateFlow("")

    val query = _query.asStateFlow()

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
            catch (e: Exception) {  }
            finally { _refreshing.value = false }
        }
    }

    fun onQueryChange(q: String) { _query.value = q }

    @OptIn(FlowPreview::class)
    val uiState = combine(_state, _query.debounce(300.milliseconds)) { state, q ->
        if (state is CatalogUiState.Content && q.isNotBlank())
            state.copy(items = state.items.filter {
                it.name.contains(q, ignoreCase = true) ||
                        (it.city?.contains(q, ignoreCase = true) == true)
            })
        else state
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState.Loading)

}
