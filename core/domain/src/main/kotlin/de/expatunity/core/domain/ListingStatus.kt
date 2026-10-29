package de.expatunity.core.domain

enum class ListingStatus { ACTIVE, RESERVED, SOLD, UNKNOWN }

data class ComplexSummary(
    val id: String, val name: String, val city: String?,
    val status: ListingStatus, val priceFrom: Double?,
    val coverImageUrl: String?, val unitsAvailable: Int,
)

data class Apartment(
    val id: String, val title: String, val areaSqm: Double?,
    val price: Double?, val expectedRentMonthly: Double?, val status: ListingStatus,
)

data class ComplexDetail(
    val id: String, val name: String, val city: String?, val address: String?,
    val status: ListingStatus, val description: String?, val images: List<String>,
    val apartments: List<Apartment>, val fallbackUsed: Boolean,
)