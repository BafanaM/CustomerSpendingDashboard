package com.example.customerspendingdashboard.core.model

import com.example.customerspendingdashboard.core.common.Money

data class CategorySpend(
    val category: Category,
    val total: Money,
    val percentage: Float,
    val transactionCount: Int,
)
