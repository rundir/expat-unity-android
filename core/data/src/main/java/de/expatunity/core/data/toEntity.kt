package de.expatunity.core.data

import de.expatunity.core.database.ComplexEntity
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.Location

fun ComplexSummary.toEntity(locale: String) =
    ComplexEntity(
        id,
        locale,
        name,
        city,
        status.name,
        priceFrom,
        coverImageUrl,
        unitsAvailable,
        location?.lat,
        location?.lng,
    )

fun ComplexEntity.toDomain() =
    ComplexSummary(
        id,
        name,
        city,
        status.toStatus(),
        priceFrom,
        coverImageUrl,
        unitsAvailable,
        location = if (lat != null && lng != null) Location(lat, lng) else null,
    )