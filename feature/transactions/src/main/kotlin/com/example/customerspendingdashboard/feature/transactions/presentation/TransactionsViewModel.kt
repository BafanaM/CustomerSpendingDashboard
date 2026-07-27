package com.example.customerspendingdashboard.feature.transactions.presentation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customerspendingdashboard.core.common.DataResult
import com.example.customerspendingdashboard.core.common.TimeRange
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.core.model.TransactionFilter
import com.example.customerspendingdashboard.domain.usecase.ObserveTransactionsUseCase
import com.example.customerspendingdashboard.domain.usecase.RefreshTransactionsUseCase
import com.example.customerspendingdashboard.feature.transactions.R
import com.example.customerspendingdashboard.feature.transactions.navigation.CATEGORY_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
private const val SEARCH_DEBOUNCE_MS = 300L

/**
 * Backs the Transactions screen. Combines a debounced search query, category filter, and
 * [TimeRange] into a single filtered [TransactionsUiState] stream, and triggers a remote refresh
 * on first load and on demand. The initial category, if any, is seeded once from the nav-arg
 * category a cross-feature deep link (tapping a category on Overview) may have supplied.
 *
 * Errors from [refresh] are non-blocking: a failed refresh surfaces [TransactionsUiState.error]
 * as a banner over whatever was last successfully loaded from Room, rather than replacing the
 * screen with an error state — there's always something on screen once data has loaded once.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class TransactionsViewModel
    @Inject
    constructor(
        private val observeTransactions: ObserveTransactionsUseCase,
        private val refreshTransactions: RefreshTransactionsUseCase,
        savedStateHandle: SavedStateHandle,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val initialCategory: Category? =
            savedStateHandle
                .get<String>(CATEGORY_ARG)
                ?.let { name -> runCatching { Category.valueOf(name) }.getOrNull() }

        private val query = MutableStateFlow("")
        private val selectedCategory = MutableStateFlow(initialCategory)
        private val selectedRange = MutableStateFlow(TimeRange.ALL)
        private val isLoading = MutableStateFlow(true)
        private val error = MutableStateFlow<String?>(null)

        private val liveFilter = combine(query, selectedCategory, selectedRange, ::TransactionFilter)

        private val transactions =
            combine(
                query.debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
                selectedCategory,
                selectedRange,
                ::TransactionFilter,
            ).flatMapLatest(observeTransactions::invoke)

        val uiState: StateFlow<TransactionsUiState> =
            combine(
                liveFilter,
                transactions,
                isLoading,
                error,
            ) { filter, transactions, loading, err ->
                TransactionsUiState(
                    isLoading = loading,
                    query = filter.query,
                    selectedCategory = filter.category,
                    selectedRange = filter.range,
                    transactions = transactions,
                    error = err,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), TransactionsUiState())

        init {
            refresh()
        }

        // Updates the search query; the transaction stream debounces before re-filtering.
        fun onQueryChanged(newQuery: String) {
            query.value = newQuery
        }

        // Updates the selected category filter.
        fun onCategorySelected(category: Category?) {
            selectedCategory.value = category
        }

        // Updates the selected time range filter.
        fun onRangeSelected(range: TimeRange) {
            selectedRange.value = range
        }

        // Re-runs the remote refresh, e.g. after the user taps retry.
        fun onRefresh() {
            refresh()
        }

        // Pulls fresh transactions and surfaces any failure as a non-blocking stale-data error.
        private fun refresh() {
            viewModelScope.launch {
                when (refreshTransactions()) {
                    is DataResult.Success -> error.value = null
                    is DataResult.Error ->
                        error.value = context.getString(R.string.transactions_refresh_error)
                }
                isLoading.value = false
            }
        }
    }
