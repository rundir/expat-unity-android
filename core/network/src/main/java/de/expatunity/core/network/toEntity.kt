package de.expatunity.core.network

import de.expatunity.core.database.ComplexEntity
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ListingStatus

fun ComplexSummary.toEntity(locale: String) =
    ComplexEntity(
        id,
        locale,
        name,
        city,
        status.name,
        priceFrom,
        coverImageUrl,
        unitsAvailable
    )

fun ComplexEntity.toDomain() =
    ComplexSummary(
        id, name, city, ListingStatus.valueOf(status), priceFrom, coverImageUrl, unitsAvailable
    )