package de.expatunity.calculator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import de.expatunity.core.domain.InvestmentCalculator
import de.expatunity.core.domain.InvestmentParams
import de.expatunity.core.domain.InvestmentResult
import de.expatunity.core.domain.YieldCalculator
import de.expatunity.core.domain.YieldInput
import de.expatunity.core.domain.YieldResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

@Serializable
data class CalculatorDestination(val price: Double? = null, val rent: Double? = null)

enum class Field(val label: String, val default: String) {
    Price("Цена, €", "318000"), Cash("Свои средства, €", "70000"),
    Expenses("Расходы сделки, €", "0"), Interest("Ставка, %", "4.3"),
    Tilgung("Погашение, %", "1.0"), Rent("Аренда, €/мес", "1010"),
    Management("Управление, €/мес", "40"), Tax("Налог, %", "42"),
}

data class CalculatorUiState(
    val inputs: Map<Field, String> = Field.entries.associateWith { it.default },
    val result: InvestmentResult? = null,
)

class CalculatorViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val args = savedStateHandle.toRoute<CalculatorDestination>()
    private val _state = MutableStateFlow(
        CalculatorUiState(
            inputs = Field.entries.associateWith { it.default } +
                    listOfNotNull(
                        args.price?.let { Field.Price to "%.0f".format(it) },
                        args.rent?.let { Field.Rent to "%.0f".format(it) },
                    )
        )
    )
    val state = _state.asStateFlow()

    init {
        recalc()
    }

    fun onChange(field: Field, value: String) {
        _state.update { it.copy(inputs = it.inputs + (field to value)) }
        recalc()
    }

    private fun recalc() = _state.update { s ->
        fun v(f: Field) = s.inputs[f]?.replace(',', '.')?.toDoubleOrNull()
        val values = Field.entries.map { v(it) }
        if (values.any { it == null } || v(Field.Price)!! <= 0) return@update s.copy(result = null)
        s.copy(
            result = InvestmentCalculator.calculate(
                InvestmentParams(
                    purchasePrice = v (Field.Price)!!,
                cashAvailable = v(Field.Cash)!!,
                dealExpenses = v(Field.Expenses)!!,
                interestRate = v(Field.Interest)!!,
                tilgung = v(Field.Tilgung)!!,
                monthlyColdRent = v(Field.Rent)!!,
                managementCosts = v(Field.Management)!!,
                taxRate = v(Field.Tax)!! / 100,
                buildingShare = 0.8,
                afaRate = 0.025,
            )
        ))
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { CalculatorViewModel(createSavedStateHandle()) }
        }
    }
}