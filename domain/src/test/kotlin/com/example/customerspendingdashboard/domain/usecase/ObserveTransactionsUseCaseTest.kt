package com.example.customerspendingdashboard.domain.usecase

import app.cash.turbine.test
import com.example.customerspendingdashboard.core.common.TimeRange
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.core.model.TransactionFilter
import com.example.customerspendingdashboard.domain.testutil.FakeTransactionRepository
import com.example.customerspendingdashboard.domain.testutil.transaction
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Verifies [ObserveTransactionsUseCase]'s range/category/search filtering. */
class ObserveTransactionsUseCaseTest {
    private val today = LocalDate(2026, 7, 23)
    private val repository = FakeTransactionRepository()
    private val useCase = ObserveTransactionsUseCase(repository, today = { today })

    // Transactions predating the selected time range are excluded.
    @Test
    fun `filters out transactions older than the selected range`() =
        runTest {
            repository.setTransactions(
                listOf(
                    transaction(id = "recent", date = today.minus(2, DateTimeUnit.DAY)),
                    transaction(id = "old", date = today.minus(60, DateTimeUnit.DAY)),
                ),
            )

            useCase(TransactionFilter(range = TimeRange.WEEK)).test {
                assertEquals(listOf("recent"), awaitItem().map { it.id })
            }
        }

    // Only transactions matching the selected category are returned.
    @Test
    fun `filters by category`() =
        runTest {
            repository.setTransactions(
                listOf(
                    transaction(id = "groceries", category = Category.GROCERIES),
                    transaction(id = "dining", category = Category.DINING),
                ),
            )

            useCase(TransactionFilter(category = Category.DINING, range = TimeRange.ALL)).test {
                assertEquals(listOf("dining"), awaitItem().map { it.id })
            }
        }

    // Search query matches merchant or note text regardless of case.
    @Test
    fun `filters by case-insensitive merchant or note search`() =
        runTest {
            repository.setTransactions(
                listOf(
                    transaction(id = "woolworths", merchant = "Woolworths"),
                    transaction(id = "uber", merchant = "Uber", note = "Airport trip"),
                    transaction(id = "unrelated", merchant = "Takealot"),
                ),
            )

            useCase(TransactionFilter(query = "port", range = TimeRange.ALL)).test {
                assertEquals(listOf("uber"), awaitItem().map { it.id })
            }
        }

    // The ALL range applies no date cutoff, however old the transaction.
    @Test
    fun `ALL range returns every transaction regardless of age`() =
        runTest {
            repository.setTransactions(listOf(transaction(id = "ancient", date = LocalDate(2020, 1, 1))))

            useCase(TransactionFilter(range = TimeRange.ALL)).test {
                assertEquals(listOf("ancient"), awaitItem().map { it.id })
            }
        }
}
