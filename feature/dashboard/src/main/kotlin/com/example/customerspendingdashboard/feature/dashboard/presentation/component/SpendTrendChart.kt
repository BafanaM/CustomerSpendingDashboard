package com.example.customerspendingdashboard.feature.dashboard.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.customerspendingdashboard.core.common.Money
import com.example.customerspendingdashboard.core.model.TrendPoint

private const val CHART_HEIGHT_DP = 140
private const val LINE_STROKE_WIDTH = 4f
private const val POINT_RADIUS = 5f
private const val DRAW_ANIMATION_MS = 700
private const val Y_AXIS_TICK_COUNT = 4
private const val AXIS_LINE_ALPHA = 0.4f
private const val AXIS_LABEL_GAP_DP = 8

/** Animated line chart of the spend trend with Y-axis gridlines/labels and X-axis point labels.
 * Hand-drawn on [Canvas] rather than via a charting library: two simple, non-interactive charts
 * don't justify pulling in an external dependency (and its own version-compatibility surface). */
@Composable
fun SpendTrendChart(
    trend: List<TrendPoint>,
    modifier: Modifier = Modifier,
) {
    if (trend.isEmpty()) return

    val lineColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.outline
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val maxMinorUnits = trend.maxOf { it.amount.minorUnits }.coerceAtLeast(1)
    val currencyCode = trend.first().amount.currencyCode

    val progress = animatedDrawProgress(trend)

    val textMeasurer = rememberTextMeasurer()
    val axisLabels = remember(maxMinorUnits, currencyCode) { yAxisLabels(maxMinorUnits, currencyCode) }
    val maxLabelWidthPx =
        remember(axisLabels, textMeasurer, labelStyle) {
            axisLabels.maxOf { textMeasurer.measure(it, labelStyle).size.width }
        }
    val density = LocalDensity.current
    val axisLabelGapPx = with(density) { AXIS_LABEL_GAP_DP.dp.toPx() }
    val leftMarginDp = with(density) { (maxLabelWidthPx + axisLabelGapPx).toDp() }

    Column(modifier) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(CHART_HEIGHT_DP.dp),
        ) {
            val leftMargin = maxLabelWidthPx + axisLabelGapPx
            drawYAxis(leftMargin, axisColor, axisLabels, textMeasurer, labelStyle)
            drawTrendLine(trend, leftMargin, size.width - leftMargin, maxMinorUnits, progress, lineColor)
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = leftMarginDp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            trend.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// Drives the line/points draw-in animation from 0 to 1, restarting whenever the trend data changes.
@Composable
private fun animatedDrawProgress(trend: List<TrendPoint>): Float {
    val progressAnimatable = remember(trend) { Animatable(0f) }
    LaunchedEffect(trend) {
        progressAnimatable.snapTo(0f)
        progressAnimatable.animateTo(1f, animationSpec = tween(DRAW_ANIMATION_MS, easing = FastOutSlowInEasing))
    }
    return progressAnimatable.value
}

// Formats Y_AXIS_TICK_COUNT evenly-spaced currency labels from 0 up to the max trend value.
private fun yAxisLabels(
    maxMinorUnits: Long,
    currencyCode: String,
): List<String> =
    (0 until Y_AXIS_TICK_COUNT).map { tick ->
        val fraction = tick / (Y_AXIS_TICK_COUNT - 1).toFloat()
        Money((maxMinorUnits * fraction).toLong(), currencyCode).format()
    }

// Draws horizontal gridlines and their currency labels along the Y axis.
private fun DrawScope.drawYAxis(
    leftMargin: Float,
    axisColor: Color,
    axisLabels: List<String>,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
) {
    for (tick in 0 until Y_AXIS_TICK_COUNT) {
        val fraction = tick / (Y_AXIS_TICK_COUNT - 1).toFloat()
        val y = size.height - (fraction * size.height)
        drawLine(
            color = axisColor,
            start = Offset(x = leftMargin, y = y),
            end = Offset(x = size.width, y = y),
            strokeWidth = 1.dp.toPx(),
            alpha = AXIS_LINE_ALPHA,
        )
        val label = textMeasurer.measure(axisLabels[tick], labelStyle)
        val labelY = (y - label.size.height / 2f).coerceIn(0f, size.height - label.size.height)
        drawText(textLayoutResult = label, topLeft = Offset(x = 0f, y = labelY))
    }
}

// Draws the animated trend line and its points, scaled to the plot area.
private fun DrawScope.drawTrendLine(
    trend: List<TrendPoint>,
    leftMargin: Float,
    plotWidth: Float,
    maxMinorUnits: Long,
    progress: Float,
    lineColor: Color,
) {
    val stepX = if (trend.size > 1) plotWidth / (trend.size - 1) else 0f
    val points =
        trend.mapIndexed { index, point ->
            val x = leftMargin + (if (trend.size > 1) index * stepX else plotWidth / 2f)
            val ratio = point.amount.minorUnits.toFloat() / maxMinorUnits
            val targetY = size.height - (ratio * size.height)
            val animatedY = size.height - ((size.height - targetY) * progress)
            Offset(x, animatedY)
        }

    if (points.size > 1) {
        val path =
            Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = LINE_STROKE_WIDTH, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
    points.forEach { drawCircle(color = lineColor, radius = POINT_RADIUS, center = it, alpha = progress) }
}
