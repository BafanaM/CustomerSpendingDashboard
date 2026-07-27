package com.example.customerspendingdashboard.domain.usecase

import app.cash.turbine.test
import com.example.customerspendingdashboard.domain.testutil.FakeTransactionRepository
import com.example.customerspendingdashboard.domain.testutil.transaction
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ObserveTransactionUseCaseTest {
    private val repository = FakeTransactionRepository()
    private val useCase = ObserveTransactionUseCase(repository)

    // Looking up an existing id returns that transaction.
    @Test
    fun `emits the matching transaction`() =
        runTest {
            repository.setTransactions(listOf(transaction(id = "t1"), transaction(id = "t2")))

            useCase("t2").test {
                assertEquals("t2", awaitItem()?.id)
            }
        }

    // Looking up an unknown id returns null instead of throwing.
    @Test
    fun `emits null when no transaction matches the id`() =
        runTest {
            repository.setTransactions(listOf(transaction(id = "t1")))

            useCase("missing").test {
                assertNull(awaitItem())
            }
        }
}
