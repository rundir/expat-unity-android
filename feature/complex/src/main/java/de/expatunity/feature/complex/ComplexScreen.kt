package de.expatunity.feature.complex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import de.expatunity.core.domain.Apartment
import de.expatunity.core.domain.ListingStatus

@Composable
fun ComplexRoute(
    viewModel: ComplexViewModel,
    onBack: () -> Unit,
    onCalculate: (Apartment) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ComplexScreen(state, onBack, onRetry = viewModel::load, onCalculate)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplexScreen(
    state: ComplexUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onCalculate: (Apartment) -> Unit,
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text((state as? ComplexUiState.Content)?.complex?.name ?: "") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
        )
    }) { padding ->
        when (state) {
            ComplexUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            is ComplexUiState.Error -> Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(state.message)
                Button(onClick = onRetry) { Text("Повторить") }
            }
            is ComplexUiState.Content -> LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val c = state.complex
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(c.images) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(280.dp, 180.dp).clip(RoundedCornerShape(12.dp)),
                            )
                        }
                    }
                }
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        c.address?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        c.description?.let { Text(it, Modifier.padding(top = 8.dp)) }
                        Text("Квартиры", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                    }
                }
                items(c.apartments, key = { it.id }) { a ->
                    ListItem(
                        headlineContent = { Text("№ ${a.title} · ${a.areaSqm ?: "?"} м²") },
                        supportingContent = { Text(a.price?.let { "%,.0f €".format(it) } ?: "Цена по запросу") },
                        trailingContent = {
                            if (a.status == ListingStatus.ACTIVE && a.price != null)
                                TextButton(onClick = { onCalculate(a) }) { Text("Посчитать") }
                            else Text(a.status.name, style = MaterialTheme.typography.labelSmall)
                        },
                    )
                }
            }
        }
    }
}