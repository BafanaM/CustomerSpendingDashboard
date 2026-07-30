package com.example.customerspendingdashboard.feature.transactions.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customerspendingdashboard.domain.usecase.ObserveTransactionUseCase
import com.example.customerspendingdashboard.feature.transactions.navigation.TRANSACTION_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val SUBSCRIPTION_TIMEOUT_MS = 5_000L

/** Backs the transaction detail screen — streams the single transaction named by the nav-arg id. */
@HiltViewModel
class TransactionDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        observeTransaction: ObserveTransactionUseCase,
    ) : ViewModel() {
        private val transactionId: String = checkNotNull(savedStateHandle[TRANSACTION_ID_ARG])

        val uiState: StateFlow<TransactionDetailUiState> =
            observeTransaction(transactionId)
                .map { transaction -> TransactionDetailUiState(isLoading = false, transaction = transaction) }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                    TransactionDetailUiState(),
                )
    }
