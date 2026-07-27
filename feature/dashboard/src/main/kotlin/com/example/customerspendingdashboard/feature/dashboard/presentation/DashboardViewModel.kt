package com.example.customerspendingdashboard.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customerspendingdashboard.core.common.DataResult
import com.example.customerspendingdashboard.core.common.TimeRange
import com.example.customerspendingdashboard.domain.usecase.ObserveCategoryBreakdownUseCase
import com.example.customerspendingdashboard.domain.usecase.ObserveSpendingSummaryUseCase
import com.example.customerspendingdashboard.domain.usecase.RefreshTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SUBSCRIPTION_TIMEOUT_MS = 5_000L

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel
    @Inject
    constructor(
        private val observeSpendingSummary: ObserveSpendingSummaryUseCase,
        private val observeCategoryBreakdown: ObserveCategoryBreakdownUseCase,
        private val refreshTransactions: RefreshTransactionsUseCase,
    ) : ViewModel() {
        private val selectedRange = MutableStateFlow(TimeRange.MONTH)
        private val isLoading = MutableStateFlow(true)
        private val error = MutableStateFlow<String?>(null)

        val uiState: StateFlow<DashboardUiState> =
            combine(
                selectedRange.flatMapLatest(observeSpendingSummary::invoke),
                selectedRange.flatMapLatest(observeCategoryBreakdown::invoke),
                selectedRange,
                isLoading,
                error,
            ) { summary, breakdown, range, loading, err ->
                DashboardUiState(
                    isLoading = loading,
                    selectedRange = range,
                    summary = summary,
                    categoryBreakdown = breakdown,
                    error = err,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), DashboardUiState())

        init {
            refresh()
        }

        // Updates the selected time range, which re-triggers the summary/breakdown flows.
        fun onRangeSelected(range: TimeRange) {
            selectedRange.value = range
        }

        // Re-runs the remote refresh, e.g. after the user taps retry.
        fun onRefresh() {
            refresh()
        }

        // Pulls fresh transactions and surfaces any failure as a non-blocking stale-data error.
        private fun refresh() {
            viewModelScope.launch {
                when (refreshTransactions()) {
                    is DataResult.Success -> error.value = null
                    is DataResult.Error ->
                        error.value =
                            "Couldn't refresh your transactions. Showing the latest saved data."
                }
                isLoading.value = false
            }
        }
    }
