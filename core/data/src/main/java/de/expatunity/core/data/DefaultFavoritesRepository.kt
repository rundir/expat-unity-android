package de.expatunity.core.data

import de.expatunity.core.database.AppDatabase
import de.expatunity.core.domain.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultFavoritesRepository @Inject constructor(
    private val database: AppDatabase,
) : FavoritesRepository {
    override fun observeFavoriteIds(): Flow<Set<String>> =
        database.favoritesDao().observeIds().map { it.toSet() }

    override suspend fun toggleFavorite(complexId: String) {
        database.favoritesDao().toggle(complexId)
    }
}