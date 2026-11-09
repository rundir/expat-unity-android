package de.expatunity.core.domain.usecase

import de.expatunity.core.domain.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteIdsUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(): Flow<Set<String>> = repository.observeFavoriteIds()
}