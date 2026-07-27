package com.example.customerspendingdashboard.domain.usecase

import com.example.customerspendingdashboard.core.model.Transaction
import com.example.customerspendingdashboard.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTransactionUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        // Delegates to the repository's single-transaction stream by id.
        operator fun invoke(id: String): Flow<Transaction?> = repository.observeTransaction(id)
    }
