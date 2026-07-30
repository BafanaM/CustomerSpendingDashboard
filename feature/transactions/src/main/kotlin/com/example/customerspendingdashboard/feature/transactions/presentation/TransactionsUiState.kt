package com.example.customerspendingdashboard.feature.transactions.presentation

import com.example.customerspendingdashboard.core.common.TimeRange
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.core.model.Transaction

/** Single immutable UI state for the Transactions screen, collected from [TransactionsViewModel]. */
data class TransactionsUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val selectedCategory: Category? = null,
    val selectedRange: TimeRange = TimeRange.ALL,
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null,
)
