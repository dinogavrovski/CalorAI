package com.calorai.app.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorai.app.data.remote.models.MealLog
import com.calorai.app.ui.components.CalorieProgressRing
import com.calorai.app.ui.components.MacroCard
import com.calorai.app.ui.theme.*

@Composable
fun HomeScreen(
    onLogMeal: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Good ${greetingPart()}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant)
                )
                Text(
                    text = uiState.profile?.email?.substringBefore("@") ?: "there",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                )
            }
            // Avatar circle
            val initial = uiState.profile?.email?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(OrangeContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = todayDateString(),
            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceDim)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main calorie card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface1),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) {
                Text(
                    text = "TODAY'S CALORIES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OnSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.isLoading) {
                    ShimmerCalorieCard()
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = uiState.consumedCalories.toString(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface,
                                    fontSize = 52.sp,
                                    letterSpacing = (-2).sp
                                )
                            )
                            Text(
                                text = "Calories Consumed",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val remaining = maxOf(0, uiState.calorieGoal - uiState.consumedCalories)
                            Text(
                                text = "$remaining left",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = OrangeAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        CalorieProgressRing(
                            consumed = uiState.consumedCalories,
                            goal = uiState.calorieGoal,
                            ringSize = 140.dp,
                            strokeWidth = 16.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Macro cards row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroCard(
                            name = "Carbs",
                            current = 0,
                            goal = 250,
                            color = MacroCarbs,
                            modifier = Modifier.weight(1f)
                        )
                        MacroCard(
                            name = "Protein",
                            current = 0,
                            goal = 150,
                            color = MacroProtein,
                            modifier = Modifier.weight(1f)
                        )
                        MacroCard(
                            name = "Fat",
                            current = 0,
                            goal = 65,
                            color = MacroFat,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Meals section
        if (!uiState.isLoading && uiState.recentMeals.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Meals",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                )
                TextButton(onClick = {}) {
                    Text(
                        text = "See All →",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OrangeAccent,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface1),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    uiState.recentMeals.forEachIndexed { index, meal ->
                        RecentMealRow(meal = meal)
                        if (index < uiState.recentMeals.lastIndex) {
                            HorizontalDivider(
                                color = Surface2,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ShimmerCalorieCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    Column {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Surface2)
                .alpha(alpha)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Surface2)
                .alpha(alpha)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface2)
                        .alpha(alpha)
                )
            }
        }
    }
}

@Composable
private fun RecentMealRow(meal: MealLog) {
    val mealColors = listOf(
        Color(0xFF3D1A0A), Color(0xFF1A1A3D), Color(0xFF0A3D1A), Color(0xFF3D3A0A)
    )
    val mealIconColors = listOf(OrangeAccent, MacroProtein, MacroFat, MacroCarbs)
    val colorIndex = (meal.note.hashCode() and 0x7FFFFFFF) % mealColors.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(mealColors[colorIndex]),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Restaurant,
                contentDescription = null,
                tint = mealIconColors[colorIndex],
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            val displayNote = if (meal.note.length > 40) meal.note.take(40) + "…" else meal.note
            Text(
                text = displayNote,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = OnSurface,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            Text(
                text = formatMealTime(meal.timestamp),
                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant)
            )
        }
        Text(
            text = "${meal.totalCalories.toInt()} kcal",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = OrangeAccent,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

private fun greetingPart(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "morning"
        hour < 17 -> "afternoon"
        else -> "evening"
    }
}

private fun todayDateString(): String {
    val now = java.time.LocalDate.now()
    return now.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d"))
}

private fun formatMealTime(isoDate: String): String {
    return try {
        val date = java.time.OffsetDateTime.parse(isoDate)
        date.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        isoDate.take(10)
    }
}
