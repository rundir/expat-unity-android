package de.expatunity.core.database

import androidx.room.Entity

@Entity(
    tableName = "complex_images",
    primaryKeys = ["complexId", "locale", "position"],
)
data class ComplexImageEntity(
    val complexId: String,
    val locale: String,
    val position: Int,
    val url: String,
)