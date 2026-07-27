package com.example.customerspendingdashboard.data.di

import com.example.customerspendingdashboard.data.remote.MockTransactionDataSourceImpl
import com.example.customerspendingdashboard.data.remote.RemoteTransactionDataSource
import com.example.customerspendingdashboard.data.repository.TransactionRepositoryImpl
import com.example.customerspendingdashboard.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    // Binds the Room-backed repository implementation to the domain interface.
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    // Binds the mock generator as the remote data source seam.
    @Binds
    @Singleton
    abstract fun bindRemoteTransactionDataSource(impl: MockTransactionDataSourceImpl): RemoteTransactionDataSource
}
