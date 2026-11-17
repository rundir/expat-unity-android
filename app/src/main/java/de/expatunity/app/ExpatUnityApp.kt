package de.expatunity.app

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.expatunity.core.ui.LocalNavAnimatedScope
import de.expatunity.core.ui.LocalSharedTransitionScope
import de.expatunity.feature.calculator.CalculatorDestination
import de.expatunity.feature.calculator.CalculatorRoute
import de.expatunity.feature.catalog.CatalogDestination
import de.expatunity.feature.catalog.CatalogRoute
import de.expatunity.feature.complex.ComplexDestination
import de.expatunity.feature.complex.ComplexRoute

@Composable
fun ExpatUnityApp() {
    val nav = rememberNavController()

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(nav, startDestination = CatalogDestination) {
                composable<CatalogDestination> { backStackEntry ->
                    CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                        CatalogRoute(
                            onOpenComplex = { id ->
                                if (backStackEntry.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                    nav.navigate(ComplexDestination(id))
                                }
                            },
                        )
                    }
                }
                composable<ComplexDestination> {
                    CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                        ComplexRoute(
                            onBack = nav::popBackStack,
                            onCalculate = { a ->
                                nav.navigate(
                                    CalculatorDestination(
                                        a.price, a.expectedRentMonthly
                                    )
                                )
                            },
                        )
                    }
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