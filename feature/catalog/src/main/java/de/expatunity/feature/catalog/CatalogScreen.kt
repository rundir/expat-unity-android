package de.expatunity.feature.catalog

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.expatunity.core.domain.ComplexSummary
import de.expatunity.core.domain.ErrorType
import de.expatunity.core.domain.ListingStatus
import de.expatunity.core.ui.ErrorState
import de.expatunity.core.ui.ErrorTypeProvider
import de.expatunity.core.ui.LocalePreviews
import de.expatunity.core.ui.theme.ExpatUnityTheme

@Composable
fun CatalogRoute(
    favoritesOnly: Boolean = false,
    viewModel: CatalogViewModel = hiltViewModel(key = "catalog-$favoritesOnly"),
    onOpenComplex: (String) -> Unit,
) {
    LaunchedEffect(favoritesOnly) {
        if (favoritesOnly) viewModel.onFavoritesOnlyChange(favoritesOnly)
    }

    val snackbar = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val offlineMessage by rememberUpdatedState(stringResource(R.string.error_offline_cached))
    val cantAddFavMessage by rememberUpdatedState(stringResource(R.string.error_fav_failed))
    val filterFavorites by viewModel.favoritesOnly.collectAsStateWithLifecycle()


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
        onRetry = viewModel::refresh,
        onRefresh = viewModel::refresh,
        onOpenComplex = onOpenComplex,
        onQueryChange = viewModel::onQueryChange,
        onToggleFavorite = viewModel::onToggleFavorite,
        favoritesOnly = filterFavorites,
        onFavoritesOnlyChange = viewModel::onFavoritesOnlyChange,
        snackbarHostState = snackbar,
        showFavoritesFilter = !favoritesOnly,
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
    showFavoritesFilter: Boolean = true,
) {
    Scaffold(
        modifier = Modifier.focusable(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Box(
            Modifier.padding(padding)
        ) {
            when (state) {
                CatalogUiState.Loading -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false,
                ) {
                    items(4) { ComplexCardPlaceholder() }
                }

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

                    if (showFavoritesFilter) FilterChip(
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
                                        query.isNotBlank() -> stringResource(
                                            R.string.catalog_no_results,
                                            query
                                        )

                                        favoritesOnly -> stringResource(R.string.catalog_no_favorites)
                                        else -> stringResource(R.string.catalog_empty)
                                    }
                                )
                            }
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
            onToggleFavorite = {},
            favoritesOnly = false,
            onFavoritesOnlyChange = {},
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
    onToggleFavorite = {},
    favoritesOnly = false,
    onFavoritesOnlyChange = {},
    showFavoritesFilter = true,
)