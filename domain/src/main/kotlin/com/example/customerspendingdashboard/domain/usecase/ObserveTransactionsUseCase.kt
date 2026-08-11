package com.example.customerspendingdashboard.domain.usecase

import com.example.customerspendingdashboard.core.model.Transaction
import com.example.customerspendingdashboard.core.model.TransactionFilter
import com.example.customerspendingdashboard.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * Applies search/category/date-range filtering to the repository's unfiltered transaction stream.
 *
 * The `today` supplier is only a secondary constructor (not the `@Inject`-annotated one) so Dagger
 * only ever has to resolve [TransactionRepository]: Dagger has no notion of Kotlin default
 * parameter values, so a default-valued lambda on the `@Inject` constructor itself would force it
 * to demand a binding for `() -> LocalDate`, which nothing provides. Tests use this constructor
 * directly to inject a fixed date.
 */
class ObserveTransactionsUseCase(
    private val repository: TransactionRepository,
    private val today: () -> LocalDate,
) {
    // Dagger-visible constructor: delegates to the primary one with the real system clock.
    @Inject
    constructor(repository: TransactionRepository) : this(
        repository,
        { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
    )

    // Emits the repository's transaction stream filtered by search query, category, and date
    // range. `today()` is re-read on every emission (not just once per filter change) so a
    // range boundary like "last 7 days" stays correct even if the screen is left open across
    // a day rollover.
    operator fun invoke(filter: TransactionFilter): Flow<List<Transaction>> =
        repository.observeTransactions().map { transactions ->
            val referenceDate = today()
            transactions.filter { it.matches(filter, referenceDate) }
        }

    // Checks whether a transaction satisfies the range/category/search-query filter.
    private fun Transaction.matches(
        filter: TransactionFilter,
        referenceDate: LocalDate,
    ): Boolean {
        val withinRange =
            filter.range.days?.let { days ->
                date >= referenceDate.minus(days, DateTimeUnit.DAY)
            } ?: true
        val matchesCategory = filter.category?.let { it == category } ?: true
        val matchesQuery =
            filter.query.isBlank() ||
                merchant.contains(filter.query, ignoreCase = true) ||
                note?.contains(filter.query, ignoreCase = true) == true
        return withinRange && matchesCategory && matchesQuery
    }
}
