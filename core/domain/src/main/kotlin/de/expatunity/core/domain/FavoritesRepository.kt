package de.expatunity.core.domain

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavoriteIds(): Flow<Set<String>>
    suspend fun toggleFavorite(complexId: String)
}