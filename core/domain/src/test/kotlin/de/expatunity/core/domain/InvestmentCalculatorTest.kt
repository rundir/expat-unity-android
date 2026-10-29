package de.expatunity.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class InvestmentCalculatorTest {
    private val r = InvestmentCalculator.calculate(
        InvestmentParams(
            purchasePrice = 318_000.0, cashAvailable = 70_000.0, dealExpenses = 0.0,
            interestRate = 4.3, tilgung = 1.0, monthlyColdRent = 1_010.0,
            managementCosts = 40.0, taxRate = 0.42, buildingShare = 0.8, afaRate = 0.025,
        )
    )

    @Test
    fun loanAmount() = assertEquals(248_000.0, r.loanAmount)
    @Test fun yearlyAfa() = assertEquals(6_360.0, r.yearlyAfa)
    @Test fun tenYears() = assertEquals(10, r.yearlyBreakdown.size)
    @Test fun yearOneTaxShield() {
        assertEquals(-5_334.53, r.yearlyBreakdown[0].taxableIncome, 0.01)
        assertEquals(2_240.50, r.yearlyBreakdown[0].taxRefund, 0.01)
    }
}