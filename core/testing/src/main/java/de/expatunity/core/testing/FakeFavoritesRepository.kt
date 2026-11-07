package de.expatunity.core.testing

import de.expatunity.core.domain.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeFavoritesRepository : FavoritesRepository {
    val favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    var error: Throwable? = null

    override fun observeFavoriteIds() = favoriteIds

    override suspend fun toggleFavorite(complexId: String) {
        error?.let { throw it }
        favoriteIds.update { ids ->
            if (complexId in ids) ids - complexId
            else ids + complexId
        }
    }
}