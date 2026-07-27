package com.example.customerspendingdashboard.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.customerspendingdashboard.core.designsystem.theme.SpendingTheme

// Relative bar heights lifted from the loading icon's own silhouette (16, 24, 32, 40, 48 out of
// a 64 viewBox), normalized against the tallest bar.
private val BAR_HEIGHT_FRACTIONS = listOf(16f / 48f, 24f / 48f, 32f / 48f, 40f / 48f, 1f)
private const val BAR_COUNT = 5
private const val DRAW_DURATION_MS = 550
private const val BAR_WIDTH_DP = 6
private const val BAR_SPACING_DP = 6
private const val CONTAINER_SIZE_DP = 48

/** Draws the loading icon's bars from bottom to top, then back down again, on an endless loop —
 * shown while data is loading. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EqualizerIndicator()
    }
}

// Animates and draws the five-bar equalizer indicator itself.
@Composable
private fun EqualizerIndicator(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    val progress =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(DRAW_DURATION_MS, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "equalizer-draw-progress",
        )

    Canvas(
        modifier =
            modifier
                .size(CONTAINER_SIZE_DP.dp)
                .semantics { contentDescription = "Loading" },
    ) {
        val barWidth = BAR_WIDTH_DP.dp.toPx()
        val spacing = BAR_SPACING_DP.dp.toPx()
        val totalWidth = BAR_COUNT * barWidth + (BAR_COUNT - 1) * spacing
        val startX = (size.width - totalWidth) / 2f

        BAR_HEIGHT_FRACTIONS.forEachIndexed { index, heightFraction ->
            val barHeight = size.height * heightFraction * progress.value
            val left = startX + index * (barWidth + spacing)
            val top = size.height - barHeight
            drawRoundRect(
                color = color,
                topLeft = Offset(x = left, y = top),
                size = Size(width = barWidth, height = barHeight),
                cornerRadius = CornerRadius(x = barWidth / 2f, y = barWidth / 2f),
            )
        }
    }
}

// Preview of LoadingState.
@Preview(showBackground = true)
@Composable
private fun LoadingStatePreview() {
    SpendingTheme {
        LoadingState()
    }
}
