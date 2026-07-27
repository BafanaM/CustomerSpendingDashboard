package com.example.customerspendingdashboard.feature.dashboard.presentation

import android.content.Context
import app.cash.turbine.test
import com.example.customerspendingdashboard.core.common.DataResult
import com.example.customerspendingdashboard.core.common.Money
import com.example.customerspendingdashboard.core.common.TimeRange
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.core.model.CategorySpend
import com.example.customerspendingdashboard.core.model.SpendingSummary
import com.example.customerspendingdashboard.core.testing.MainDispatcherExtension
import com.example.customerspendingdashboard.domain.usecase.ObserveCategoryBreakdownUseCase
import com.example.customerspendingdashboard.domain.usecase.ObserveSpendingSummaryUseCase
import com.example.customerspendingdashboard.domain.usecase.RefreshTransactionsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class DashboardViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcherExtension = MainDispatcherExtension()
    }

    private val summary =
        SpendingSummary(
            totalSpend = Money(50_000, "ZAR"),
            totalIncome = Money(200_000, "ZAR"),
            netChange = Money(150_000, "ZAR"),
            transactionCount = 5,
            trend = emptyList(),
            periodLabel = TimeRange.MONTH.displayLabel,
        )
    private val breakdown = listOf(CategorySpend(Category.GROCERIES, Money(50_000, "ZAR"), 1f, 5))

    private val observeSpendingSummary = mockk<ObserveSpendingSummaryUseCase>()
    private val observeCategoryBreakdown = mockk<ObserveCategoryBreakdownUseCase>()
    private val refreshTransactions = mockk<RefreshTransactionsUseCase>()
    private val context = mockk<Context>()

    // Builds a DashboardViewModel with the mocked use cases stubbed to sensible defaults.
    private fun viewModel(): DashboardViewModel {
        every { observeSpendingSummary(any()) } returns flowOf(summary)
        every { observeCategoryBreakdown(any()) } returns flowOf(breakdown)
        every { context.getString(any()) } returns "Couldn't refresh your transactions. Showing the latest saved data."
        return DashboardViewModel(observeSpendingSummary, observeCategoryBreakdown, refreshTransactions, context)
    }

    // A successful refresh yields the mocked summary and breakdown with no error.
    @Test
    fun `loads summary and breakdown after a successful refresh`() =
        runTest {
            coEvery { refreshTransactions() } returns DataResult.Success(Unit)

            viewModel().uiState.test {
                val state = awaitItem()
                assertFalse(state.isLoading)
                assertEquals(summary, state.summary)
                assertEquals(breakdown, state.categoryBreakdown)
                assertEquals(null, state.error)
            }
        }

    // A failed refresh still stops loading and sets a non-null error message.
    @Test
    fun `surfaces an error message when refresh fails`() =
        runTest {
            coEvery { refreshTransactions() } returns DataResult.Error(IllegalStateException("offline"))

            viewModel().uiState.test {
                val state = awaitItem()
                assertFalse(state.isLoading)
                assertNotNull(state.error)
            }
        }

    // Calling onRangeSelected updates the emitted state's selected range.
    @Test
    fun `onRangeSelected updates the selected range`() =
        runTest {
            coEvery { refreshTransactions() } returns DataResult.Success(Unit)
            val vm = viewModel()

            vm.uiState.test {
                awaitItem()
                vm.onRangeSelected(TimeRange.YEAR)
                assertEquals(TimeRange.YEAR, awaitItem().selectedRange)
            }
        }
}
