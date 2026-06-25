package com.calorai.app.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorai.app.data.remote.models.MealLog
import com.calorai.app.ui.components.AppIcons
import com.calorai.app.ui.components.CalorieProgressRing
import com.calorai.app.ui.components.MacroCard
import com.calorai.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Subtle white border used on all cards throughout the screen
private val CardBorder = BorderStroke(1.dp, Color(0x14FFFFFF))

@Composable
fun HomeScreen(
    onLogMeal: () -> Unit,
    onWeightClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1A1A), Color(0xFF111111)),
                    startY = 0f,
                    endY = 1200f
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OrangeAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("N", style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp
                    ))
                }
                Text(
                    text = "NoteBite",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = OnSurface, fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp, letterSpacing = (-0.5).sp
                    )
                )
            }

            // Streak + Bell + Avatar
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Streak pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .border(CardBorder, RoundedCornerShape(99.dp))
                        .background(Surface1)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(AppIcons.Flame, contentDescription = "Streak",
                        tint = OrangeAccent, modifier = Modifier.size(14.dp))
                    Text("8", style = MaterialTheme.typography.labelMedium.copy(
                        color = OnSurface, fontWeight = FontWeight.Bold))
                }
                // Bell
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(CardBorder, CircleShape)
                        .background(Surface1),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(AppIcons.Bell, contentDescription = "Notifications",
                        tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                // Avatar
                val initial = uiState.profile?.email?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(BorderStroke(1.5.dp, OrangeAccent.copy(alpha = 0.6f)), CircleShape)
                        .background(Brush.linearGradient(listOf(OrangeAccent, Color(0xFFFF8C00)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initial, style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White, fontWeight = FontWeight.ExtraBold))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Calendar strip card ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(CardBorder, RoundedCornerShape(20.dp))
                .background(Surface1)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            CalendarStrip()
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Today at a Glance card ───────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(CardBorder, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface1),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Card header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Icon(AppIcons.Energy, contentDescription = null,
                                tint = OrangeAccent, modifier = Modifier.size(16.dp))
                            Text("Today at a Glance",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = OnSurface, fontWeight = FontWeight.Bold))
                        }
                        Text("Calories and macros for today",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant))
                    }
                    Icon(AppIcons.MoreDots, contentDescription = "More",
                        tint = Color(0xFF555555), modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calories left + arc gauge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val remaining = maxOf(0, uiState.calorieGoal - uiState.consumedCalories)
                        Text(
                            text = remaining.toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = OnSurface, fontWeight = FontWeight.ExtraBold,
                                fontSize = 42.sp, letterSpacing = (-2).sp, lineHeight = 42.sp
                            )
                        )
                        Text("Calories Left",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OnSurfaceVariant, fontWeight = FontWeight.Medium))
                    }

                    CalorieProgressRing(
                        consumed = uiState.consumedCalories,
                        goal = uiState.calorieGoal,
                        ringSize = 110.dp,
                        strokeWidth = 12.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Macro mini-cards row ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MacroCard(
                name = "Carbs",
                current = uiState.consumedCarbs.toInt(),
                goal = 250,
                color = MacroCarbs,
                modifier = Modifier.weight(1f)
            )
            MacroCard(
                name = "Protein",
                current = uiState.consumedProtein.toInt(),
                goal = 150,
                color = MacroProtein,
                modifier = Modifier.weight(1f)
            )
            MacroCard(
                name = "Fat",
                current = uiState.consumedFat.toInt(),
                goal = 65,
                color = MacroFat,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Weight badge ─────────────────────────────────────────────────────
        WeightBadge(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = onWeightClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Recent Meals ─────────────────────────────────────────────────────
        if (!uiState.isLoading && uiState.recentMeals.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Meals",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = OnSurface, fontWeight = FontWeight.Bold))
                TextButton(onClick = {}) {
                    Text("See All →",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OrangeAccent, fontWeight = FontWeight.SemiBold))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(CardBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface1),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    uiState.recentMeals.forEachIndexed { index, meal ->
                        RecentMealRow(meal = meal)
                        if (index < uiState.recentMeals.lastIndex) {
                            HorizontalDivider(
                                color = Color(0xFF2A2A2A),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(error,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                modifier = Modifier.padding(horizontal = 20.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun CalendarStrip() {
    val today = LocalDate.now()
    val days = (-3..3).map { today.plusDays(it.toLong()) }

    Column {
        // Date header with arrows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", style = MaterialTheme.typography.titleMedium.copy(
                color = OnSurfaceDim, fontWeight = FontWeight.Light))
            Text(
                text = today.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy")),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = OnSurface, fontWeight = FontWeight.SemiBold)
            )
            Text("›", style = MaterialTheme.typography.titleMedium.copy(
                color = OnSurfaceDim, fontWeight = FontWeight.Light))
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                val isToday = day == today
                val dayName = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    .uppercase().take(3)

                // Orange ring border on selected day, transparent otherwise
                val dayModifier = if (isToday) {
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.2.dp, OrangeAccent), RoundedCornerShape(12.dp))
                        .padding(horizontal = 7.dp, vertical = 8.dp)
                } else {
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .padding(horizontal = 7.dp, vertical = 8.dp)
                }

                Column(
                    modifier = dayModifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isToday) OrangeAccent else OnSurfaceDim,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = day.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (isToday) OnSurface else OnSurfaceVariant,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    )
                    // Small dot: orange for today, subtle for others
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(if (isToday) OrangeAccent else Color(0xFF333333))
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentMealRow(meal: MealLog) {
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
                .background(OrangeContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(AppIcons.Bowl, contentDescription = null,
                tint = OrangeAccent, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            val displayNote = if (meal.note.length > 40) meal.note.take(40) + "…" else meal.note
            Text(displayNote,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = OnSurface, fontWeight = FontWeight.Medium),
                maxLines = 1)
            Text(formatMealTime(meal.timestamp),
                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant))
        }
        Text("${meal.totalCalories.toInt()} kcal",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = OrangeAccent, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun WeightBadge(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val viewModel: com.calorai.app.ui.screens.weight.WeightViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(CardBorder, RoundedCornerShape(16.dp))
            .background(Surface1)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrangeContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("⚖", fontSize = 16.sp)
            }
            Column {
                Text("Weight", style = MaterialTheme.typography.labelMedium.copy(
                    color = OnSurfaceVariant, fontWeight = FontWeight.Medium))
                if (state.latest != null) {
                    Text(
                        "%.1f kg".format(state.latest!!.weightKg),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OnSurface, fontWeight = FontWeight.Bold)
                    )
                } else {
                    Text("Tap to log", style = MaterialTheme.typography.bodySmall.copy(
                        color = OnSurfaceDim))
                }
            }
        }
        Text("›", style = MaterialTheme.typography.titleMedium.copy(
            color = OnSurfaceDim, fontWeight = FontWeight.Light))
    }
}

private fun formatMealTime(isoDate: String): String {
    return try {
        val date = java.time.OffsetDateTime.parse(isoDate)
        date.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        isoDate.take(10)
    }
}
