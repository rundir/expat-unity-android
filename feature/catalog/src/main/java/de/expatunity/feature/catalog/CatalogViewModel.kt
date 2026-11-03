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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val locale = localeProvider.apiLocale()
    private val _refreshing = MutableStateFlow(false)
    val refreshing = _refreshing.asStateFlow()
    private val _error = MutableStateFlow<ErrorType?>(null)
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    private val _events = Channel<CatalogEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    @OptIn(FlowPreview::class)
    val uiState = combine(
        repository.observeComplexes(locale),
        _error,
        _query.debounce { if (it.isEmpty()) 0 else 300 },
    ) { items, error, q ->
        when {
            items.isNotEmpty() -> CatalogUiState.Content(items.filterBy(q))
            error != null -> CatalogUiState.Error(error)
            _refreshing.value -> CatalogUiState.Loading
            else -> CatalogUiState.Content(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState.Loading)

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            _error.value = null
            try {
                repository.refreshComplexes(locale)
            } catch (e: CancellationException) { throw e
            } catch (e: AppException) {
                if (uiState.value is CatalogUiState.Content) _events.send(CatalogEvent.ShowError(e.type))
                else _error.value = e.type
            } catch (e: Exception) {
                if (uiState.value is CatalogUiState.Content) _events.send(CatalogEvent.ShowError(ErrorType.Unknown))
                else _error.value = ErrorType.Unknown
            } finally { _refreshing.value = false }
        }
    }

    fun onQueryChange(q: String) { _query.value = q }

    private fun List<ComplexSummary>.filterBy(q: String) =
        if (q.isBlank()) this else filter { it.name.contains(q, ignoreCase = true) }

    sealed interface CatalogEvent { data class ShowError(val type: ErrorType) : CatalogEvent }

}
