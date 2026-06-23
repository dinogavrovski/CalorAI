package com.calorai.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorai.app.ui.theme.OrangeAccent
import com.calorai.app.ui.theme.Surface2
import com.calorai.app.ui.theme.OnSurface
import com.calorai.app.ui.theme.OnSurfaceVariant

/**
 * Semicircular gauge — half arc, flat side at the bottom.
 * The arc sweeps from 180° (left) to 0° (right), i.e. the top half of a circle.
 */
@Composable
fun CalorieProgressRing(
    consumed: Int,
    goal: Int,
    modifier: Modifier = Modifier,
    ringSize: Dp = 180.dp,
    strokeWidth: Dp = 18.dp
) {
    val progress = if (goal > 0) (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
    val isOverGoal = consumed > goal

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "calorieProgress"
    )

    val trackColor = Surface2
    val progressColor = if (isOverGoal) Color(0xFFFF5252) else OrangeAccent

    // The semicircle sweeps 180 degrees, starting at 180 (left) going clockwise to 0 (right)
    val startAngle = 180f
    val totalSweep = 180f

    Box(
        modifier = modifier.size(width = ringSize, height = ringSize / 2 + strokeWidth),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcRect = Size(size.width - strokePx, size.width - strokePx)

            // Track arc (background)
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcRect,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = startAngle,
                    sweepAngle = totalSweep * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcRect,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center text — positioned to sit inside the flat bottom of the semicircle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = consumed.toString(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isOverGoal) Color(0xFFFF5252) else OnSurface,
                    fontSize = if (ringSize >= 160.dp) 36.sp else 24.sp
                )
            )
            Text(
                text = "of $goal kcal",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = OnSurfaceVariant,
                    fontSize = if (ringSize >= 160.dp) 12.sp else 10.sp
                )
            )
        }
    }
}
