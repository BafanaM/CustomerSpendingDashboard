package com.example.customerspendingdashboard.core.model

import com.example.customerspendingdashboard.core.common.TimeRange

/** The combined search/category/date-range filter applied to the transaction list. */
data class TransactionFilter(
    val query: String = "",
    val category: Category? = null,
    val range: TimeRange = TimeRange.MONTH,
)
