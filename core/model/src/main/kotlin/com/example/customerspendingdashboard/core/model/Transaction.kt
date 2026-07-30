package com.example.customerspendingdashboard.core.model

import com.example.customerspendingdashboard.core.common.Money
import kotlinx.datetime.LocalDate

/** A single debit or credit — the core domain model everything else is derived from. */
data class Transaction(
    val id: String,
    val merchant: String,
    val category: Category,
    val amount: Money,
    val date: LocalDate,
    val type: TransactionType,
    val note: String? = null,
)
