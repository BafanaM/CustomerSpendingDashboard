package com.example.customerspendingdashboard.feature.transactions.presentation

import com.example.customerspendingdashboard.core.model.Transaction

/** Single immutable UI state for the transaction detail screen. */
data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val transaction: Transaction? = null,
)
