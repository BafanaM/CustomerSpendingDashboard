package com.example.customerspendingdashboard.data.repository

import app.cash.turbine.test
import com.example.customerspendingdashboard.core.common.DataResult
import com.example.customerspendingdashboard.core.database.dao.TransactionDao
import com.example.customerspendingdashboard.core.database.entity.TransactionEntity
import com.example.customerspendingdashboard.core.testing.FakeDispatcherProvider
import com.example.customerspendingdashboard.data.remote.RemoteTransactionDataSource
import com.example.customerspendingdashboard.data.remote.TransactionDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TransactionRepositoryImplTest {
    private val dao = mockk<TransactionDao>()
    private val remoteDataSource = mockk<RemoteTransactionDataSource>()
    private val repository = TransactionRepositoryImpl(dao, remoteDataSource, FakeDispatcherProvider())

    // DAO rows are mapped into domain Transaction models.
    @Test
    fun `observeTransactions maps entities to domain models`() =
        runTest {
            every { dao.observeAll() } returns flowOf(listOf(entity(id = "1")))

            repository.observeTransactions().test {
                assertEquals("1", awaitItem().single().id)
                awaitComplete()
            }
        }

    // A missing DAO row maps to a null domain result, not an exception.
    @Test
    fun `observeTransaction maps a null row to null`() =
        runTest {
            every { dao.observeById("missing") } returns flowOf(null)

            repository.observeTransaction("missing").test {
                assertEquals(null, awaitItem())
                awaitComplete()
            }
        }

    // A successful refresh pulls remote data and writes it into the DAO.
    @Test
    fun `refresh fetches from remote and upserts into the dao`() =
        runTest {
            coEvery { remoteDataSource.fetchTransactions() } returns listOf(dto(id = "1"))
            coEvery { dao.upsertAll(any()) } returns Unit

            val result = repository.refresh()

            assertTrue(result is DataResult.Success)
            coVerify { dao.upsertAll(match { it.single().id == "1" }) }
        }

    // A remote fetch failure is caught and surfaced as a DataResult.Error, not thrown.
    @Test
    fun `refresh surfaces a remote failure as an Error result`() =
        runTest {
            coEvery { remoteDataSource.fetchTransactions() } throws IllegalStateException("network down")

            val result = repository.refresh()

            assertTrue(result is DataResult.Error)
        }

    // Builds a fixture entity row with sensible defaults.
    private fun entity(id: String) =
        TransactionEntity(
            id = id,
            merchant = "Checkers",
            category = "GROCERIES",
            amountMinorUnits = 15_000,
            currencyCode = "ZAR",
            date = "2026-07-01",
            type = "DEBIT",
            note = null,
        )

    // Builds a fixture DTO with sensible defaults.
    private fun dto(id: String) =
        TransactionDto(
            id = id,
            merchant = "Checkers",
            category = "GROCERIES",
            amountMinorUnits = 15_000,
            currencyCode = "ZAR",
            date = "2026-07-01",
            type = "DEBIT",
            note = null,
        )
}
