package com.example.customerspendingdashboard.domain.usecase

import com.example.customerspendingdashboard.core.common.DataResult
import com.example.customerspendingdashboard.domain.repository.TransactionRepository
import javax.inject.Inject

class RefreshTransactionsUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        // Delegates to the repository's remote-refresh-then-persist operation.
        suspend operator fun invoke(): DataResult<Unit> = repository.refresh()
    }
