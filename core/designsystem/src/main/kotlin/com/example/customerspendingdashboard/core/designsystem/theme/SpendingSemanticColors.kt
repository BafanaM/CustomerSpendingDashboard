package com.example.customerspendingdashboard.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/** Domain-specific colors (spend/income) that Material3's default color scheme has no slot for. */
@Immutable
data class SpendingSemanticColors(
    val spend: androidx.compose.ui.graphics.Color,
    val income: androidx.compose.ui.graphics.Color,
)

private val LightSemanticColors = SpendingSemanticColors(spend = SpendRed, income = IncomeGreen)
private val DarkSemanticColors = SpendingSemanticColors(spend = SpendRed, income = Teal80)

val LocalSpendingSemanticColors = staticCompositionLocalOf { LightSemanticColors }

// Picks the light or dark semantic color set to match the active theme.
@Composable
fun spendingSemanticColors(darkTheme: Boolean) = if (darkTheme) DarkSemanticColors else LightSemanticColors
