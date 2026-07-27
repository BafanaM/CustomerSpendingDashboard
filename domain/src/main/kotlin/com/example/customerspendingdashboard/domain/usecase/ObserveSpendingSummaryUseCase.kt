package com.example.customerspendingdashboard.domain.usecase

import com.example.customerspendingdashboard.core.common.Money
import com.example.customerspendingdashboard.core.common.TimeRange
import com.example.customerspendingdashboard.core.common.sum
import com.example.customerspendingdashboard.core.model.SpendingSummary
import com.example.customerspendingdashboard.core.model.Transaction
import com.example.customerspendingdashboard.core.model.TransactionFilter
import com.example.customerspendingdashboard.core.model.TransactionType
import com.example.customerspendingdashboard.core.model.TrendPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import javax.inject.Inject

private const val ISO_MONDAY = 1

/** Aggregates the filtered transaction stream into totals plus a chronological spend trend. */
class ObserveSpendingSummaryUseCase
    @Inject
    constructor(
        private val observeTransactions: ObserveTransactionsUseCase,
    ) {
        // Emits the spending summary for the given time range, recomputed on every change.
        operator fun invoke(range: TimeRange): Flow<SpendingSummary> =
            observeTransactions(TransactionFilter(range = range)).map { transactions ->
                transactions.toSpendingSummary(range)
            }

        // Totals spend/income and builds the chronological trend for a transaction list.
        private fun List<Transaction>.toSpendingSummary(range: TimeRange): SpendingSummary {
            val currencyCode = firstOrNull()?.amount?.currencyCode ?: Money.DEFAULT_CURRENCY_CODE
            val totalSpend = filter { it.type == TransactionType.DEBIT }.map { it.amount }.sum(currencyCode)
            val totalIncome = filter { it.type == TransactionType.CREDIT }.map { it.amount }.sum(currencyCode)
            return SpendingSummary(
                totalSpend = totalSpend,
                totalIncome = totalIncome,
                netChange = totalIncome - totalSpend,
                transactionCount = size,
                trend = computeTrend(range, currencyCode),
                periodLabel = range.displayLabel,
            )
        }

        // Buckets debits by day (week/month ranges) or by month (year/all ranges) into trend points.
        private fun List<Transaction>.computeTrend(
            range: TimeRange,
            currencyCode: String,
        ): List<TrendPoint> =
            filter { it.type == TransactionType.DEBIT }
                .groupBy { it.date.bucketStart(range) }
                .toSortedMap()
                .map { (bucketStart, transactions) ->
                    TrendPoint(
                        label = bucketStart.label(range),
                        amount = transactions.map { it.amount }.sum(currencyCode),
                    )
                }

        // Rounds a date down to the start of its trend bucket (Monday of the week, or 1st of the month).
        private fun LocalDate.bucketStart(range: TimeRange): LocalDate =
            if (range == TimeRange.YEAR || range == TimeRange.ALL) {
                LocalDate(year, month, 1)
            } else {
                minus(dayOfWeek.isoDayNumber - ISO_MONDAY, kotlinx.datetime.DateTimeUnit.DAY)
            }

        // Formats a bucket start date as a chart axis label ("Jul" for month buckets, "1 Jul" for day buckets).
        private fun LocalDate.label(range: TimeRange): String {
            val shortMonth =
                month.name
                    .take(MONTH_LABEL_LENGTH)
                    .lowercase()
                    .replaceFirstChar { it.uppercase() }
            return if (range == TimeRange.YEAR || range == TimeRange.ALL) {
                shortMonth
            } else {
                "$dayOfMonth $shortMonth"
            }
        }

        private companion object {
            const val MONTH_LABEL_LENGTH = 3
        }
    }
