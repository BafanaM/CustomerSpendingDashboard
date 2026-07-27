package com.example.customerspendingdashboard.feature.dashboard.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.customerspendingdashboard.core.model.Category

// Stateful entry point: collects the ViewModel's UI state and renders the stateless content.
@Composable
fun DashboardScreen(
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(
        state = uiState,
        onRangeSelected = viewModel::onRangeSelected,
        onRetry = viewModel::onRefresh,
        onCategoryClick = onCategoryClick,
        modifier = modifier,
    )
}
