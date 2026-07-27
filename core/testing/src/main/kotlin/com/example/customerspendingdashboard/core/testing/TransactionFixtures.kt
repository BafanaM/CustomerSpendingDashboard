package com.example.customerspendingdashboard.core.testing

import com.example.customerspendingdashboard.core.common.Money
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.core.model.Transaction
import com.example.customerspendingdashboard.core.model.TransactionType
import kotlinx.datetime.LocalDate

// Builds a [Transaction] fixture with sensible defaults, overridable per-field for test cases.
fun transaction(
    id: String = "t1",
    merchant: String = "Checkers",
    category: Category = Category.GROCERIES,
    amountMinorUnits: Long = 15_000,
    currencyCode: String = "ZAR",
    date: LocalDate = LocalDate(2026, 7, 1),
    type: TransactionType = TransactionType.DEBIT,
    note: String? = null,
) = Transaction(
    id = id,
    merchant = merchant,
    category = category,
    amount = Money(amountMinorUnits, currencyCode),
    date = date,
    type = type,
    note = note,
)
