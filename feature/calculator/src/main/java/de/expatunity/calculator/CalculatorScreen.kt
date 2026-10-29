package de.expatunity.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.expatunity.core.domain.InvestmentResult
import de.expatunity.core.domain.YearProjection

@Composable
fun CalculatorRoute(
    vm: CalculatorViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    CalculatorScreen(state, vm::onChange, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    onChange: (Field, String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Прогноз на 10 лет") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
        )
    }) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { Text("Прогноз на 10 лет", style = MaterialTheme.typography.headlineSmall) }
            items(Field.entries) { f ->
                OutlinedTextField(
                    value = state.inputs.getValue(f),
                    onValueChange = { onChange(f, it) },
                    label = { Text(f.label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
            state.result?.let { r ->
                item { SummaryCard(r) }
                item { Text("По годам", style = MaterialTheme.typography.titleMedium) }
                items(r.yearlyBreakdown, key = { it.year }) { YearRow(it) }
            }
        }
    }
}

@Composable
private fun SummaryCard(r: InvestmentResult) = Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Line("Кредит", r.loanAmount)
        Line("Платёж в месяц", r.monthlyAnnuity)
        Line("Cashflow за 10 лет", r.totalNetCashflow)
        Line("Погашено тела", r.equityGained)
        Line("Рост стоимости", r.totalAppreciation)
        HorizontalDivider()
        Line("Итого доходность", r.totalReturn, bold = true)
    }
}

@Composable
private fun YearRow(y: YearProjection) = Row(Modifier.fillMaxWidth()) {
    Text("${y.year}", Modifier.width(32.dp))
    Text("Возврат ${y.taxRefund.eur()}", Modifier.weight(1f))
    Text(y.netCashflow.eur(),
        color = if (y.netCashflow < 0) MaterialTheme.colorScheme.error else Color.Unspecified)
}

@Composable
private fun Line(label: String, value: Double, bold: Boolean = false) = Row(Modifier.fillMaxWidth()) {
    Text(label, Modifier.weight(1f))
    Text(value.eur(), fontWeight = if (bold) FontWeight.Bold else null)
}

private fun Double.eur() = "%,.0f €".format(this)