package com.example.customerspendingdashboard.domain.usecase

import app.cash.turbine.test
import com.example.customerspendingdashboard.core.common.Money
import com.example.customerspendingdashboard.core.common.TimeRange
import com.example.customerspendingdashboard.core.model.TransactionType
import com.example.customerspendingdashboard.domain.testutil.FakeTransactionRepository
import com.example.customerspendingdashboard.domain.testutil.transaction
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObserveSpendingSummaryUseCaseTest {
    private val today = LocalDate(2026, 7, 23)
    private val repository = FakeTransactionRepository()
    private val observeTransactions = ObserveTransactionsUseCase(repository, today = { today })
    private val useCase = ObserveSpendingSummaryUseCase(observeTransactions)

    // Summary totals spend and income separately and derives net change from them.
    @Test
    fun `computes total spend, income and net change`() =
        runTest {
            repository.setTransactions(
                listOf(
                    transaction(id = "d1", amountMinorUnits = 10_000, type = TransactionType.DEBIT, date = today),
                    transaction(id = "d2", amountMinorUnits = 5_000, type = TransactionType.DEBIT, date = today),
                    transaction(id = "c1", amountMinorUnits = 20_000, type = TransactionType.CREDIT, date = today),
                ),
            )

            useCase(TimeRange.ALL).test {
                val summary = awaitItem()
                assertEquals(Money(15_000, "ZAR"), summary.totalSpend)
                assertEquals(Money(20_000, "ZAR"), summary.totalIncome)
                assertEquals(Money(5_000, "ZAR"), summary.netChange)
                assertEquals(3, summary.transactionCount)
            }
        }

    // Transactions in the same ISO week collapse into a single trend point.
    @Test
    fun `buckets debit transactions in the same ISO week into a single trend point`() =
        runTest {
            repository.setTransactions(
                listOf(
                    transaction(id = "mon", amountMinorUnits = 1_000, date = LocalDate(2026, 7, 20)),
                    transaction(id = "wed", amountMinorUnits = 2_000, date = LocalDate(2026, 7, 22)),
                ),
            )

            useCase(TimeRange.ALL).test {
                val summary = awaitItem()
                assertEquals(1, summary.trend.size)
                assertEquals(Money(3_000, "ZAR"), summary.trend.single().amount)
            }
        }

    // No transactions means a zeroed-out summary with an empty trend.
    @Test
    fun `empty transaction list yields an empty summary with no trend`() =
        runTest {
            useCase(TimeRange.MONTH).test {
                val summary = awaitItem()
                assertEquals(Money.zero(), summary.totalSpend)
                assertEquals(emptyList<Any>(), summary.trend)
            }
        }
}
