package de.expatunity.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
abstract class ComplexDetailDao {
    @Query("SELECT * FROM complex_details WHERE id = :id AND locale = :locale")
    abstract suspend fun getDetail(
        id: String, locale: String,
    ): ComplexDetailEntity?

    @Query("""
        SELECT * FROM complex_images
        WHERE complexId = :id AND locale = :locale
        ORDER BY position
    """)
    abstract suspend fun getImages(
        id: String, locale: String,
    ): List<ComplexImageEntity>

    @Query("""
        SELECT * FROM complex_apartments
        WHERE complexId = :id AND locale = :locale
        ORDER BY id
    """)
    abstract suspend fun getApartments(
        id: String, locale: String,
    ): List<ApartmentEntity>

    @Transaction
    open suspend fun getCached(
        id: String, locale: String,
    ): CachedComplexDetail? {
        val detail = getDetail(id, locale) ?: return null
        return CachedComplexDetail(
            detail = detail,
            images = getImages(id, locale),
            apartments = getApartments(id, locale),
        )
    }
    @Upsert
    abstract suspend fun upsertDetail(detail: ComplexDetailEntity)

    @Insert
    abstract suspend fun insertImages(images: List<ComplexImageEntity>)

    @Insert
    abstract suspend fun insertApartments(apartments: List<ApartmentEntity>)

    @Query("DELETE FROM complex_images WHERE complexId = :id AND locale = :locale")
    abstract suspend fun clearImages(id: String, locale: String)

    @Query("DELETE FROM complex_apartments WHERE complexId = :id AND locale = :locale")
    abstract suspend fun clearApartments(id: String, locale: String)

    @Transaction
    open suspend fun replaceAll(cached: CachedComplexDetail) {
        val detail = cached.detail
        upsertDetail(detail)
        clearImages(detail.id, detail.locale)
        clearApartments(detail.id, detail.locale)
        insertImages(cached.images)
        insertApartments(cached.apartments)
    }

//    fun observe(id: String, locale: String): Flow<CachedComplexDetail?> =
//        dbInvalidations()
//            .map { getCached(id, locale) }
}

data class CachedComplexDetail(
    val detail: ComplexDetailEntity,
    val images: List<ComplexImageEntity>,
    val apartments: List<ApartmentEntity>,
)