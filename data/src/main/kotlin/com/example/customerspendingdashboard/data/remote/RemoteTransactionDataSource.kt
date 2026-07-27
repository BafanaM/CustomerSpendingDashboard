package com.example.customerspendingdashboard.data.remote

/**
 * Seam standing in for a real backend. Swapping the mock for a Retrofit-backed implementation
 * later means adding one new class and rebinding it in the data module's `RepositoryModule` —
 * nothing above this layer changes.
 */
interface RemoteTransactionDataSource {
    // Fetches the full remote transaction set for a refresh.
    suspend fun fetchTransactions(): List<TransactionDto>
}
