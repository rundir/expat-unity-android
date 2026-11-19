package de.expatunity.core.database

import androidx.room.Entity

@Entity(
    tableName = "complex_details",
    primaryKeys = ["id", "locale"],
)
data class ComplexDetailEntity(
    val id: String,
    val locale: String,
    val name: String,
    val city: String?,
    val address: String?,
    val status: String,
    val description: String?,
    val fallbackUsed: Boolean,
    val videoUrl: String?
)