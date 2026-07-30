package com.example.customerspendingdashboard.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.customerspendingdashboard.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/** Room's read/write surface over the `transactions` table. */
@Dao
interface TransactionDao {
    // Streams every stored transaction, newest first.
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    // Streams a single transaction by id, or null if it doesn't exist.
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeById(id: String): Flow<TransactionEntity?>

    // Inserts new rows and overwrites existing ones with matching ids.
    @Upsert
    suspend fun upsertAll(entities: List<TransactionEntity>)

    // Deletes every stored transaction.
    @Query("DELETE FROM transactions")
    suspend fun clearAll()

    // Returns how many transactions are currently stored.
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int
}
