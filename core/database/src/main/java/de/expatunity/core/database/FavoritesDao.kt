package de.expatunity.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FavoritesDao {
    @Query("SELECT complexId FROM favorites")
    abstract fun observeIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE complexId = :id)")
    abstract suspend fun contains(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE complexId = :id")
    abstract suspend fun delete(id: String)

    @Transaction
    open suspend fun toggle(id: String) {
        if (contains(id)) delete(id)
        else insert(FavoriteEntity(id))
    }
}