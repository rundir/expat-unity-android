package de.expatunity.core.domain.usecase

import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ComplexRepository
import kotlinx.coroutines.flow.Flow

class ObserveComplexUseCase(private val repository: ComplexRepository) {
    operator fun invoke(id: String, locale: String): Flow<ComplexDetail?> =
        repository.observeComplex(id, locale)
}