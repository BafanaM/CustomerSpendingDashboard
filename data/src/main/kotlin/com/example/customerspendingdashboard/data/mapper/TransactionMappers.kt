package com.example.customerspendingdashboard.data.mapper

import com.example.customerspendingdashboard.core.common.Money
import com.example.customerspendingdashboard.core.database.entity.TransactionEntity
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.core.model.Transaction
import com.example.customerspendingdashboard.core.model.TransactionType
import com.example.customerspendingdashboard.data.remote.TransactionDto
import kotlinx.datetime.LocalDate

// Maps a persisted Room entity to its domain model.
fun TransactionEntity.toDomain(): Transaction =
    Transaction(
        id = id,
        merchant = merchant,
        category = Category.valueOf(category),
        amount = Money(amountMinorUnits, currencyCode),
        date = LocalDate.parse(date),
        type = TransactionType.valueOf(type),
        note = note,
    )

// Maps a remote DTO to the Room entity shape for persistence.
fun TransactionDto.toEntity(): TransactionEntity =
    TransactionEntity(
        id = id,
        merchant = merchant,
        category = category,
        amountMinorUnits = amountMinorUnits,
        currencyCode = currencyCode,
        date = date,
        type = type,
        note = note,
    )
