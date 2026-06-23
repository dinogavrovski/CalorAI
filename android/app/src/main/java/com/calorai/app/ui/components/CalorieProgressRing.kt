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
import com.calorai.app.ui.theme.Green400
import com.calorai.app.ui.theme.Surface2

@Composable
fun CalorieProgressRing(
    consumed: Int,
    goal: Int,
    modifier: Modifier = Modifier,
    ringSize: Dp = 200.dp,
    strokeWidth: Dp = 14.dp
) {
    val progress = if (goal > 0) (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
    val isOverGoal = consumed > goal

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "calorieProgress"
    )

    val trackColor = Surface2
    val progressColor = if (isOverGoal) {
        Color(0xFFFF5252)  // red when over goal
    } else {
        Green400
    }

    // Gentle pulse when near/over goal
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcRect = Size(size.width - strokePx, size.height - strokePx)

            // Track (background arc)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcRect,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Glow / shadow layer
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor.copy(alpha = glowAlpha * 0.25f),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(inset - strokePx * 0.3f, inset - strokePx * 0.3f),
                    size = Size(arcRect.width + strokePx * 0.6f, arcRect.height + strokePx * 0.6f),
                    style = Stroke(width = strokePx * 1.6f, cap = StrokeCap.Round)
                )
            }

            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            progressColor.copy(alpha = 0.6f),
                            progressColor,
                            progressColor
                        ),
                        center = Offset(size.width / 2f, size.height / 2f)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcRect,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Centre text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = consumed.toString(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isOverGoal) Color(0xFFFF5252) else Green400,
                    fontSize = if (ringSize >= 180.dp) 40.sp else 28.sp
                )
            )
            Text(
                text = "of $goal kcal",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = if (ringSize >= 180.dp) 13.sp else 11.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            val remaining = goal - consumed
            Text(
                text = if (isOverGoal) "+${-remaining} over" else "$remaining left",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isOverGoal) Color(0xFFFF5252).copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            )
        }
    }
}
