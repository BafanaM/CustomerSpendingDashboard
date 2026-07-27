package com.example.customerspendingdashboard.domain.testutil

import com.example.customerspendingdashboard.core.common.DataResult
import com.example.customerspendingdashboard.core.model.Transaction
import com.example.customerspendingdashboard.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Kept local to domain's test sourceset (rather than core:testing) to avoid a module cycle:
 * core:testing depends on domain to implement this interface, so domain's own tests can't
 * depend back on core:testing.
 */
class FakeTransactionRepository(
    initial: List<Transaction> = emptyList(),
) : TransactionRepository {
    private val transactions = MutableStateFlow(initial)
    var refreshResult: DataResult<Unit> = DataResult.Success(Unit)

    // Emits the current in-memory transaction list.
    override fun observeTransactions(): Flow<List<Transaction>> = transactions

    // Emits the transaction with the given id, or null if it's not in the current list.
    override fun observeTransaction(id: String): Flow<Transaction?> =
        transactions.map { list -> list.find { it.id == id } }

    // Returns the pre-configured refresh result.
    override suspend fun refresh(): DataResult<Unit> = refreshResult

    // Test hook to replace the in-memory transaction list.
    fun setTransactions(list: List<Transaction>) {
        transactions.value = list
    }
}
