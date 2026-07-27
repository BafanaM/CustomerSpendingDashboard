package com.example.customerspendingdashboard.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.customerspendingdashboard.feature.dashboard.navigation.DASHBOARD_ROUTE
import com.example.customerspendingdashboard.feature.dashboard.navigation.dashboardScreen
import com.example.customerspendingdashboard.feature.transactions.navigation.TRANSACTIONS_BASE_ROUTE
import com.example.customerspendingdashboard.feature.transactions.navigation.TRANSACTIONS_ROUTE
import com.example.customerspendingdashboard.feature.transactions.navigation.TRANSACTION_DETAIL_ROUTE
import com.example.customerspendingdashboard.feature.transactions.navigation.navigateToTransactions
import com.example.customerspendingdashboard.feature.transactions.navigation.transactionsGraph

private val TOP_LEVEL_ROUTES = setOf(DASHBOARD_ROUTE, TRANSACTIONS_ROUTE)
private const val TRANSITION_DURATION_MS = 300

// Composition root: wires the top bar, bottom nav, and both feature nav graphs together.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val currentRoute =
        navController
            .currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route

    Scaffold(
        topBar = {
            Crossfade(targetState = currentRoute == TRANSACTION_DETAIL_ROUTE, label = "top-bar") { isDetail ->
                if (isDetail) {
                    TopAppBar(
                        title = { Text("Transaction details") },
                        navigationIcon = {
                            IconButton(onClick = navController::popBackStack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                    )
                } else {
                    TopAppBar(title = { Text("Spending Dashboard") })
                }
            }
        },
        bottomBar = {
            if (currentRoute in TOP_LEVEL_ROUTES) {
                AppBottomBar(currentRoute = currentRoute, navController = navController)
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = DASHBOARD_ROUTE,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeInAndSlideIn() },
            exitTransition = { fadeOut(tween(TRANSITION_DURATION_MS)) },
            popEnterTransition = { fadeInAndSlideIn() },
            popExitTransition = { fadeOut(tween(TRANSITION_DURATION_MS)) },
        ) {
            dashboardScreen(onCategoryClick = { category -> navController.navigateToTransactions(category) })
            transactionsGraph(navController)
        }
    }
}

// Shared enter transition: fade combined with a slight slide-in from the right.
private fun AnimatedContentTransitionScope<*>.fadeInAndSlideIn() =
    fadeIn(tween(TRANSITION_DURATION_MS)) +
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(TRANSITION_DURATION_MS),
            initialOffset = { it / 8 },
        )

// Bottom navigation bar switching between the Overview and Transactions top-level destinations.
@Composable
private fun AppBottomBar(
    currentRoute: String?,
    navController: NavController,
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == DASHBOARD_ROUTE,
            onClick = { navController.navigateToTopLevel(DASHBOARD_ROUTE) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("Overview") },
        )
        NavigationBarItem(
            selected = currentRoute == TRANSACTIONS_ROUTE,
            onClick = { navController.navigateToTopLevel(TRANSACTIONS_BASE_ROUTE) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text("Transactions") },
        )
    }
}

// Navigates to a top-level destination, preserving each tab's back stack state.
private fun NavController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
