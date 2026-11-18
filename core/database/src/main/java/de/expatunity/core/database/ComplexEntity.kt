package de.expatunity.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "complexes", primaryKeys = ["id", "locale"])
data class ComplexEntity(
    val id: String,
    val locale: String,
    val name: String,
    val city: String?,
    val status: String,
    val priceFrom: Double?,
    val coverImageUrl: String?,
    val unitsAvailable: Int,
    val lat: Double?,
    val lng: Double?,
)

@Dao
interface ComplexDao {
    @Query("SELECT * FROM complexes WHERE locale = :locale")
    fun observe(locale: String): Flow<List<ComplexEntity>>

    @Upsert
    suspend fun upsertAll(items: List<ComplexEntity>)

    @Query("DELETE FROM complexes WHERE locale = :locale")
    suspend fun clear(locale: String)

    @Transaction
    suspend fun replaceAll(locale: String, items: List<ComplexEntity>) {
        clear(locale)
        upsertAll(items)
    }}