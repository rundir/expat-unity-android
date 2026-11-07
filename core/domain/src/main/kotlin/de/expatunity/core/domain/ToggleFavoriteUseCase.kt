package de.expatunity.core.domain

class ToggleFavoriteUseCase(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(complexId: String) {
        repository.toggleFavorite(complexId)
    }
}