package com.example.customerspendingdashboard.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.customerspendingdashboard.core.database.dao.TransactionDao
import com.example.customerspendingdashboard.core.database.entity.TransactionEntity

/** The app's single Room database — offline-first source of truth for transactions. */
@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SpendingDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "spending.db"
    }
}
