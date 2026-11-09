package de.expatunity.core.domain.usecase

import de.expatunity.core.domain.ComplexRepository

class RefreshComplexUseCase(private val repository: ComplexRepository) {
    suspend operator fun invoke(id: String, locale: String) =
        repository.refreshComplex(id, locale)
}