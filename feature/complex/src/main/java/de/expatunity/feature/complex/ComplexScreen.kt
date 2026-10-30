package de.expatunity.feature.complex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.expatunity.app.ui.theme.ExpatUnityTheme
import de.expatunity.core.domain.Apartment
import de.expatunity.core.domain.ComplexDetail
import de.expatunity.core.domain.ListingStatus
import de.expatunity.core.ui.ImageGallery
import de.expatunity.core.ui.LocalePreviews

@Composable
fun ComplexRoute(
    viewModel: ComplexViewModel = hiltViewModel(),
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
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigate_back),
                    )
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
                Text(state.message ?: stringResource(R.string.error_generic))
                Button(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
            }
            is ComplexUiState.Content -> LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val c = state.complex
                item {
                    ImageGallery(c.images)
                }
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        c.address?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        c.description?.let { Text(it, Modifier.padding(top = 8.dp)) }
                        Text(
                            stringResource(R.string.complex_apartments_section),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                }
                items(c.apartments, key = { it.id }) { a ->
                    ListItem(
                        headlineContent = {
                            Text(
                                stringResource(
                                    R.string.apartment_headline,
                                    a.title,
                                    a.areaSqm?.toString() ?: stringResource(R.string.value_unknown),
                                )
                            )
                        },
                        supportingContent = {
                            Text(
                                a.price?.let { stringResource(R.string.price_eur, it) }
                                    ?: stringResource(R.string.price_on_request)
                            )
                        },
                        trailingContent = {
                            if (a.status == ListingStatus.ACTIVE && a.price != null)
                                TextButton(onClick = { onCalculate(a) }) {
                                    Text(stringResource(R.string.action_calculate))
                                }
                            else Text(a.status.label(), style = MaterialTheme.typography.labelSmall)
                        },
                    )
                }
            }
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

private val previewComplex = ComplexDetail(
    id = "1",
    name = "Mobile Test Landshut",
    address = "Landshut",
    description = "Neubau, 2026",
    images = listOf("test","test1", "test2"),
    apartments = listOf(
        Apartment(
            id = "a1", title = "1", areaSqm = 54.0, price = 165000.0,
            expectedRentMonthly = 650.0, status = ListingStatus.ACTIVE
        ),
        Apartment(
            id = "a2", title = "2", areaSqm = 72.0, price = null,
            expectedRentMonthly = null, status = ListingStatus.SOLD
        ),
    ),
    city = "Landshut",
    status = ListingStatus.ACTIVE,
    fallbackUsed = false,
)

@LocalePreviews
@Composable
private fun ComplexScreenPreview() {
    ExpatUnityTheme {
        ComplexScreen(
            state = ComplexUiState.Content(previewComplex),
            onBack = {}, onRetry = {}, onCalculate = {},
        )
    }
}

@Preview
@Composable
private fun ComplexErrorPreview() {
    ExpatUnityTheme {
        ComplexScreen(ComplexUiState.Error("offline"), {}, {}, {})
    }
}