package de.expatunity.core.domain
import kotlin.test.Test
import kotlin.test.assertEquals

class YieldCalculatorTest {
    @Test
    fun grossYield_isRentTimes12DividedByPrice() {
        val r = YieldCalculator.calculate(YieldInput(300_000.0, 1_000.0, 200.0))
        assertEquals(0.04, r.grossYield, 1e-9)
    }
}