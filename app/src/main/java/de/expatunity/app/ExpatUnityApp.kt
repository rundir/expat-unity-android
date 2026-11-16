package de.expatunity.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.expatunity.feature.calculator.CalculatorDestination
import de.expatunity.feature.calculator.CalculatorRoute
import de.expatunity.feature.catalog.CatalogDestination
import de.expatunity.feature.catalog.CatalogRoute
import de.expatunity.feature.complex.ComplexDestination
import de.expatunity.feature.complex.ComplexRoute

@Composable
fun ExpatUnityApp() {
    val nav = rememberNavController()

    NavHost(nav, startDestination = CatalogDestination) {
        composable<CatalogDestination> {
            CatalogRoute(onOpenComplex = { nav.navigate(ComplexDestination(it)) })
        }
        composable<ComplexDestination> {
            ComplexRoute(
                onBack = nav::popBackStack,
                onCalculate = { a -> nav.navigate(CalculatorDestination(a.price, a.expectedRentMonthly)) },
            )
        }
        composable<CalculatorDestination> {
            CalculatorRoute(
                onBack = nav::popBackStack,
                )
        }
    }
}