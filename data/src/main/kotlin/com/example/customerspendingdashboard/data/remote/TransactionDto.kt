package com.example.customerspendingdashboard.data.remote

/**
 * Shape of a transaction as it would arrive from a real API: primitive/string fields only, no
 * domain types. Deliberately mirrors [com.example.customerspendingdashboard.core.database.entity.TransactionEntity]
 * 1:1 for now — if a real backend's JSON diverges from that shape later, only this class and its
 * mapper change.
 */
data class TransactionDto(
    val id: String,
    val merchant: String,
    val category: String,
    val amountMinorUnits: Long,
    val currencyCode: String,
    val date: String,
    val type: String,
    val note: String?,
)
