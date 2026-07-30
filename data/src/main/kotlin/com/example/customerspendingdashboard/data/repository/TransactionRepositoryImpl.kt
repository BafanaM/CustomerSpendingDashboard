package com.example.customerspendingdashboard.data.repository

import com.example.customerspendingdashboard.core.common.DataResult
import com.example.customerspendingdashboard.core.common.DispatcherProvider
import com.example.customerspendingdashboard.core.common.runCatchingResult
import com.example.customerspendingdashboard.core.database.dao.TransactionDao
import com.example.customerspendingdashboard.core.model.Transaction
import com.example.customerspendingdashboard.data.mapper.toDomain
import com.example.customerspendingdashboard.data.mapper.toEntity
import com.example.customerspendingdashboard.data.remote.RemoteTransactionDataSource
import com.example.customerspendingdashboard.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * [TransactionRepository] implementation: Room is the single source of truth for reads, and
 * [refresh] is the only operation that talks to [RemoteTransactionDataSource].
 */
class TransactionRepositoryImpl
    @Inject
    constructor(
        private val dao: TransactionDao,
        private val remoteDataSource: RemoteTransactionDataSource,
        private val dispatcherProvider: DispatcherProvider,
    ) : TransactionRepository {
        // Streams all locally-persisted transactions, mapped to domain models.
        override fun observeTransactions(): Flow<List<Transaction>> =
            dao
                .observeAll()
                .map { entities -> entities.map { it.toDomain() } }
                .flowOn(dispatcherProvider.default)

        // Streams a single locally-persisted transaction by id, mapped to a domain model.
        override fun observeTransaction(id: String): Flow<Transaction?> =
            dao
                .observeById(id)
                .map { it?.toDomain() }
                .flowOn(dispatcherProvider.default)

        // Pulls fresh transactions from the remote source and upserts them into Room.
        override suspend fun refresh(): DataResult<Unit> =
            withContext(dispatcherProvider.io) {
                runCatchingResult {
                    val remoteTransactions = remoteDataSource.fetchTransactions()
                    dao.upsertAll(remoteTransactions.map { it.toEntity() })
                }
            }
    }
