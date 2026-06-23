package com.calorai.app.ui.screens.history

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorai.app.data.remote.models.MealLog
import com.calorai.app.ui.theme.*

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0)
            .padding(paddingValues)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meal History",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = OnSurface)
            )
            IconButton(
                onClick = { viewModel.loadHistory() },
                modifier = Modifier.clip(CircleShape).background(Surface2).size(40.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }

        when {
            uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green400)
            }
            uiState.errorMessage != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(uiState.errorMessage ?: "Unknown error", style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant), modifier = Modifier.padding(horizontal = 32.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.loadHistory() }) { Text("Retry", color = Green400) }
                }
            }
            uiState.meals.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NoMeals, contentDescription = null, tint = OnSurfaceDim, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No meals logged yet", style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceVariant))
                    Text("Start tracking to see your history here.", style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceDim))
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.meals, key = { it.id }) { meal -> MealHistoryCard(meal = meal) }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun MealHistoryCard(meal: MealLog) {
    var expanded by remember { mutableStateOf(false) }
    val avgCalories = meal.totalCalorieRange.takeIf { it.size >= 2 }?.let { ((it[0] + it[1]) / 2).toInt() } ?: meal.totalCalories.toInt()

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Surface1),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(GreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = Green400, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val displayNote = meal.note.take(50).let { if (meal.note.length > 50) "$it…" else it }
                    Text(text = displayNote, style = MaterialTheme.typography.bodyMedium.copy(color = OnSurface, fontWeight = FontWeight.Medium), maxLines = 1)
                    Text(text = formatDate(meal.timestamp), style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "$avgCalories", style = MaterialTheme.typography.titleSmall.copy(color = Green400, fontWeight = FontWeight.Bold))
                    Text(text = "kcal", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant))
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null, tint = OnSurfaceDim, modifier = Modifier.size(20.dp)
                )
            }

            if (expanded && meal.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Surface3, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Breakdown", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant, letterSpacing = 0.8.sp))
                Spacer(modifier = Modifier.height(8.dp))
                meal.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Green400.copy(alpha = 0.6f)))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = item.name, style = MaterialTheme.typography.bodySmall.copy(color = OnSurface), modifier = Modifier.weight(1f))
                        Text(text = "${item.calories.toInt()} kcal", style = MaterialTheme.typography.labelSmall.copy(color = Green300))
                    }
                }
                if (meal.totalCalorieRange.size >= 2) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Range: ${meal.totalCalorieRange[0].toInt()}–${meal.totalCalorieRange[1].toInt()} kcal",
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceDim, fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

private fun formatDate(isoDate: String): String {
    return try {
        val date = java.time.OffsetDateTime.parse(isoDate)
        date.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy · HH:mm"))
    } catch (e: Exception) {
        isoDate.take(10)
    }
}
