package com.example.customerspendingdashboard.feature.transactions.presentation

import com.example.customerspendingdashboard.core.model.Transaction

data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val transaction: Transaction? = null,
)
