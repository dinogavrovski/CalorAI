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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorai.app.ui.components.AppIcons
import com.calorai.app.ui.components.CalorieProgressRing
import com.calorai.app.ui.components.MacroCard
import com.calorai.app.ui.theme.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Subtle white border used on all cards throughout the screen
private val CardBorder = BorderStroke(1.dp, Color(0x14FFFFFF))

@Composable
fun HomeScreen(
    onLogMeal: () -> Unit,
    onNavigateToWeight: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadData()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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

        // ── Weight graph card ────────────────────────────────────────────────
        WeightGraphCard(
            onNavigateToWeight = onNavigateToWeight,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

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
private fun WeightGraphCard(
    onNavigateToWeight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weightViewModel: com.calorai.app.ui.screens.weight.WeightViewModel =
        hiltViewModel()
    val state by weightViewModel.state.collectAsStateWithLifecycle()

    val entries = state.entries
    val latest = state.latest

    Card(
        modifier = modifier
            .border(CardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onNavigateToWeight),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface1),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text("⚖", fontSize = 14.sp)
                        Text(
                            "Weight",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = OnSurface, fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        "Last 30 days",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (latest != null) {
                        val trend = if (entries.size >= 2)
                            entries.last().weightKg - entries[entries.size - 2].weightKg
                        else null

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "%.1f kg".format(latest.weightKg),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = OnSurface, fontWeight = FontWeight.ExtraBold
                                )
                            )
                            if (trend != null && trend != 0.0) {
                                val trendColor = if (trend < 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                Text(
                                    text = "${if (trend < 0) "↓" else "↑"} ${"%.1f".format(Math.abs(trend))} kg",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = trendColor, fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(OrangeAccent.copy(alpha = 0.15f))
                            .clickable(onClick = {
                                weightViewModel.showSheet()
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            AppIcons.Add,
                            contentDescription = "Log weight",
                            tint = OrangeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (entries.size >= 2) {
                WeightLineChart(entries = entries)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Log your weight to see progress",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceDim)
                    )
                }
            }
        }
    }

    // Weight input sheet
    if (state.showInputSheet) {
        WeightInputSheet(
            onDismiss = { weightViewModel.hideSheet() },
            onConfirm = { kg -> weightViewModel.logWeight(kg) }
        )
    }
}

@Composable
private fun WeightLineChart(
    entries: List<com.calorai.app.data.remote.models.WeightEntry>
) {
    if (entries.isEmpty()) return

    val weights = entries.map { it.weightKg.toFloat() }
    val minW = weights.min()
    val maxW = weights.max()
    val range = (maxW - minW).coerceAtLeast(1f)

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "chartAnim"
    )

    val lineColor = OrangeAccent
    val gradientTop = OrangeAccent.copy(alpha = 0.3f)
    val gradientBot = OrangeAccent.copy(alpha = 0f)

    val yLabels = listOf(maxW, (minW + maxW) / 2f, minW)
    val xLabelFormatter = DateTimeFormatter.ofPattern("d MMM")

    val xLabels = if (entries.size >= 3) {
        listOf(entries.first(), entries[entries.size / 2], entries.last())
    } else {
        listOf(entries.first(), entries.last())
    }.map { entry ->
        try { OffsetDateTime.parse(entry.loggedAt).format(xLabelFormatter) } catch (e: Exception) { "" }
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .height(100.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                yLabels.forEach { label ->
                    Text(
                        "%.0f".format(label),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OnSurfaceDim, fontSize = 9.sp
                        )
                    )
                }
            }

            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
            ) {
                val w = size.width
                val h = size.height
                val n = weights.size
                if (n < 2) return@Canvas

                fun xOf(i: Int) = i / (n - 1f) * w
                fun yOf(v: Float) = h - ((v - minW) / range) * h * 0.85f - h * 0.075f

                val animN = (n * animProgress).toInt().coerceAtLeast(2).coerceAtMost(n)

                // Gradient fill path
                val fillPath = Path().apply {
                    moveTo(xOf(0), yOf(weights[0]))
                    for (i in 1 until animN) {
                        val cx = (xOf(i - 1) + xOf(i)) / 2f
                        cubicTo(cx, yOf(weights[i - 1]), cx, yOf(weights[i]), xOf(i), yOf(weights[i]))
                    }
                    lineTo(xOf(animN - 1), h)
                    lineTo(xOf(0), h)
                    close()
                }
                drawPath(
                    fillPath,
                    brush = Brush.verticalGradient(
                        listOf(gradientTop, gradientBot),
                        startY = 0f,
                        endY = h
                    )
                )

                // Line path
                val linePath = Path().apply {
                    moveTo(xOf(0), yOf(weights[0]))
                    for (i in 1 until animN) {
                        val cx = (xOf(i - 1) + xOf(i)) / 2f
                        cubicTo(cx, yOf(weights[i - 1]), cx, yOf(weights[i]), xOf(i), yOf(weights[i]))
                    }
                }
                drawPath(
                    linePath,
                    color = lineColor,
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Dot on latest point
                val lastX = xOf(animN - 1)
                val lastY = yOf(weights[animN - 1])
                drawCircle(color = Color.White, radius = 5f, center = Offset(lastX, lastY))
                drawCircle(color = lineColor, radius = 3f, center = Offset(lastX, lastY))
            }
        }

        // X-axis date labels
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            xLabels.forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OnSurfaceDim, fontSize = 9.sp
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightInputSheet(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var input by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface2,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Log Weight",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = OnSurface, fontWeight = FontWeight.Bold
                )
            )
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Weight (kg)") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    focusedLabelColor = OrangeAccent,
                    unfocusedBorderColor = OnSurfaceDim,
                    unfocusedLabelColor = OnSurfaceDim,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val kg = input.toDoubleOrNull()
                    if (kg != null && kg > 0) onConfirm(kg)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
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
