package com.example.customerspendingdashboard.feature.transactions.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.customerspendingdashboard.core.common.TimeRange
import com.example.customerspendingdashboard.core.designsystem.component.EmptyState
import com.example.customerspendingdashboard.core.designsystem.component.ErrorState
import com.example.customerspendingdashboard.core.designsystem.component.FilterChipRow
import com.example.customerspendingdashboard.core.designsystem.component.LoadingState
import com.example.customerspendingdashboard.core.designsystem.theme.SpendingTheme
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.feature.transactions.presentation.component.TransactionRow

private const val PHASE_TRANSITION_MS = 350
private const val COLLAPSED_TRANSACTION_COUNT = 5

private enum class TransactionsPhase { LOADING, ERROR, LOADED }

// Crossfades between loading/error/loaded phases based on UI state.
@Composable
fun TransactionsContent(
    state: TransactionsUiState,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onRangeSelected: (TimeRange) -> Unit,
    onRetry: () -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase =
        when {
            state.isLoading -> TransactionsPhase.LOADING
            state.error != null && state.transactions.isEmpty() -> TransactionsPhase.ERROR
            else -> TransactionsPhase.LOADED
        }

    Crossfade(
        targetState = phase,
        modifier = modifier.fillMaxSize(),
        animationSpec = tween(PHASE_TRANSITION_MS),
        label = "transactions-phase",
    ) { targetPhase ->
        when (targetPhase) {
            TransactionsPhase.LOADING -> LoadingState(modifier = Modifier.fillMaxSize())
            TransactionsPhase.ERROR ->
                ErrorState(
                    message = state.error.orEmpty(),
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                )
            TransactionsPhase.LOADED ->
                TransactionsLoaded(
                    state = state,
                    onQueryChanged = onQueryChanged,
                    onCategorySelected = onCategorySelected,
                    onRangeSelected = onRangeSelected,
                    onTransactionClick = onTransactionClick,
                )
        }
    }
}

// Search field, range/category filters, and the (initially collapsed) transaction list.
@Composable
private fun TransactionsLoaded(
    state: TransactionsUiState,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onRangeSelected: (TimeRange) -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryOptions: List<Category?> = listOf(null) + Category.entries
    var showAll by remember(state.query, state.selectedCategory, state.selectedRange) { mutableStateOf(false) }
    val visibleTransactions = if (showAll) state.transactions else state.transactions.take(COLLAPSED_TRANSACTION_COUNT)
    val hiddenCount = state.transactions.size - visibleTransactions.size

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search transactions") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
            )
        }
        item {
            FilterChipRow(
                options = TimeRange.entries,
                selected = state.selectedRange,
                onSelect = onRangeSelected,
                label = { it.displayLabel },
            )
        }
        item {
            FilterChipRow(
                options = categoryOptions,
                selected = state.selectedCategory,
                onSelect = onCategorySelected,
                label = { it?.displayName ?: "All" },
            )
        }
        if (state.transactions.isEmpty()) {
            item { EmptyState(message = "No transactions match your filters.") }
        } else {
            items(visibleTransactions, key = { it.id }) { transaction ->
                TransactionRow(transaction = transaction, onClick = { onTransactionClick(transaction.id) })
            }
            if (hiddenCount > 0) {
                item {
                    OutlinedButton(onClick = { showAll = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Show $hiddenCount more")
                    }
                }
            }
        }
    }
}

// Preview of TransactionsContent in its loaded state.
@Preview(showBackground = true)
@Composable
private fun TransactionsContentPreview() {
    SpendingTheme {
        TransactionsContent(
            state = TransactionsUiState(isLoading = false),
            onQueryChanged = {},
            onCategorySelected = {},
            onRangeSelected = {},
            onRetry = {},
            onTransactionClick = {},
        )
    }
}
