package com.example.customerspendingdashboard.feature.transactions.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.feature.transactions.presentation.TransactionDetailScreen
import com.example.customerspendingdashboard.feature.transactions.presentation.TransactionsScreen

const val TRANSACTIONS_BASE_ROUTE = "transactions"
const val CATEGORY_ARG = "category"

/** Route pattern (with an optional category query arg) — this is what `NavBackStackEntry.destination.route`
 * resolves to, so callers matching on the current destination (e.g. bottom-nav highlighting) compare
 * against this, not [TRANSACTIONS_BASE_ROUTE]. */
const val TRANSACTIONS_ROUTE = "$TRANSACTIONS_BASE_ROUTE?$CATEGORY_ARG={$CATEGORY_ARG}"

const val TRANSACTION_ID_ARG = "transactionId"
const val TRANSACTION_DETAIL_ROUTE = "transactions/{$TRANSACTION_ID_ARG}"

// Registers the transaction list and detail destinations in the nav graph.
fun NavGraphBuilder.transactionsGraph(navController: NavController) {
    composable(
        route = TRANSACTIONS_ROUTE,
        arguments =
            listOf(
                navArgument(CATEGORY_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
    ) {
        TransactionsScreen(
            onTransactionClick = { id -> navController.navigate("transactions/$id") },
        )
    }
    composable(
        route = TRANSACTION_DETAIL_ROUTE,
        arguments = listOf(navArgument(TRANSACTION_ID_ARG) { type = NavType.StringType }),
    ) {
        TransactionDetailScreen()
    }
}

/** Builds and navigates to the transactions route, optionally pre-filtered to [category] — used
 * from the dashboard's category breakdown, where tapping a category should jump straight to its
 * transactions.
 *
 * Transactions is a top-level (bottom-nav) destination, so this pops up to the graph's start
 * destination just like the bottom bar's own tab switch does. Without that popUpTo, a category
 * click pushes a *second* transactions entry on top of any tab-switch entry already on the back
 * stack, and the bottom bar's "Overview" tap — which only pops back to the start destination —
 * no longer lands on top of a single, predictable transactions entry. `restoreState` is
 * deliberately omitted so a fresh, correctly-filtered screen is always shown rather than a stale
 * previously-saved (possibly differently-filtered) one. */
fun NavController.navigateToTransactions(category: Category? = null) {
    val route =
        if (category != null) {
            "$TRANSACTIONS_BASE_ROUTE?$CATEGORY_ARG=${category.name}"
        } else {
            TRANSACTIONS_BASE_ROUTE
        }
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
    }
}
