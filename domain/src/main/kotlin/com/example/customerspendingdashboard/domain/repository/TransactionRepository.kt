package com.example.customerspendingdashboard.domain.repository

import com.example.customerspendingdashboard.core.common.DataResult
import com.example.customerspendingdashboard.core.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for transactions. Reads are unfiltered/continuous (local DB backed);
 * filtering and aggregation are business rules that live in the use cases, not here.
 */
interface TransactionRepository {
    // Continuous stream of all locally-stored transactions, unfiltered.
    fun observeTransactions(): Flow<List<Transaction>>

    // Continuous stream of a single transaction by id, or null if it doesn't exist.
    fun observeTransaction(id: String): Flow<Transaction?>

    /** Pulls fresh data from the remote source and persists it locally. */
    suspend fun refresh(): DataResult<Unit>
}
