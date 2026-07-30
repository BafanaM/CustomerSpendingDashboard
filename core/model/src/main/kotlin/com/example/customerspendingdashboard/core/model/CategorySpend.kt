package com.example.customerspendingdashboard.core.model

import com.example.customerspendingdashboard.core.common.Money

/** Total spend within a single [category] for a given period, plus its share of the total. */
data class CategorySpend(
    val category: Category,
    val total: Money,
    val percentage: Float,
    val transactionCount: Int,
)
