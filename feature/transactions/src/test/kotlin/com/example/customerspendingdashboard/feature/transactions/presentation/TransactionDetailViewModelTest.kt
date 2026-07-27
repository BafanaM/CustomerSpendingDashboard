package com.example.customerspendingdashboard.feature.transactions.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.customerspendingdashboard.core.testing.MainDispatcherExtension
import com.example.customerspendingdashboard.core.testing.transaction
import com.example.customerspendingdashboard.domain.usecase.ObserveTransactionUseCase
import com.example.customerspendingdashboard.feature.transactions.navigation.TRANSACTION_ID_ARG
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TransactionDetailViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcherExtension = MainDispatcherExtension()
    }

    private val observeTransaction = mockk<ObserveTransactionUseCase>()

    // The transaction id read from SavedStateHandle resolves to the matching transaction.
    @Test
    fun `emits the matching transaction for the nav-arg id`() =
        runTest {
            val expected = transaction(id = "t1", merchant = "Uber")
            every { observeTransaction("t1") } returns flowOf(expected)
            val savedStateHandle = SavedStateHandle(mapOf(TRANSACTION_ID_ARG to "t1"))

            val viewModel = TransactionDetailViewModel(savedStateHandle, observeTransaction)

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(false, state.isLoading)
                assertEquals("Uber", state.transaction?.merchant)
            }
        }

    // An unknown transaction id resolves to a null transaction, not an exception.
    @Test
    fun `emits a null transaction when nothing matches`() =
        runTest {
            every { observeTransaction("missing") } returns flowOf(null)
            val savedStateHandle = SavedStateHandle(mapOf(TRANSACTION_ID_ARG to "missing"))

            val viewModel = TransactionDetailViewModel(savedStateHandle, observeTransaction)

            viewModel.uiState.test {
                assertNull(awaitItem().transaction)
            }
        }
}
