package de.expatunity.app

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.expatunity.core.domain.Apartment
import de.expatunity.core.ui.LocalNavAnimatedScope
import de.expatunity.core.ui.LocalSharedTransitionScope
import de.expatunity.feature.calculator.CalculatorDestination
import de.expatunity.feature.calculator.CalculatorRoute
import de.expatunity.feature.catalog.CatalogDestination
import de.expatunity.feature.catalog.CatalogRoute
import de.expatunity.feature.complex.ComplexRoute
import kotlinx.coroutines.launch

@Composable
fun ExpatUnityApp() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination
    Scaffold(
        bottomBar = {
            NavigationBar {
                TopLevelTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = current.isOn(tab),
                        onClick = { nav.navigateToTab(tab) },
                        icon = { Icon(tab.icon, null) },
                        label = { Text(stringResource(tab.label)) },
                    )
                }
            }
        },
    ) { padding ->
        SharedTransitionLayout(Modifier.padding(bottom = padding.calculateBottomPadding())) {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavHost(nav, startDestination = CatalogDestination) {
                    composable<CatalogDestination> { CatalogListDetail(nav, favoritesOnly = false) }
                    composable<FavoritesDestination> { CatalogListDetail(nav, favoritesOnly = true) }
                    composable<MapDestination> { TabPlaceholder("Map coming soon") }
                    composable<CalculatorDestination> {
                        CalculatorRoute(onBack = nav::popBackStack)
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun ComplexDetailPane(
    navigator: ThreePaneScaffoldNavigator<String>,
    onCalculate: (Apartment) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val showBack =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Hidden
    val currentId = navigator.currentDestination?.contentKey
    var lastId by rememberSaveable { mutableStateOf<String?>(null) }
    if (currentId != null) lastId = currentId
    lastId?.let { id ->
        ComplexRoute(
            complexId = id,
            onBack = { scope.launch { navigator.navigateBack() } },
            onCalculate = onCalculate,
            showBack = showBack
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun CatalogListDetail(nav: NavHostController, favoritesOnly: Boolean) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                    CatalogRoute(
                        favoritesOnly = favoritesOnly,
                        onOpenComplex = { id ->
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                            }
                        },
                    )
                }
            }
        },
        detailPane = {
            AnimatedPane {
                CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                    ComplexDetailPane(navigator = navigator, onCalculate = { a ->
                        nav.navigate(
                            CalculatorDestination(a.price, a.expectedRentMonthly),
                        ) { launchSingleTop = true }
                    })
                }
            }
        },
    )
}

@Composable
private fun TabPlaceholder(text: String) {
    Surface(Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) { Text(text) }
    }
}

private fun NavDestination?.isOn(tab: TopLevelTab) =
    this?.hierarchy?.any { it.hasRoute(tab.route::class) } == true

private fun NavHostController.navigateToTab(tab: TopLevelTab) =
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }