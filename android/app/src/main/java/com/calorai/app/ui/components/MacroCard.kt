package com.calorai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorai.app.ui.theme.OnSurface
import com.calorai.app.ui.theme.OnSurfaceVariant
import com.calorai.app.ui.theme.Surface2

@Composable
fun MacroCard(
    name: String,
    current: Int,
    goal: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) (current.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Surface2)
            .padding(12.dp)
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = OnSurfaceVariant,
                letterSpacing = 0.8.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
            Text(
                text = "${current}g",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = " / ${goal}g",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = OnSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Progress bar track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}
