package de.expatunity.core.database

import androidx.room.Entity

@Entity(
    tableName = "complex_apartments",
    primaryKeys = ["complexId", "locale", "id"],
)
data class ApartmentEntity(
    val complexId: String,
    val locale: String,
    val id: String,
    val title: String,
    val areaSqm: Double?,
    val price: Double?,
    val expectedRentMonthly: Double?,
    val status: String,
)