package com.example.customerspendingdashboard.core.model

import com.example.customerspendingdashboard.core.common.TimeRange

data class TransactionFilter(
    val query: String = "",
    val category: Category? = null,
    val range: TimeRange = TimeRange.MONTH,
)
