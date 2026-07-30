package com.example.customerspendingdashboard.feature.dashboard.presentation

import com.example.customerspendingdashboard.core.common.TimeRange
import com.example.customerspendingdashboard.core.model.CategorySpend
import com.example.customerspendingdashboard.core.model.SpendingSummary

/** Single immutable UI state for the Overview screen, collected from [DashboardViewModel]. */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val selectedRange: TimeRange = TimeRange.MONTH,
    val summary: SpendingSummary = SpendingSummary.empty(periodLabel = TimeRange.MONTH.displayLabel),
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val error: String? = null,
)
