package com.calorai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorai.app.ui.theme.OnSurface
import com.calorai.app.ui.theme.OnSurfaceVariant
import com.calorai.app.ui.theme.Surface1
import com.calorai.app.ui.theme.MacroCarbs
import com.calorai.app.ui.theme.MacroProtein
import com.calorai.app.ui.theme.MacroFat

@Composable
fun MacroCard(
    name: String,
    current: Int,
    goal: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) (current.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
    val icon = macroIcon(name)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(BorderStroke(1.dp, Color(0x14FFFFFF)), RoundedCornerShape(18.dp))
            .background(Surface1)
            .padding(12.dp)
    ) {
        // Header: icon + colored name + dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }
            Icon(
                imageVector = AppIcons.MoreDots,
                contentDescription = "More",
                tint = Color(0xFF444444),
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Big number
        Text(
            text = "$current",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = OnSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                letterSpacing = (-0.5).sp
            )
        )
        Text(
            text = "/ ${goal}g",
            style = MaterialTheme.typography.bodySmall.copy(
                color = OnSurfaceVariant,
                fontSize = 11.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Thick pill progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0xFF2A2A2A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(99.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun macroIcon(name: String): ImageVector = when (name.lowercase()) {
    "carbs" -> AppIcons.Wheat
    "protein" -> AppIcons.Steak
    "fat" -> AppIcons.Drop
    else -> AppIcons.Energy
}
