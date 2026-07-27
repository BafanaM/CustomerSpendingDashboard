package com.example.customerspendingdashboard.core.model

import com.example.customerspendingdashboard.core.common.Money

/** One bucket in a spend-over-time series, e.g. a week or a month. */
data class TrendPoint(
    val label: String,
    val amount: Money,
)
