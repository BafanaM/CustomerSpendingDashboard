package com.example.customerspendingdashboard.core.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class MoneyTest {
    // Same-currency addition sums minor units.
    @Test
    fun `plus adds minor units for matching currencies`() {
        val total = Money(1_000, "ZAR") + Money(250, "ZAR")

        assertEquals(Money(1_250, "ZAR"), total)
    }

    // Adding mismatched currencies throws.
    @Test
    fun `plus rejects mismatched currencies`() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(1_000, "ZAR") + Money(250, "USD")
        }
    }

    // Summing an empty collection yields zero.
    @Test
    fun `sum of empty collection is zero`() {
        val total = emptyList<Money>().sum("ZAR")

        assertEquals(Money.zero("ZAR"), total)
    }

    // Comparison orders by minor units.
    @Test
    fun `compareTo orders by minor units`() {
        assertEquals(true, Money(500, "ZAR") < Money(600, "ZAR"))
    }
}
