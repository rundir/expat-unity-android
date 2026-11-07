package de.expatunity.core.network

import de.expatunity.core.database.CachedComplexDetail
import de.expatunity.core.domain.Apartment
import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ListingStatus

fun CachedComplexDetail.toDomain() = ComplexDetail(
    id = detail.id,
    name = detail.name,
    city = detail.city,
    address = detail.address,
    status = ListingStatus.valueOf(detail.status),
    description = detail.description,
    images = images.map { it.url },
    apartments = apartments.map {
        Apartment(
            id = it.id,
            title = it.title,
            areaSqm = it.areaSqm,
            price = it.price,
            expectedRentMonthly = it.expectedRentMonthly,
            status = ListingStatus.valueOf(it.status),
        )
    },
    fallbackUsed = detail.fallbackUsed,
)