package de.expatunity.calculator

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import de.expatunity.core.domain.InvestmentCalculator
import de.expatunity.core.domain.InvestmentParams
import de.expatunity.core.domain.InvestmentResult
import de.expatunity.feature.calculator.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class CalculatorDestination(val price: Double? = null, val rent: Double? = null)

enum class Field(@StringRes val labelRes: Int, val default: String) {
    Price(R.string.field_price, "318000"),
    Cash(R.string.field_cash, "70000"),
    Expenses(R.string.field_expenses, "0"),
    Interest(R.string.field_interest, "4.3"),
    Tilgung(R.string.field_tilgung, "1.0"),
    Rent(R.string.field_rent, "1010"),
    Management(R.string.field_management, "40"),
    Tax(R.string.field_tax, "42"),
}

data class CalculatorUiState(
    val inputs: Map<Field, String> = Field.entries.associateWith { it.default },
    val result: InvestmentResult? = null,
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : ViewModel() {

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
}