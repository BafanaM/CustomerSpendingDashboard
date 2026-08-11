package com.example.customerspendingdashboard.feature.transactions.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.customerspendingdashboard.core.common.DataResult
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.core.testing.FakeStringProvider
import com.example.customerspendingdashboard.core.testing.MainDispatcherExtension
import com.example.customerspendingdashboard.core.testing.transaction
import com.example.customerspendingdashboard.domain.usecase.ObserveTransactionsUseCase
import com.example.customerspendingdashboard.domain.usecase.RefreshTransactionsUseCase
import com.example.customerspendingdashboard.feature.transactions.navigation.CATEGORY_ARG
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/** Verifies [TransactionsViewModel]'s filtering, debounce, and nav-arg seeding behavior. */
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcherExtension = MainDispatcherExtension()
    }

    private val allTransactions = listOf(transaction(id = "1"), transaction(id = "2"))

    private val observeTransactions = mockk<ObserveTransactionsUseCase>()
    private val refreshTransactions = mockk<RefreshTransactionsUseCase>()
    private val stringProvider = FakeStringProvider()

    // Builds a TransactionsViewModel with the mocked use cases stubbed to sensible defaults.
    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): TransactionsViewModel {
        every { observeTransactions(any()) } returns flowOf(allTransactions)
        coEvery { refreshTransactions() } returns DataResult.Success(Unit)
        return TransactionsViewModel(observeTransactions, refreshTransactions, savedStateHandle, stringProvider)
    }

    // A successful refresh yields the mocked transaction list with no error.
    @Test
    fun `loads transactions after a successful refresh`() =
        runTest {
            viewModel().uiState.test {
                val state = awaitItem()
                assertFalse(state.isLoading)
                assertEquals(allTransactions, state.transactions)
            }
        }

    // Calling onCategorySelected updates the emitted state without any debounce delay.
    @Test
    fun `onCategorySelected updates the selected category immediately`() =
        runTest {
            val vm = viewModel()

            vm.uiState.test {
                awaitItem()
                vm.onCategorySelected(Category.DINING)
                assertEquals(Category.DINING, awaitItem().selectedCategory)
            }
        }

    // A category passed via SavedStateHandle seeds the initial selected category.
    @Test
    fun `initializes the selected category from the nav-arg when present`() =
        runTest {
            val savedStateHandle = SavedStateHandle(mapOf(CATEGORY_ARG to Category.HEALTH.name))

            viewModel(savedStateHandle).uiState.test {
                assertEquals(Category.HEALTH, awaitItem().selectedCategory)
            }
        }

    // The query text updates immediately in state, even though the re-filtered results are debounced.
    @Test
    fun `onQueryChanged reflects the typed query immediately without waiting for the debounce`() =
        runTest {
            val vm = viewModel()

            vm.uiState.test {
                awaitItem()
                vm.onQueryChanged("coffee")
                val state = awaitItem()
                assertEquals("coffee", state.query)
                // The debounced re-query hasn't fired yet, so the transaction list is still the old one.
                assertEquals(allTransactions, state.transactions)
            }
        }
}
