package com.example.customerspendingdashboard.feature.dashboard.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.customerspendingdashboard.core.model.Category
import com.example.customerspendingdashboard.core.model.CategorySpend

private const val BAR_HEIGHT_DP = 8
private const val DRAW_ANIMATION_MS = 700

/** Animated horizontal bar chart of category spend, sorted largest first — more legible on a
 * phone width than a pie/donut, and needs no charting library for what is effectively a sorted
 * list with a visual weight per row. Tapping a row jumps to that category's transactions. */
@Composable
fun CategoryBreakdownChart(
    breakdown: List<CategorySpend>,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (breakdown.isEmpty()) return

    val maxPercentage = breakdown.maxOf { it.percentage }.coerceAtLeast(Float.MIN_VALUE)

    val progressAnimatable = remember(breakdown) { Animatable(0f) }
    LaunchedEffect(breakdown) {
        progressAnimatable.snapTo(0f)
        progressAnimatable.animateTo(1f, animationSpec = tween(DRAW_ANIMATION_MS, easing = FastOutSlowInEasing))
    }
    val progress = progressAnimatable.value

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        breakdown.forEach { categorySpend ->
            CategoryBar(
                categorySpend = categorySpend,
                fraction = (categorySpend.percentage / maxPercentage) * progress,
                onClick = { onCategoryClick(categorySpend.category) },
            )
        }
    }
}

// A single clickable category row: label, total, and its proportional bar.
@Composable
private fun CategoryBar(
    categorySpend: CategorySpend,
    fraction: Float,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .semantics {
                    contentDescription = "${categorySpend.category.displayName}, ${categorySpend.total.format()}"
                },
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = categorySpend.category.displayName, style = MaterialTheme.typography.bodyMedium)
            Text(text = categorySpend.total.format(), style = MaterialTheme.typography.bodyMedium)
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(BAR_HEIGHT_DP.dp)
                    .clip(RoundedCornerShape(BAR_HEIGHT_DP.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(BAR_HEIGHT_DP.dp))
                        .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
