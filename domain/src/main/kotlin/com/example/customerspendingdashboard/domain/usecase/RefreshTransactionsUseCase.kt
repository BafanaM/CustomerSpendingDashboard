package com.example.customerspendingdashboard.domain.usecase

import com.example.customerspendingdashboard.core.common.DataResult
import com.example.customerspendingdashboard.domain.repository.TransactionRepository
import javax.inject.Inject

/** Triggers a one-shot pull of fresh transactions from the remote source into local storage. */
class RefreshTransactionsUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        // Delegates to the repository's remote-refresh-then-persist operation.
        suspend operator fun invoke(): DataResult<Unit> = repository.refresh()
    }
