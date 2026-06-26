package com.calorai.app.ui.screens.history

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
            .background(Background)
            .padding(paddingValues)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meal History",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            )
            IconButton(
                onClick = { viewModel.loadHistory() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Surface2)
                    .size(40.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = OrangeAccent,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
            }

            uiState.errorMessage != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.loadHistory() },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }

            uiState.meals.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.NoMeals,
                        contentDescription = null,
                        tint = OnSurfaceDim,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "No meals logged yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to log your first meal",
                        style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant),
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                val grouped = groupMealsByDate(uiState.meals)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    grouped.forEach { (dateLabel, meals) ->
                        item(key = dateLabel) {
                            DayGroupCard(
                                dateLabel = dateLabel,
                                meals = meals,
                                savingMealId = uiState.savingMealId,
                                onSaveCalories = { id, cal -> viewModel.updateMealCalories(id, cal) }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DayGroupCard(
    dateLabel: String,
    meals: List<MealLog>,
    savingMealId: String?,
    onSaveCalories: (String, Double) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val dayTotal = meals.sumOf { it.totalCalories }.toInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Date header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$dayTotal kcal",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold
                    )
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = OnSurfaceDim,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface1),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    meals.forEachIndexed { idx, meal ->
                        MealHistoryRow(
                            meal = meal,
                            isSaving = savingMealId == meal.id,
                            onSaveCalories = { cal -> onSaveCalories(meal.id, cal) }
                        )
                        if (idx < meals.lastIndex) {
                            HorizontalDivider(
                                color = Surface2,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealHistoryRow(
    meal: MealLog,
    isSaving: Boolean,
    onSaveCalories: (Double) -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var fieldValue by remember { mutableStateOf("") }
    val displayCalories = meal.totalCalories.toInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(OrangeContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                val displayNote = if (meal.note.length > 45) meal.note.take(45) + "…" else meal.note
                Text(
                    text = displayNote,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = OnSurface,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
                Text(
                    text = formatTime(meal.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant)
                )
            }
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = OrangeAccent,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            fieldValue = displayCalories.toString()
                            editing = !editing
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$displayCalories kcal",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit calories",
                        tint = OnSurfaceDim,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = editing,
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 68.dp, end = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = { v -> if (v.all { it.isDigit() } || v.isEmpty()) fieldValue = v },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    suffix = { Text("kcal", color = OnSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        fieldValue.toDoubleOrNull()?.let { onSaveCalories(it) }
                        editing = false
                    }),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        unfocusedBorderColor = Surface3,
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        cursorColor = OrangeAccent,
                        focusedContainerColor = Surface2,
                        unfocusedContainerColor = Surface1
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                IconButton(
                    onClick = {
                        fieldValue.toDoubleOrNull()?.let { onSaveCalories(it) }
                        editing = false
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = { editing = false },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Surface3)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = OnSurface, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

private fun groupMealsByDate(meals: List<MealLog>): Map<String, List<MealLog>> {
    val today = java.time.LocalDate.now()
    val yesterday = today.minusDays(1)
    val result = linkedMapOf<String, MutableList<MealLog>>()

    meals.forEach { meal ->
        val date = try {
            java.time.OffsetDateTime.parse(meal.timestamp).toLocalDate()
        } catch (e: Exception) {
            null
        }
        val label = when (date) {
            today -> "Today"
            yesterday -> "Yesterday"
            null -> "Unknown"
            else -> date.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
        }
        result.getOrPut(label) { mutableListOf() }.add(meal)
    }
    return result
}

private fun formatTime(isoDate: String): String {
    return try {
        val date = java.time.OffsetDateTime.parse(isoDate)
        date.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        isoDate.take(10)
    }
}
