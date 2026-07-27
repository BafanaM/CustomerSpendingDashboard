package com.example.customerspendingdashboard.feature.transactions.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

// Stateful entry point: collects the ViewModel's UI state and renders the stateless content.
@Composable
fun TransactionsScreen(
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    TransactionsContent(
        state = uiState,
        onQueryChanged = viewModel::onQueryChanged,
        onCategorySelected = viewModel::onCategorySelected,
        onRangeSelected = viewModel::onRangeSelected,
        onRetry = viewModel::onRefresh,
        onTransactionClick = onTransactionClick,
        modifier = modifier,
    )
}
