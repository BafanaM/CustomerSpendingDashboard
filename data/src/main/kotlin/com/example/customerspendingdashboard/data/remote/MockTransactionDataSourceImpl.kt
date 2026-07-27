package com.example.customerspendingdashboard.data.remote

import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.core.model.TransactionType
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import javax.inject.Inject
import kotlin.random.Random

private const val SIMULATED_NETWORK_DELAY_MS = 500L
private const val SEED = 20260723L
private const val TOTAL_TRANSACTION_COUNT = 57
private const val MONTHS_OF_HISTORY = 9
private const val INCOME_TRANSACTION_COUNT = MONTHS_OF_HISTORY
private const val SPEND_TRANSACTION_COUNT = TOTAL_TRANSACTION_COUNT - INCOME_TRANSACTION_COUNT
private const val PAYDAY = 25
private const val MAX_DAY_OF_MONTH = 28
private const val SALARY_MINOR_UNITS = 2_500_000L
private const val SALARY_VARIANCE_MINOR_UNITS = 50_000L

/**
 * Stands in for a real backend: generates exactly [TOTAL_TRANSACTION_COUNT] deterministic
 * (seeded), realistic ZAR transactions spread across the last [MONTHS_OF_HISTORY] fully-elapsed
 * months, with a simulated network delay — rather than hand-authoring a fixture file of rows.
 */
class MockTransactionDataSourceImpl
    @Inject
    constructor() : RemoteTransactionDataSource {
        // Simulates a network round-trip, then returns the deterministically-generated dataset.
        override suspend fun fetchTransactions(): List<TransactionDto> {
            delay(SIMULATED_NETWORK_DELAY_MS)
            return generateTransactions()
        }

        // Builds exactly TOTAL_TRANSACTION_COUNT transactions: one salary credit per elapsed month
        // plus randomized spend transactions distributed across those same months.
        private fun generateTransactions(): List<TransactionDto> {
            val random = Random(SEED)
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val transactions = mutableListOf<TransactionDto>()

            // monthsAgo runs 1..MONTHS_OF_HISTORY (never the current, still-in-progress month),
            // so every generated date is guaranteed to already be in the past — no "date <= today"
            // filtering is needed, which is what keeps the total exactly TOTAL_TRANSACTION_COUNT.
            for (monthsAgo in 1..MONTHS_OF_HISTORY) {
                val monthAnchor = today.minus(monthsAgo, DateTimeUnit.MONTH)
                val salaryDate = LocalDate(monthAnchor.year, monthAnchor.month, PAYDAY)
                transactions += salaryTransaction(salaryDate, random)
            }

            repeat(SPEND_TRANSACTION_COUNT) { index ->
                val monthsAgo = 1 + (index % MONTHS_OF_HISTORY)
                val monthAnchor = today.minus(monthsAgo, DateTimeUnit.MONTH)
                val date = LocalDate(monthAnchor.year, monthAnchor.month, random.nextInt(1, MAX_DAY_OF_MONTH))
                transactions += spendTransaction(date, random, index)
            }

            return transactions
        }

        // Builds a single randomized-amount salary credit for the given payday.
        private fun salaryTransaction(
            date: LocalDate,
            random: Random,
        ): TransactionDto {
            val variance = random.nextLong(-SALARY_VARIANCE_MINOR_UNITS, SALARY_VARIANCE_MINOR_UNITS)
            return TransactionDto(
                id = "txn-salary-$date",
                merchant = "Employer Inc",
                category = Category.INCOME.name,
                amountMinorUnits = SALARY_MINOR_UNITS + variance,
                currencyCode = "ZAR",
                date = date.toString(),
                type = TransactionType.CREDIT.name,
                note = null,
            )
        }

        // Builds a single randomized debit transaction for a random category and merchant.
        private fun spendTransaction(
            date: LocalDate,
            random: Random,
            index: Int,
        ): TransactionDto {
            val category = MERCHANTS_BY_CATEGORY.keys.toList()[random.nextInt(MERCHANTS_BY_CATEGORY.size)]
            val merchant = MERCHANTS_BY_CATEGORY.getValue(category).let { it[random.nextInt(it.size)] }
            return TransactionDto(
                id = "txn-$date-$index",
                merchant = merchant,
                category = category.name,
                amountMinorUnits = category.randomAmount(random),
                currencyCode = "ZAR",
                date = date.toString(),
                type = TransactionType.DEBIT.name,
                note = null,
            )
        }

        private companion object {
            val MERCHANTS_BY_CATEGORY =
                mapOf(
                    Category.GROCERIES to listOf("Woolworths", "Checkers", "Pick n Pay", "Spar"),
                    Category.TRANSPORT to listOf("Uber", "Bolt", "Shell", "Gautrain"),
                    Category.DINING to listOf("Mugg & Bean", "Nando's", "KFC", "Ocean Basket"),
                    Category.SHOPPING to listOf("Takealot", "Mr Price", "Woolworths", "Amazon"),
                    Category.BILLS_UTILITIES to listOf("Eskom", "Vodacom", "City of Joburg", "DSTV"),
                    Category.ENTERTAINMENT to listOf("Ster-Kinekor", "Netflix", "Showmax", "Spotify"),
                    Category.HEALTH to listOf("Clicks", "Dis-Chem", "Netcare", "Life Healthcare"),
                    Category.TRANSFERS to listOf("EFT Transfer", "Family Support", "Rent Payment"),
                )

            // Realistic ZAR minor-unit ranges per category for the mock generator — a data table,
            // not arithmetic, so named constants per bound would only restate the category name.
            // Picks a realistic random amount within this category's typical spend range.
            @Suppress("MagicNumber")
            fun Category.randomAmount(random: Random): Long =
                when (this) {
                    Category.GROCERIES -> random.nextLong(8_000, 55_000)
                    Category.TRANSPORT -> random.nextLong(2_500, 35_000)
                    Category.DINING -> random.nextLong(6_000, 45_000)
                    Category.SHOPPING -> random.nextLong(15_000, 200_000)
                    Category.BILLS_UTILITIES -> random.nextLong(30_000, 180_000)
                    Category.ENTERTAINMENT -> random.nextLong(5_000, 25_000)
                    Category.HEALTH -> random.nextLong(10_000, 90_000)
                    Category.TRANSFERS -> random.nextLong(50_000, 500_000)
                    Category.INCOME, Category.OTHER -> random.nextLong(5_000, 50_000)
                }
        }
    }
