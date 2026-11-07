package de.expatunity.core.domain

data class Apartment(
    val id: String, val title: String, val areaSqm: Double?,
    val price: Double?, val expectedRentMonthly: Double?, val status: ListingStatus,
)