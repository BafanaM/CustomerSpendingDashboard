package com.example.customerspendingdashboard.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.customerspendingdashboard.core.database.SpendingDatabase
import com.example.customerspendingdashboard.core.database.entity.TransactionEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Runs on Robolectric (JUnit4) rather than JUnit5: Robolectric's runner integration is most
 * stable via `@RunWith`, so DB/Android-framework-dependent tests deliberately stay on JUnit4
 * while pure business-logic tests elsewhere in the project use JUnit5.
 */
@RunWith(RobolectricTestRunner::class)
class TransactionDaoTest {
    private lateinit var database: SpendingDatabase
    private lateinit var dao: TransactionDao

    // Builds a fresh in-memory database before each test.
    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SpendingDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = database.transactionDao()
    }

    // Closes the in-memory database after each test.
    @After
    fun tearDown() {
        database.close()
    }

    // Inserted rows come back ordered newest-first.
    @Test
    fun `upsertAll then observeAll emits inserted rows ordered by date desc`() =
        runTest {
            dao.upsertAll(
                listOf(
                    entity(id = "1", date = "2026-01-01"),
                    entity(id = "2", date = "2026-02-01"),
                ),
            )

            dao.observeAll().test {
                val rows = awaitItem()
                assertEquals(listOf("2", "1"), rows.map { it.id })
            }
        }

    // Upserting an existing id overwrites that row instead of duplicating it.
    @Test
    fun `upsertAll replaces rows with matching id`() =
        runTest {
            dao.upsertAll(listOf(entity(id = "1", merchant = "Original")))
            dao.upsertAll(listOf(entity(id = "1", merchant = "Updated")))

            assertEquals(1, dao.count())
            dao.observeAll().test {
                assertEquals("Updated", awaitItem().single().merchant)
            }
        }

    // clearAll empties the table.
    @Test
    fun `clearAll removes every row`() =
        runTest {
            dao.upsertAll(listOf(entity(id = "1"), entity(id = "2")))

            dao.clearAll()

            assertEquals(0, dao.count())
        }

    // replaceAll drops rows missing from the new set, not just adds/updates the ones present.
    @Test
    fun `replaceAll removes rows absent from the new set`() =
        runTest {
            dao.upsertAll(listOf(entity(id = "1"), entity(id = "2")))

            dao.replaceAll(listOf(entity(id = "2", merchant = "Updated"), entity(id = "3")))

            dao.observeAll().test {
                assertEquals(setOf("2", "3"), awaitItem().map { it.id }.toSet())
            }
        }

    // Builds a fixture transaction entity with sensible defaults.
    private fun entity(
        id: String,
        merchant: String = "Checkers",
        date: String = "2026-01-15",
    ) = TransactionEntity(
        id = id,
        merchant = merchant,
        category = "GROCERIES",
        amountMinorUnits = 15_000,
        currencyCode = "ZAR",
        date = date,
        type = "DEBIT",
        note = null,
    )
}
