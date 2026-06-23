package com.calorai.app.ui.screens.home

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorai.app.data.remote.models.MealLog
import com.calorai.app.ui.components.CalorieProgressRing
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
            .background(Surface0)
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Good ${greetingPart()}", style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant))
                Text(
                    text = uiState.profile?.email?.substringBefore("@") ?: "there",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = OnSurface)
                )
            }
            IconButton(
                onClick = { viewModel.loadData() },
                modifier = Modifier.clip(CircleShape).background(Surface2).size(40.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Surface1),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Today's Progress", style = MaterialTheme.typography.titleSmall.copy(color = OnSurfaceVariant, letterSpacing = 1.sp))
                Spacer(modifier = Modifier.height(20.dp))
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(200.dp).padding(40.dp), color = Green400)
                } else {
                    CalorieProgressRing(consumed = uiState.consumedCalories, goal = uiState.calorieGoal, ringSize = 200.dp, strokeWidth = 14.dp)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    MacroChip(label = "Consumed", value = "${uiState.todayCalories} kcal")
                    MacroChip(label = "Goal", value = "${uiState.calorieGoal} kcal")
                    MacroChip(label = "Left", value = "${maxOf(0, uiState.calorieGoal - uiState.todayCalories)} kcal")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onLogMeal,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green400, contentColor = Color(0xFF003314)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log a Meal", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.recentMeals.isNotEmpty()) {
            Text("Recent Meals", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = OnSurface))
            Spacer(modifier = Modifier.height(12.dp))
            uiState.recentMeals.forEach { meal ->
                RecentMealCard(meal = meal)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(error, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun MacroChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.labelMedium.copy(color = OnSurface, fontWeight = FontWeight.SemiBold))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant))
    }
}

@Composable
private fun RecentMealCard(meal: MealLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface1),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(GreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Green400, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val displayNote = meal.note.take(40).let { if (meal.note.length > 40) "$it…" else it }
                Text(displayNote, style = MaterialTheme.typography.bodyMedium.copy(color = OnSurface, fontWeight = FontWeight.Medium), maxLines = 1)
                Text(meal.timestamp.take(10), style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant))
            }
            Text("${meal.totalCalories.toInt()} kcal", style = MaterialTheme.typography.labelMedium.copy(color = Green400, fontWeight = FontWeight.SemiBold))
        }
    }
}

private fun greetingPart(): String {
    val hour = java.time.LocalTime.now().hour
    return when { hour < 12 -> "morning"; hour < 17 -> "afternoon"; else -> "evening" }
}
