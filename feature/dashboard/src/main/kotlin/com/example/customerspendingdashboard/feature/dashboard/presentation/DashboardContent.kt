package com.example.customerspendingdashboard.feature.dashboard.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.customerspendingdashboard.core.common.Money
import com.example.customerspendingdashboard.core.common.TimeRange
import com.example.customerspendingdashboard.core.designsystem.component.ErrorState
import com.example.customerspendingdashboard.core.designsystem.component.FilterChipRow
import com.example.customerspendingdashboard.core.designsystem.component.LoadingState
import com.example.customerspendingdashboard.core.designsystem.component.SectionHeader
import com.example.customerspendingdashboard.core.designsystem.theme.SpendingTheme
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.core.model.CategorySpend
import com.example.customerspendingdashboard.core.model.SpendingSummary
import com.example.customerspendingdashboard.core.model.TrendPoint
import com.example.customerspendingdashboard.feature.dashboard.presentation.component.CategoryBreakdownChart
import com.example.customerspendingdashboard.feature.dashboard.presentation.component.SpendSummaryRow
import com.example.customerspendingdashboard.feature.dashboard.presentation.component.SpendTrendChart

private const val PHASE_TRANSITION_MS = 350
private const val RANGE_TRANSITION_MS = 300

private enum class DashboardPhase { LOADING, ERROR, LOADED }

// Crossfades between loading/error/loaded phases based on UI state.
@Composable
fun DashboardContent(
    state: DashboardUiState,
    onRangeSelected: (TimeRange) -> Unit,
    onRetry: () -> Unit,
    onCategoryClick: (Category) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val phase =
        when {
            state.isLoading -> DashboardPhase.LOADING
            state.error != null && state.summary.transactionCount == 0 -> DashboardPhase.ERROR
            else -> DashboardPhase.LOADED
        }

    Crossfade(
        targetState = phase,
        modifier = modifier.fillMaxSize(),
        animationSpec = tween(PHASE_TRANSITION_MS),
        label = "dashboard-phase",
    ) { targetPhase ->
        when (targetPhase) {
            DashboardPhase.LOADING -> LoadingState(modifier = Modifier.fillMaxSize())
            DashboardPhase.ERROR ->
                ErrorState(
                    message = state.error.orEmpty(),
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                )
            DashboardPhase.LOADED ->
                DashboardLoaded(
                    state = state,
                    onRangeSelected = onRangeSelected,
                    onCategoryClick = onCategoryClick,
                )
        }
    }
}

// Scrollable loaded-state layout: range filter, optional stale-data banner, and range-dependent content.
@Composable
private fun DashboardLoaded(
    state: DashboardUiState,
    onRangeSelected: (TimeRange) -> Unit,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            FilterChipRow(
                options = TimeRange.entries,
                selected = state.selectedRange,
                onSelect = onRangeSelected,
                label = { it.displayLabel },
            )
        }
        if (state.error != null) {
            item { StaleDataBanner(state.error) }
        }
        item {
            AnimatedContent(
                targetState = state.selectedRange,
                transitionSpec = {
                    (fadeIn(tween(RANGE_TRANSITION_MS))) togetherWith (fadeOut(tween(RANGE_TRANSITION_MS)))
                },
                label = "dashboard-range",
            ) {
                RangeDependentContent(state = state, onCategoryClick = onCategoryClick)
            }
        }
    }
}

// Spend summary, trend chart, and category breakdown for the currently selected range.
@Composable
private fun RangeDependentContent(
    state: DashboardUiState,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SpendSummaryRow(summary = state.summary)
        if (state.summary.trend.isNotEmpty()) {
            Column {
                SectionHeader(title = "Spending Trend")
                SpendTrendChart(trend = state.summary.trend, modifier = Modifier.padding(top = 8.dp))
            }
        }
        if (state.categoryBreakdown.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "Top Categories")
                    CategoryBreakdownChart(
                        breakdown = state.categoryBreakdown,
                        onCategoryClick = onCategoryClick,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
    }
}

// Non-blocking banner shown when a refresh failed but cached data is still displayed.
@Composable
private fun StaleDataBanner(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

// Preview of DashboardContent in its loaded state with sample summary and category data.
@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun DashboardContentPreview() {
    val summary =
        SpendingSummary(
            totalSpend = Money(423_550, "ZAR"),
            totalIncome = Money(2_500_000, "ZAR"),
            netChange = Money(2_076_450, "ZAR"),
            transactionCount = 42,
            trend =
                listOf(
                    TrendPoint("1 Jul", Money(15_000, "ZAR")),
                    TrendPoint("8 Jul", Money(22_000, "ZAR")),
                    TrendPoint("15 Jul", Money(9_000, "ZAR")),
                ),
            periodLabel = TimeRange.MONTH.displayLabel,
        )
    SpendingTheme {
        DashboardContent(
            state =
                DashboardUiState(
                    isLoading = false,
                    summary = summary,
                    categoryBreakdown =
                        listOf(
                            CategorySpend(Category.GROCERIES, Money(150_000, "ZAR"), 0.4f, 12),
                            CategorySpend(Category.DINING, Money(90_000, "ZAR"), 0.25f, 8),
                        ),
                ),
            onRangeSelected = {},
            onRetry = {},
        )
    }
}
