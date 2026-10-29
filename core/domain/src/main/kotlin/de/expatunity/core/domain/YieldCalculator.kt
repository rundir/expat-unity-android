package de.expatunity.core.domain

data class YieldInput(
    val price: Double,          // цена объекта, €
    val monthlyRent: Double,    // холодная аренда, €/мес
    val monthlyCosts: Double,   // нерасп. Hausgeld и пр., €/мес
    val purchaseCostsPct: Double = 0.10, // Grunderwerbsteuer+нотариус+маклер
)

data class YieldResult(
    val grossYield: Double,
    val netYield: Double,
    val annualNetIncome: Double,
)

object YieldCalculator {
    fun calculate(input: YieldInput): YieldResult {
        require(input.price > 0) { "price must be > 0" }
        val annualRent = input.monthlyRent * 12
        val annualNet = annualRent - input.monthlyCosts * 12
        val totalInvest = input.price * (1 + input.purchaseCostsPct)
        return YieldResult(
            grossYield = annualRent / input.price,
            netYield = annualNet / totalInvest,
            annualNetIncome = annualNet,
        )
    }
}