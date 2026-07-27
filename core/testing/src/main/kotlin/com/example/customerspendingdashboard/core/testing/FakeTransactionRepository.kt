package com.example.customerspendingdashboard.core.testing

import com.example.customerspendingdashboard.core.common.DataResult
import com.example.customerspendingdashboard.core.model.Transaction
import com.example.customerspendingdashboard.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [TransactionRepository] fake for data/feature module tests. */
class FakeTransactionRepository(
    initial: List<Transaction> = emptyList(),
) : TransactionRepository {
    private val transactionsFlow = MutableStateFlow(initial)

    var refreshResult: DataResult<Unit> = DataResult.Success(Unit)
    var refreshCallCount: Int = 0
        private set

    // Emits the current in-memory transaction list.
    override fun observeTransactions(): Flow<List<Transaction>> = transactionsFlow

    // Emits the transaction with the given id, or null if it's not in the current list.
    override fun observeTransaction(id: String): Flow<Transaction?> =
        transactionsFlow.map { list -> list.find { it.id == id } }

    // Records the call and returns the pre-configured [refreshResult].
    override suspend fun refresh(): DataResult<Unit> {
        refreshCallCount++
        return refreshResult
    }

    // Test hook to replace the in-memory transaction list.
    fun setTransactions(list: List<Transaction>) {
        transactionsFlow.value = list
    }
}
