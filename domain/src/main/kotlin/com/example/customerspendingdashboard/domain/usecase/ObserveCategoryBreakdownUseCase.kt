package com.example.customerspendingdashboard.domain.usecase

import com.example.customerspendingdashboard.core.common.TimeRange
import com.example.customerspendingdashboard.core.common.sum
import com.example.customerspendingdashboard.core.model.CategorySpend
import com.example.customerspendingdashboard.core.model.Transaction
import com.example.customerspendingdashboard.core.model.TransactionFilter
import com.example.customerspendingdashboard.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Groups debit transactions by category, largest spend first. */
class ObserveCategoryBreakdownUseCase
    @Inject
    constructor(
        private val observeTransactions: ObserveTransactionsUseCase,
    ) {
        // Emits the category breakdown for the given time range, recomputed on every change.
        operator fun invoke(range: TimeRange): Flow<List<CategorySpend>> =
            observeTransactions(TransactionFilter(range = range)).map { transactions -> transactions.toBreakdown() }

        // Groups debits by category into per-category totals, percentages, and counts.
        private fun List<Transaction>.toBreakdown(): List<CategorySpend> {
            val debits = filter { it.type == TransactionType.DEBIT }
            if (debits.isEmpty()) return emptyList()

            val currencyCode = debits.first().amount.currencyCode
            val total = debits.map { it.amount }.sum(currencyCode)

            return debits
                .groupBy { it.category }
                .map { (category, transactions) ->
                    val categoryTotal = transactions.map { it.amount }.sum(currencyCode)
                    CategorySpend(
                        category = category,
                        total = categoryTotal,
                        percentage =
                            if (total.minorUnits == 0L) {
                                0f
                            } else {
                                categoryTotal.minorUnits.toFloat() / total.minorUnits
                            },
                        transactionCount = transactions.size,
                    )
                }.sortedByDescending { it.total.minorUnits }
        }
    }
