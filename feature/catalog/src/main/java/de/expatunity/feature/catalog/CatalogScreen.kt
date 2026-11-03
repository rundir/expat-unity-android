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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import de.expatunity.app.ui.theme.ExpatUnityTheme
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ErrorType
import de.expatunity.core.domain.ListingStatus
import de.expatunity.core.ui.ErrorState
import de.expatunity.core.ui.ErrorTypeProvider
import de.expatunity.core.ui.LocalePreviews

@Composable
fun CatalogRoute(
    viewModel: CatalogViewModel = hiltViewModel(),
    onOpenComplex: (String) -> Unit,
    modifier: Modifier,
) {

    val snackbar = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val offlineMessage by rememberUpdatedState(stringResource(R.string.error_offline_cached))
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CatalogViewModel.CatalogEvent.ShowError -> snackbar.showSnackbar(offlineMessage)
            }
        }
    }

    CatalogScreen(
        state, refreshing, query,
        snackbarHostState = snackbar,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::refresh,
        onRefresh = viewModel::refresh,
        onOpenComplex = onOpenComplex,
        modifier = modifier,
    )
}

@Composable
fun CatalogScreen(
    state: CatalogUiState,
    refreshing: Boolean,
    query: String,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onOpenComplex: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold (
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Box(modifier.fillMaxSize().padding(padding)) {
            when (state) {
                CatalogUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                is CatalogUiState.Error ->
                    ErrorState(state.type, onRetry)

                is CatalogUiState.Content -> Column(Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text(stringResource(R.string.catalog_search_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.cd_clear_search)
                                )
                            }
                        },
                        singleLine = true,
                    )

                    PullToRefreshBox(
                        isRefreshing = refreshing,
                        onRefresh = onRefresh,
                    ) {
                        if (state.items.isEmpty()) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                Alignment.Center,
                            ) {
                                Text(
                                    if (query.isBlank()) stringResource(R.string.catalog_empty)
                                    else stringResource(R.string.catalog_no_results, query)
                                )
                            }
                        } else LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(state.items, key = { it.id }) {
                                ComplexCard(
                                    it,
                                    onClick = { onOpenComplex(it.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplexCard(item: ComplexSummary, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = item.coverImageUrl, contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        )
        Column(Modifier.padding(12.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
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

@Composable
private fun ListingStatus.label(): String = stringResource(
    when (this) {
        ListingStatus.ACTIVE -> R.string.status_active
        ListingStatus.RESERVED -> R.string.status_reserved
        ListingStatus.SOLD -> R.string.status_sold
        ListingStatus.UNKNOWN -> R.string.status_unknown
    }
)

@Preview
@LocalePreviews
@Composable
private fun CatalogPreview() {
    ExpatUnityTheme {
        CatalogScreen(

            CatalogUiState.Content(
                listOf(
                    ComplexSummary(
                        "1",
                        "Wohnpark Am See",
                        "Leipzig",
                        ListingStatus.ACTIVE,
                        249000.0,
                        null,
                        3
                    )
                )
            ),
            refreshing = false,
            "",
            onRetry = {},
            onRefresh = {},
            onOpenComplex = {},
            onQueryChange = {},
        )
    }
}

class UnitCountProvider : PreviewParameterProvider<Int> {
    override val values = sequenceOf(1, 3, 5, 21)
}

@LocalePreviews
@Composable
private fun UnitsPreview(
    @PreviewParameter(UnitCountProvider::class) count: Int
) {
    ExpatUnityTheme {
        Text(pluralStringResource(R.plurals.catalog_units_available, count, count))
    }
}

@LocalePreviews
@Composable
private fun CatalogError(
    @PreviewParameter(ErrorTypeProvider::class) type: ErrorType
) = CatalogScreen(
    CatalogUiState.Error(type),
    refreshing = false,
    "",
    onRetry = {},
    onRefresh = {},
    onOpenComplex = {},
    onQueryChange = {},
)