package com.example.customerspendingdashboard.feature.dashboard.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.customerspendingdashboard.core.designsystem.component.StatCard
import com.example.customerspendingdashboard.core.model.SpendingSummary

// Equal-height Spent/Income stat cards side by side.
@Composable
fun SpendSummaryRow(
    summary: SpendingSummary,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            label = "Spent",
            value = summary.totalSpend.format(),
            supportingText = summary.periodLabel,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        StatCard(
            label = "Income",
            value = summary.totalIncome.format(),
            supportingText = summary.periodLabel,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}
