package de.expatunity.core.testing

import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ListingStatus

fun sampleSummary(id: String, name: String): ComplexSummary {
        return ComplexSummary(
            id,
            name,
            "city",
            ListingStatus.ACTIVE,
            0.0,
            "coverImageUrl",
            9
        )
    }