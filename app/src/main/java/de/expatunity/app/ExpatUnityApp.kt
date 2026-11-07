package de.expatunity.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.expatunity.calculator.CalculatorDestination
import de.expatunity.calculator.CalculatorRoute
import de.expatunity.feature.catalog.CatalogRoute
import de.expatunity.feature.complex.ComplexDestination
import de.expatunity.feature.complex.ComplexRoute

@Composable
fun ExpatUnityApp() {
    val nav = rememberNavController()

    NavHost(nav, startDestination = CatalogDestination) {
        composable<CatalogDestination> {
            Scaffold { p ->
                CatalogRoute(onOpenComplex = { nav.navigate(ComplexDestination(it)) }, modifier = Modifier.padding(p))
            }
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