package com.example.customerspendingdashboard.core.model

import com.example.customerspendingdashboard.core.common.Money

data class SpendingSummary(
    val totalSpend: Money,
    val totalIncome: Money,
    val netChange: Money,
    val transactionCount: Int,
    val trend: List<TrendPoint>,
    val periodLabel: String,
) {
    companion object {
        // Builds an all-zero summary for a period with no transactions yet.
        fun empty(
            currencyCode: String = Money.DEFAULT_CURRENCY_CODE,
            periodLabel: String = "",
        ) = SpendingSummary(
            totalSpend = Money.zero(currencyCode),
            totalIncome = Money.zero(currencyCode),
            netChange = Money.zero(currencyCode),
            transactionCount = 0,
            trend = emptyList(),
            periodLabel = periodLabel,
        )
    }
}
