package de.expatunity.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
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
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ErrorType
import de.expatunity.core.domain.ListingStatus
import de.expatunity.core.ui.ErrorState
import de.expatunity.core.ui.ErrorTypeProvider
import de.expatunity.core.ui.FavoriteButton
import de.expatunity.core.ui.LocalePreviews
import de.expatunity.core.ui.theme.ExpatUnityTheme

@Composable
fun CatalogRoute(
    viewModel: CatalogViewModel = hiltViewModel(),
    onOpenComplex: (String) -> Unit,
) {

    val snackbar = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val offlineMessage by rememberUpdatedState(stringResource(R.string.error_offline_cached))
    val cantAddFavMessage by rememberUpdatedState(stringResource(R.string.error_fav_failed))
    val favoritesOnly by viewModel.favoritesOnly.collectAsStateWithLifecycle()


    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CatalogViewModel.CatalogEvent.ShowError -> snackbar.showSnackbar(offlineMessage)
                CatalogViewModel.CatalogEvent.FavoriteUpdateFailed -> snackbar.showSnackbar(
                    cantAddFavMessage
                )
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
        onToggleFavorite = viewModel::onToggleFavorite,
        favoritesOnly = favoritesOnly,
        onFavoritesOnlyChange = viewModel::onFavoritesOnlyChange,
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
    onToggleFavorite: (String) -> Unit,
    favoritesOnly: Boolean,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Box(
            Modifier.padding(padding)
        ) {
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

                    FilterChip(
                        selected = favoritesOnly,
                        onClick = { onFavoritesOnlyChange(!favoritesOnly) },
                        label = { Text(stringResource(R.string.catalog_favorites_only)) },
                        modifier = Modifier.padding(horizontal = 16.dp),
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
                                    when {
                                        query.isNotBlank() -> stringResource(R.string.catalog_no_results, query)
                                        favoritesOnly -> stringResource(R.string.catalog_no_favorites)
                                        else -> stringResource(R.string.catalog_empty)
                                    }
                                )                            }
                        } else LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(state.items, key = { it.id }) { item ->
                                ComplexCard(
                                    item = item,
                                    isFavorite = item.id in state.favoriteIds,
                                    onToggleFavorite = { onToggleFavorite(item.id) },
                                    onClick = { onOpenComplex(item.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplexCard(
    item: ComplexSummary,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = item.coverImageUrl, contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
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
            favoritesOnly = false,
            onRetry = {},
            onRefresh = {},
            onOpenComplex = {},
            onQueryChange = {},
            onToggleFavorite = {},
            onFavoritesOnlyChange = {}
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
    favoritesOnly = false,
    onRetry = {},
    onRefresh = {},
    onOpenComplex = {},
    onQueryChange = {},
    onToggleFavorite = {},
    onFavoritesOnlyChange = {},
)