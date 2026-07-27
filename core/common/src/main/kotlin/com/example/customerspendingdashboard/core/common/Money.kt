package com.example.customerspendingdashboard.core.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * An amount of money stored as minor units (cents) to avoid floating point drift, paired with
 * an ISO-4217 currency code. Formatting is locale-aware but currency-driven, so a ZAR amount
 * always renders with the "R" symbol regardless of device locale.
 */
data class Money(
    val minorUnits: Long,
    val currencyCode: String = DEFAULT_CURRENCY_CODE,
) : Comparable<Money> {
    // Adds two amounts of the same currency.
    operator fun plus(other: Money): Money {
        require(currencyCode == other.currencyCode) { "Cannot add mismatched currencies" }
        return copy(minorUnits = minorUnits + other.minorUnits)
    }

    // Subtracts two amounts of the same currency.
    operator fun minus(other: Money): Money {
        require(currencyCode == other.currencyCode) { "Cannot subtract mismatched currencies" }
        return copy(minorUnits = minorUnits - other.minorUnits)
    }

    // Orders amounts of the same currency by minor units.
    override fun compareTo(other: Money): Int {
        require(currencyCode == other.currencyCode) { "Cannot compare mismatched currencies" }
        return minorUnits.compareTo(other.minorUnits)
    }

    // Renders this amount as a locale-formatted currency string (e.g. "R 1 234,56").
    fun format(locale: Locale = Locale.getDefault()): String {
        val currency = Currency.getInstance(currencyCode)
        val format = NumberFormat.getCurrencyInstance(locale).apply { setCurrency(currency) }
        val major =
            BigDecimal(minorUnits)
                .movePointLeft(currency.defaultFractionDigits)
                .setScale(currency.defaultFractionDigits, RoundingMode.HALF_EVEN)
        return format.format(major)
    }

    companion object {
        const val DEFAULT_CURRENCY_CODE = "ZAR"

        // Builds a zero-value amount in the given currency.
        fun zero(currencyCode: String = DEFAULT_CURRENCY_CODE) = Money(0, currencyCode)
    }
}

// Sums a collection of amounts, defaulting to zero if the collection is empty.
fun Iterable<Money>.sum(currencyCode: String = Money.DEFAULT_CURRENCY_CODE): Money =
    fold(Money.zero(currencyCode)) { acc, money -> acc + money }
