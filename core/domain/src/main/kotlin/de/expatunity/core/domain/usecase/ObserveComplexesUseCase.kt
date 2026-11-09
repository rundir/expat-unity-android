package de.expatunity.core.domain.usecase

import de.expatunity.core.domain.ComplexRepository
import de.expatunity.core.domain.ComplexSummary
import kotlinx.coroutines.flow.Flow

class ObserveComplexesUseCase(private val repository: ComplexRepository) {
    operator fun invoke(locale: String): Flow<List<ComplexSummary>> =
        repository.observeComplexes(locale)
}

