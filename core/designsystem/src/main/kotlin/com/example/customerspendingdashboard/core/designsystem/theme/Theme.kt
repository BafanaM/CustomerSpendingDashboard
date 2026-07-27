package com.example.customerspendingdashboard.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val LightColors =
    lightColorScheme(
        primary = Blue40,
        secondary = BlueGrey40,
        tertiary = Teal40,
        error = SpendRed,
        errorContainer = SpendRedContainer,
        surface = SurfaceLight,
    )

private val DarkColors =
    darkColorScheme(
        primary = Blue80,
        secondary = BlueGrey80,
        tertiary = Teal80,
        error = SpendRed,
        errorContainer = SpendRedContainer,
        surface = SurfaceDark,
    )

/**
 * Applies the app's Material3 color scheme, typography, and semantic colors to its content.
 *
 * Dynamic color defaults to off: it derives the palette from the device wallpaper rather than
 * [darkTheme], so on a dark-toned wallpaper it can render a muted, dark-looking scheme even in
 * "light" mode — which reads as the app ignoring the device's light/dark setting. The brand
 * [LightColors]/[DarkColors] palettes track [darkTheme] (device theme) directly instead.
 */
@Composable
fun SpendingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            darkTheme -> DarkColors
            else -> LightColors
        }

    CompositionLocalProvider(LocalSpendingSemanticColors provides spendingSemanticColors(darkTheme)) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SpendingTypography,
            content = content,
        )
    }
}
