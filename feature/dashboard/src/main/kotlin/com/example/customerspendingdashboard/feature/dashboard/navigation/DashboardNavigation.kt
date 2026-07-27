package com.example.customerspendingdashboard.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.feature.dashboard.presentation.DashboardScreen

const val DASHBOARD_ROUTE = "dashboard"

// Registers the dashboard destination in the nav graph.
fun NavGraphBuilder.dashboardScreen(onCategoryClick: (Category) -> Unit) {
    composable(DASHBOARD_ROUTE) {
        DashboardScreen(onCategoryClick = onCategoryClick)
    }
}
