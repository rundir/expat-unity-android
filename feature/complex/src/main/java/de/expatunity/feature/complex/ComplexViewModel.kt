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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

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
    private val locale = localeProvider.apiLocale()
    private val error = MutableStateFlow<ErrorType?>(null)
    private var refreshJob: Job? = null
    val refreshError = error.asStateFlow()

    val state: StateFlow<ComplexUiState> = combine(
        repository.observeComplex(id, locale),
        error,
    ) { cached, refreshError ->
        when {
            cached != null -> ComplexUiState.Content(cached)
            refreshError != null -> ComplexUiState.Error(refreshError)
            else -> ComplexUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ComplexUiState.Loading,
    )

    init {
        load()
    }

    fun load() {
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            error.value = null
            try {
                repository.refreshComplex(id, locale)
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppException) {
                error.value = e.type
            } catch (e: Exception) {
                error.value = ErrorType.Unknown
            }
        }
    }}