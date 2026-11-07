package de.expatunity.core.domain

data class ComplexDetail(
    val id: String, val name: String, val city: String?, val address: String?,
    val status: ListingStatus, val description: String?, val images: List<String>,
    val apartments: List<Apartment>, val fallbackUsed: Boolean,
)