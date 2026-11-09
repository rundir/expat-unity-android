package de.expatunity.core.domain.usecase

import de.expatunity.core.domain.FavoritesRepository

class ToggleFavoriteUseCase(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(complexId: String) {
        repository.toggleFavorite(complexId)
    }
}