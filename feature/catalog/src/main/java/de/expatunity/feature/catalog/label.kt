package de.expatunity.feature.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.expatunity.core.domain.ListingStatus

@Composable
fun ListingStatus.label(): String = stringResource(
    when (this) {
        ListingStatus.ACTIVE -> R.string.status_active
        ListingStatus.RESERVED -> R.string.status_reserved
        ListingStatus.SOLD -> R.string.status_sold
        ListingStatus.UNKNOWN -> R.string.status_unknown
    }
)