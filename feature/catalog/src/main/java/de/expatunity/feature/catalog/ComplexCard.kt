package de.expatunity.feature.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.ui.FavoriteButton
import de.expatunity.core.ui.sharedComplexImage

@Composable
fun ComplexCard(
    item: ComplexSummary,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = item.coverImageUrl,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .sharedComplexImage(item.id, tag = "TAG-CARD")
                .fillMaxWidth()
                .height(180.dp),
        )
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconToggleButton(
                    checked = isFavorite,
                    onCheckedChange = { onToggleFavorite() },
                ) {
                    FavoriteButton(
                        isFavorite = isFavorite,
                        onToggle = { onToggleFavorite() },
                        addLabel = stringResource(R.string.action_add_favorite),
                        removeLabel = stringResource(R.string.action_remove_favorite),
                    )
                }
            }
            item.city?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Text(
                item.priceFrom?.let { stringResource(R.string.price_from, it) }
                    ?: stringResource(R.string.price_on_request)
            )
            val units = pluralStringResource(
                R.plurals.catalog_units_available,
                item.unitsAvailable,
                item.unitsAvailable
            )
            Text(
                stringResource(
                    R.string.catalog_status_units,
                    item.status.label(),
                    units,
                ),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}