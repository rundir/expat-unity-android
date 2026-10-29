package de.expatunity.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ListingStatus

@Composable
fun CatalogRoute(
    viewModel: CatalogViewModel = hiltViewModel(),
    onOpenComplex: (String) -> Unit,
    modifier: Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle<CatalogUiState>()
    CatalogScreen(state, onRetry = viewModel::load, onOpenComplex = onOpenComplex, modifier)
}

@Composable
fun CatalogScreen(
    state: CatalogUiState,
    onRetry: () -> Unit,
    onOpenComplex: (String) -> Unit,
    modifier: Modifier
) {
    when (state) {
        CatalogUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is CatalogUiState.Error -> Column(
            Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(state.message)
            Button(onClick = onRetry) { Text("Повторить") }
        }
        is CatalogUiState.Content ->
            if (state.items.isEmpty()) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Пока нет объектов") }
            else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
                items(state.items, key = { it.id }) { ComplexCard(it, onClick = { onOpenComplex(it.id) }) }

            }
    }
}

@Composable
private fun ComplexCard(item: ComplexSummary, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = item.coverImageUrl, contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(180.dp),
        )
        Column(Modifier.padding(12.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            item.city?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Text(item.priceFrom?.let { "от %,.0f €".format(it) } ?: "Цена по запросу")
            Text("${item.status} · свободно: ${item.unitsAvailable}", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Preview
@Composable
private fun CatalogPreview() = CatalogScreen(
    CatalogUiState.Content(listOf(ComplexSummary("1", "Wohnpark Am See", "Leipzig", ListingStatus.ACTIVE, 249000.0, null, 3))),
    onRetry = {}, onOpenComplex = {}, Modifier.padding(),
)