package com.example.customerspendingdashboard.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room's persisted shape of a transaction. Deliberately Android/Room-only: this module knows
 * nothing about the domain [com.example.customerspendingdashboard.core.model.Transaction] model
 * — mapping is owned by the data module, keeping this module swappable/testable in isolation.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val merchant: String,
    val category: String,
    val amountMinorUnits: Long,
    val currencyCode: String,
    val date: String,
    val type: String,
    val note: String?,
)
