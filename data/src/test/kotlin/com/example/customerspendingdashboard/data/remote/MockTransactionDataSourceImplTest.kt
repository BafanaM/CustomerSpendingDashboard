package com.example.customerspendingdashboard.data.remote

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Verifies the mock generator's determinism, count, date-safety, and id-uniqueness invariants. */
class MockTransactionDataSourceImplTest {
    private val dataSource = MockTransactionDataSourceImpl()

    // The seeded generator produces the identical dataset on every call.
    @Test
    fun `generation is deterministic across calls`() =
        runTest {
            val first = dataSource.fetchTransactions()
            val second = dataSource.fetchTransactions()

            assertEquals(first, second)
        }

    // Every generated date falls in an already-elapsed month, never in the future.
    @Test
    fun `never generates a transaction dated after today`() =
        runTest {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            val transactions = dataSource.fetchTransactions()

            assertTrue(transactions.all { LocalDate.parse(it.date) <= today })
        }

    // The dataset contains both salary credits and spend debits.
    @Test
    fun `generates both income and spend transactions`() =
        runTest {
            val transactions = dataSource.fetchTransactions()

            assertFalse(transactions.isEmpty())
            assertTrue(transactions.any { it.type == "CREDIT" })
            assertTrue(transactions.any { it.type == "DEBIT" })
        }

    // No two generated transactions share an id.
    @Test
    fun `every transaction id is unique`() =
        runTest {
            val transactions = dataSource.fetchTransactions()

            assertEquals(transactions.size, transactions.map { it.id }.toSet().size)
        }

    // The generator always yields exactly the target transaction count.
    @Test
    fun `generates exactly 57 transactions`() =
        runTest {
            val transactions = dataSource.fetchTransactions()

            assertEquals(57, transactions.size)
        }
}
