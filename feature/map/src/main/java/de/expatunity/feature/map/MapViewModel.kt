package de.expatunity.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.expatunity.core.domain.LocaleProvider
import de.expatunity.core.domain.usecase.ObserveComplexesUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MapViewModel @Inject constructor(
    observeComplexes: ObserveComplexesUseCase,
    localeProvider: LocaleProvider,
    ) : ViewModel() {
    private val locale = localeProvider.apiLocale()

    val markers = observeComplexes(locale)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}