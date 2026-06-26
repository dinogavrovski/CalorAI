package com.calorai.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.calorai.app.ui.theme.OrangeAccent

@Composable
fun WeightSparkline(
    weights: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = OrangeAccent
) {
    if (weights.size < 2) return

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "sparklineAnim"
    )

    val minW = (weights.min() - 0.5f).coerceAtLeast(0f)
    val maxW = weights.max() + 0.5f
    val range = (maxW - minW).coerceAtLeast(0.01f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val n = weights.size

        val pts = weights.mapIndexed { i, wt ->
            val x = w * i / (n - 1).toFloat()
            val y = h - h * ((wt - minW) / range).coerceIn(0f, 1f)
            Offset(x, y)
        }

        val animated = pts.map { Offset(it.x, it.y + (h - it.y) * (1f - animProgress)) }

        // Gradient fill
        val fillPath = Path().apply {
            moveTo(animated.first().x, h)
            animated.forEach { lineTo(it.x, it.y) }
            lineTo(animated.last().x, h)
            close()
        }
        drawPath(
            fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        // Line
        val linePath = Path().apply {
            moveTo(animated.first().x, animated.first().y)
            animated.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            linePath,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Last dot only
        animated.last().let { pt ->
            drawCircle(lineColor, radius = 3.dp.toPx(), center = pt)
            drawCircle(Color(0xFF1C1C1E), radius = 1.5.dp.toPx(), center = pt)
        }
    }
}
