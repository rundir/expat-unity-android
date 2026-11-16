package de.expatunity.feature.complex

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.expatunity.core.domain.AppException
import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ErrorType
import de.expatunity.core.domain.LocaleProvider
import de.expatunity.core.domain.usecase.ObserveComplexUseCase
import de.expatunity.core.domain.usecase.ObserveFavoriteIdsUseCase
import de.expatunity.core.domain.usecase.RefreshComplexUseCase
import de.expatunity.core.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class ComplexDestination(val id: String)

sealed interface ComplexUiState {
    data object Loading : ComplexUiState
    data class Content(
        val complex: ComplexDetail,
        val isFavorite: Boolean = false,
    ) : ComplexUiState
    data class Error(val type: ErrorType) : ComplexUiState
}

sealed interface ComplexEvent {
    data object FavoriteUpdateFailed : ComplexEvent
}

@HiltViewModel
class ComplexViewModel @Inject constructor(
    observeComplex: ObserveComplexUseCase,
    private val refreshComplex: RefreshComplexUseCase,
    observeFavoriteIds: ObserveFavoriteIdsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    localeProvider: LocaleProvider,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val id: String = checkNotNull(savedStateHandle["id"])
    private val locale = localeProvider.apiLocale()
    private val error = MutableStateFlow<ErrorType?>(null)
    private var refreshJob: Job? = null
    val refreshError = error.asStateFlow()
    private val _events = Channel<ComplexEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val state = combine(
        observeComplex(id, locale),
        error,
        observeFavoriteIds().map { id in it }.distinctUntilChanged(),
    ) { cached, err, isFavorite ->
        when {
            cached != null -> ComplexUiState.Content(cached, isFavorite)
            err != null -> ComplexUiState.Error(err)
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
                refreshComplex(id, locale)
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppException) {
                error.value = e.type
            } catch (e: Exception) {
                error.value = ErrorType.Unknown
            }
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch {
            try {
                toggleFavorite(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(ComplexEvent.FavoriteUpdateFailed)
            }
        }
    }
}