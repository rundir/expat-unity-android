package de.expatunity.core.domain

data class InvestmentParams(
    val purchasePrice: Double,
    val cashAvailable: Double,
    val dealExpenses: Double,
    val interestRate: Double,      // 4.3 = 4.3 %
    val tilgung: Double,           // 1.0 = 1 %
    val monthlyColdRent: Double,
    val managementCosts: Double,   // €/мес
    val taxRate: Double,           // 0.42
    val buildingShare: Double,     // 0.8
    val afaRate: Double,           // 0.025
    val rentGrowthRate: Double = 0.0,
    val appreciationRate: Double = 0.02,
    val forcedMonthlyAnnuity: Double? = null,
)

data class YearProjection(
    val year: Int, val remainingLoanBalance: Double, val yearlyInterest: Double,
    val yearlyPrincipal: Double, val yearlyAnnuity: Double, val yearlyGrossRent: Double,
    val yearlyAfa: Double, val taxableIncome: Double, val taxRefund: Double,
    val netCashflow: Double, val propertyValue: Double, val cumulativeCashflow: Double,
)

data class InvestmentResult(
    val loanAmount: Double, val monthlyAnnuity: Double, val yearlyAnnuity: Double,
    val yearlyAfa: Double, val totalNetCashflow: Double, val equityGained: Double,
    val totalAppreciation: Double, val finalPropertyValue: Double,
    val finalRemainingLoanBalance: Double, val totalReturn: Double,
    val yearlyBreakdown: List<YearProjection>,
)

object InvestmentCalculator {
    private const val HORIZON_YEARS = 10

    fun calculate(p: InvestmentParams): InvestmentResult {
        val downPayment = maxOf(0.0, p.cashAvailable - p.dealExpenses)
        val loanAmount = p.purchasePrice - downPayment
        val monthlyRate = p.interestRate / 100 / 12

        val monthlyAnnuity = p.forcedMonthlyAnnuity?.takeIf { it > 0 }
            ?: if (loanAmount > 0) loanAmount * (p.interestRate + p.tilgung) / 100 / 12 else 0.0
        val yearlyAnnuity = monthlyAnnuity * 12
        val yearlyAfa = p.purchasePrice * p.buildingShare * p.afaRate
        val yearlyManagement = p.managementCosts * 12

        var balance = loanAmount
        var cumulative = 0.0
        val breakdown = (1..HORIZON_YEARS).map { year ->
            val grossRent = p.monthlyColdRent * 12 * Math.pow(1 + p.rentGrowthRate, (year - 1).toDouble())
            var interest = 0.0
            var principal = 0.0
            repeat(12) {
                if (balance <= 0) return@repeat
                val i = balance * monthlyRate
                val pr = minOf(monthlyAnnuity - i, balance)
                interest += i; principal += pr; balance -= pr
            }
            val taxable = grossRent - interest - yearlyManagement - yearlyAfa
            val refund = if (taxable < 0) -taxable * p.taxRate else 0.0
            val net = grossRent - yearlyAnnuity - yearlyManagement + refund
            cumulative += net
            YearProjection(
                year, balance.r2(), interest.r2(), principal.r2(), yearlyAnnuity.r2(),
                grossRent.r2(), yearlyAfa.r2(), taxable.r2(), refund.r2(), net.r2(),
                (p.purchasePrice * Math.pow(1 + p.appreciationRate, year.toDouble())).r2(),
                cumulative.r2(),
            )
        }

        val finalValue = p.purchasePrice * Math.pow(1 + p.appreciationRate, HORIZON_YEARS.toDouble())
        val equity = loanAmount - balance
        val appreciation = finalValue - p.purchasePrice
        return InvestmentResult(
            loanAmount.r2(), monthlyAnnuity.r2(), yearlyAnnuity.r2(), yearlyAfa.r2(),
            cumulative.r2(), equity.r2(), appreciation.r2(), finalValue.r2(),
            balance.r2(), (cumulative + equity + appreciation).r2(), breakdown,
        )
    }

    private fun Double.r2(): Double =
        if (isFinite()) Math.round(this * 100) / 100.0 else 0.0
}