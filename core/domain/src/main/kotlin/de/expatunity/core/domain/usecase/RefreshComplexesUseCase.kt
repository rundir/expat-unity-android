package de.expatunity.core.domain.usecase

import de.expatunity.core.domain.ComplexRepository

class RefreshComplexesUseCase(private val repository: ComplexRepository) {
    suspend operator fun invoke(locale: String) = repository.refreshComplexes(locale)
}