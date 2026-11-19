package de.expatunity.core.network

import kotlinx.serialization.Serializable

@Serializable
data class ComplexListDto(val items: List<ComplexSummaryDto>, val page: Int, val pageSize: Int, val total: Int)

@Serializable
data class ComplexSummaryDto(
    val id: String, val name: String, val city: String? = null, val status: String,
    val location: LocationDto? = null,
    val priceFrom: Double? = null, val coverImageUrl: String? = null, val unitsAvailable: Int = 0,
)

@Serializable
data class UnitDto(
    val id: String, val title: String, val areaSqm: Double? = null, val price: Double? = null,
    val expectedRentMonthly: Double? = null, val status: String,
)

@Serializable
data class ComplexDetailDto(
    val id: String, val name: String, val city: String? = null, val address: String? = null,
    val status: String, val description: String? = null, val images: List<String> = emptyList(),
    val location: LocationDto? = null,
    val units: List<UnitDto> = emptyList(), val fallbackUsed: Boolean = false,
    val videoUrl: String? = null,
    )

@Serializable
data class LocationDto(val lat: Double?, val lng: Double?)