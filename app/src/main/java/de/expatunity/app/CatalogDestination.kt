package de.expatunity.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.expatunity.calculator.CalculatorDestination
import de.expatunity.calculator.CalculatorRoute
import de.expatunity.core.network.NetworkModule
import de.expatunity.feature.catalog.CatalogRoute
import de.expatunity.feature.catalog.CatalogViewModel
import de.expatunity.feature.complex.ComplexDestination
import de.expatunity.feature.complex.ComplexRoute
import de.expatunity.feature.complex.ComplexViewModel
import kotlinx.serialization.Serializable

@Serializable
object CatalogDestination // можно положить в feature:catalog

@Composable
fun ExpatUnityApp() {
    val nav = rememberNavController()
    val repo = NetworkModule.complexRepository
    val locale = "de"

    NavHost(nav, startDestination = CatalogDestination) {
        composable<CatalogDestination> {
            val vm: CatalogViewModel = viewModel(factory = CatalogViewModel.factory(repo, locale))
            Scaffold { p ->
                CatalogRoute(vm, onOpenComplex = { nav.navigate(ComplexDestination(it)) }, modifier = Modifier.padding(p))
            }
        }
        composable<ComplexDestination> {
            val vm: ComplexViewModel = viewModel(factory = ComplexViewModel.factory(repo, locale))
            ComplexRoute(
                vm,
                onBack = nav::popBackStack,
                onCalculate = { a -> nav.navigate(CalculatorDestination(a.price, a.expectedRentMonthly)) },
            )
        }
        composable<CalculatorDestination> {
            CalculatorRoute(
                onBack = nav::popBackStack,
                ) // внутри возьми SavedStateHandle через factory так же, как в ComplexViewModel
        }
    }
}