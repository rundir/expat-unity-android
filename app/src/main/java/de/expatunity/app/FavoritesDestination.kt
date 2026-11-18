package de.expatunity.app

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector
import de.expatunity.feature.catalog.CatalogDestination
import kotlinx.serialization.Serializable

@Serializable
data object FavoritesDestination
@Serializable
data object MapDestination

enum class TopLevelTab(val route: Any, val icon: ImageVector, @StringRes val label: Int) {
    Catalog(CatalogDestination, Icons.AutoMirrored.Filled.List, R.string.tab_catalog),
    Favorites(FavoritesDestination, Icons.Default.Favorite, R.string.tab_favorites),
    Map(MapDestination, Icons.Default.Place, R.string.tab_map),
}