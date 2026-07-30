package com.example.customerspendingdashboard.domain.usecase

import com.example.customerspendingdashboard.core.common.DataResult
import com.example.customerspendingdashboard.domain.testutil.FakeTransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Verifies [RefreshTransactionsUseCase] passes repository results straight through. */
class RefreshTransactionsUseCaseTest {
    private val repository = FakeTransactionRepository()
    private val useCase = RefreshTransactionsUseCase(repository)

    // A successful repository refresh is passed straight through.
    @Test
    fun `propagates a successful refresh`() =
        runTest {
            repository.refreshResult = DataResult.Success(Unit)

            val result = useCase()

            assertTrue(result is DataResult.Success)
        }

    // A failed repository refresh is also passed straight through.
    @Test
    fun `propagates a failed refresh`() =
        runTest {
            repository.refreshResult = DataResult.Error(IllegalStateException("network down"))

            val result = useCase()

            assertTrue(result is DataResult.Error)
        }
}
