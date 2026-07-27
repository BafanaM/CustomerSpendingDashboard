package com.example.customerspendingdashboard.domain.usecase

import app.cash.turbine.test
import com.example.customerspendingdashboard.core.common.TimeRange
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.core.model.TransactionType
import com.example.customerspendingdashboard.domain.testutil.FakeTransactionRepository
import com.example.customerspendingdashboard.domain.testutil.transaction
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObserveCategoryBreakdownUseCaseTest {
    private val today = LocalDate(2026, 7, 23)
    private val repository = FakeTransactionRepository()
    private val observeTransactions = ObserveTransactionsUseCase(repository, today = { today })
    private val useCase = ObserveCategoryBreakdownUseCase(observeTransactions)

    // Categories are grouped and ordered from highest to lowest spend.
    @Test
    fun `groups spend by category and sorts by total descending`() =
        runTest {
            repository.setTransactions(
                listOf(
                    transaction(id = "g1", category = Category.GROCERIES, amountMinorUnits = 3_000, date = today),
                    transaction(id = "d1", category = Category.DINING, amountMinorUnits = 9_000, date = today),
                    transaction(id = "g2", category = Category.GROCERIES, amountMinorUnits = 2_000, date = today),
                ),
            )

            useCase(TimeRange.ALL).test {
                val breakdown = awaitItem()
                assertEquals(listOf(Category.DINING, Category.GROCERIES), breakdown.map { it.category })
                assertEquals(2, breakdown.first { it.category == Category.GROCERIES }.transactionCount)
            }
        }

    // Income (credit) transactions never appear in the spend breakdown.
    @Test
    fun `excludes credit transactions from the breakdown`() =
        runTest {
            repository.setTransactions(
                listOf(
                    transaction(id = "salary", category = Category.INCOME, type = TransactionType.CREDIT, date = today),
                    transaction(id = "rent", category = Category.BILLS_UTILITIES, date = today),
                ),
            )

            useCase(TimeRange.ALL).test {
                assertEquals(listOf(Category.BILLS_UTILITIES), awaitItem().map { it.category })
            }
        }

    // Per-category percentages add up to (approximately) 100% of total spend.
    @Test
    fun `percentages sum to roughly one across categories`() =
        runTest {
            repository.setTransactions(
                listOf(
                    transaction(id = "g1", category = Category.GROCERIES, amountMinorUnits = 6_000, date = today),
                    transaction(id = "d1", category = Category.DINING, amountMinorUnits = 4_000, date = today),
                ),
            )

            useCase(TimeRange.ALL).test {
                val breakdown = awaitItem()
                assertEquals(1f, breakdown.sumOf { it.percentage.toDouble() }.toFloat(), 0.001f)
            }
        }
}
