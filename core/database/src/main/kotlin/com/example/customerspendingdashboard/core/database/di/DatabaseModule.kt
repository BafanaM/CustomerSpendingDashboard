package com.example.customerspendingdashboard.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.customerspendingdashboard.core.database.SpendingDatabase
import com.example.customerspendingdashboard.core.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt bindings for the Room database and its DAO. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // Builds the single app-wide Room database instance.
    @Provides
    @Singleton
    fun provideSpendingDatabase(
        @ApplicationContext context: Context,
    ): SpendingDatabase =
        Room
            .databaseBuilder(context, SpendingDatabase::class.java, SpendingDatabase.DATABASE_NAME)
            .build()

    // Exposes the transaction DAO from the shared database instance.
    @Provides
    fun provideTransactionDao(database: SpendingDatabase): TransactionDao = database.transactionDao()
}
