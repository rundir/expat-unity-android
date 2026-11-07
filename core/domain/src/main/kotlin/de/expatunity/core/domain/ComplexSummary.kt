package de.expatunity.core.domain

data class ComplexSummary(
    val id: String, val name: String, val city: String?,
    val status: ListingStatus, val priceFrom: Double?,
    val coverImageUrl: String?, val unitsAvailable: Int,
)