package de.expatunity.app

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.expatunity.core.ui.LocalNavAnimatedScope
import de.expatunity.core.ui.LocalSharedTransitionScope
import de.expatunity.feature.calculator.CalculatorDestination
import de.expatunity.feature.calculator.CalculatorRoute
import de.expatunity.feature.catalog.CatalogDestination
import de.expatunity.feature.catalog.CatalogRoute
import de.expatunity.feature.complex.ComplexRoute
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ExpatUnityApp() {
    val nav = rememberNavController()

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(nav, startDestination = CatalogDestination) {
                composable<CatalogDestination> {
                    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
                    val scope = rememberCoroutineScope()

                    NavigableListDetailPaneScaffold(
                        navigator = navigator,
                        listPane = {
                            AnimatedPane {
                                CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                                    CatalogRoute(
                                        onOpenComplex = { id ->
                                            scope.launch {
                                                navigator.navigateTo(
                                                    ListDetailPaneScaffoldRole.Detail, id
                                                )
                                            }
                                        },
                                    )
                                }
                            }
                        },
                        detailPane = {
                            AnimatedPane {
                                CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                                    val showBack = navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Hidden
                                    val currentId = navigator.currentDestination?.contentKey
                                    var lastId by rememberSaveable { mutableStateOf<String?>(null) }
                                    if (currentId != null) lastId = currentId
                                    lastId?.let { id ->
                                        ComplexRoute(
                                            complexId = id,
                                            onBack = { scope.launch { navigator.navigateBack() } },
                                            onCalculate = { a ->
                                                nav.navigate(
                                                    CalculatorDestination(
                                                        a.price, a.expectedRentMonthly
                                                    ),
                                                ) {
                                                    launchSingleTop = true
                                                }
                                            },
                                            showBack = showBack
                                        )
                                    }
                                }
                            }
                        },
                    )
                }
                composable<CalculatorDestination> {
                    CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                        CalculatorRoute(
                            onBack = nav::popBackStack,
                        )
                    }
                }
            }
        }
    }
}