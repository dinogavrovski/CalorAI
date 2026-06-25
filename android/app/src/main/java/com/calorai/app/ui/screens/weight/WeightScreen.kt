package com.calorai.app.ui.screens.weight

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorai.app.data.remote.models.WeightEntry
import com.calorai.app.ui.theme.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    paddingValues: PaddingValues = PaddingValues(),
    viewModel: WeightViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF111111)), endY = 1200f)
            )
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Weight",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = OnSurface, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp
                )
            )
            Button(
                onClick = { viewModel.showSheet() },
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("+ Log Weight", style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White, fontWeight = FontWeight.Bold))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Latest weight stat card
        state.latest?.let { latest ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, Color(0x14FFFFFF)), RoundedCornerShape(20.dp))
                    .background(Surface1)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Weight", style = MaterialTheme.typography.bodySmall.copy(
                            color = OnSurfaceVariant))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "%.1f".format(latest.weightKg),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = OnSurface, fontWeight = FontWeight.ExtraBold,
                                    fontSize = 40.sp, letterSpacing = (-1.5).sp
                                )
                            )
                            Text("kg", style = MaterialTheme.typography.bodyLarge.copy(
                                color = OnSurfaceVariant, fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(bottom = 6.dp))
                        }
                    }

                    // Trend arrow if ≥ 2 entries
                    if (state.entries.size >= 2) {
                        val diff = state.entries.last().weightKg - state.entries[state.entries.size - 2].weightKg
                        val trendColor = if (diff <= 0) MacroFat else Color(0xFFFF5252)
                        val arrow = if (diff <= 0) "↓" else "↑"
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(arrow, style = MaterialTheme.typography.headlineMedium.copy(
                                color = trendColor, fontWeight = FontWeight.Bold))
                            Text("%.1f kg".format(Math.abs(diff)),
                                style = MaterialTheme.typography.labelSmall.copy(color = trendColor))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Period toggle
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Surface1)
                .padding(4.dp)
        ) {
            Row {
                listOf("week" to "Week", "month" to "Month", "year" to "Year").forEach { (key, label) ->
                    val selected = state.period == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (selected) OrangeAccent else Color.Transparent)
                            .clickable { viewModel.setPeriod(key) }
                            .padding(horizontal = 20.dp, vertical = 7.dp)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelMedium.copy(
                            color = if (selected) Color.White else OnSurfaceVariant,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        ))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Graph card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.dp, Color(0x14FFFFFF)), RoundedCornerShape(20.dp))
                .background(Surface1)
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = OrangeAccent,
                    modifier = Modifier.align(Alignment.Center).size(28.dp),
                    strokeWidth = 2.dp
                )
            } else if (state.entries.isEmpty()) {
                Text(
                    "No data yet.\nTap \"+ Log Weight\" to get started.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                WeightLineChart(entries = state.entries)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Entry list
        if (state.entries.isNotEmpty()) {
            Text("History", style = MaterialTheme.typography.titleSmall.copy(
                color = OnSurface, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, Color(0x14FFFFFF)), RoundedCornerShape(20.dp))
                    .background(Surface1)
            ) {
                Column {
                    state.entries.reversed().forEachIndexed { index, entry ->
                        WeightRow(entry)
                        if (index < state.entries.lastIndex) {
                            HorizontalDivider(color = Color(0xFF2A2A2A),
                                modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Input bottom sheet
    if (state.showInputSheet) {
        WeightInputSheet(
            onDismiss = { viewModel.hideSheet() },
            onSave = { viewModel.logWeight(it) }
        )
    }
}

@Composable
private fun WeightLineChart(entries: List<WeightEntry>) {
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "chartAnim"
    )

    val weights = entries.map { it.weightKg.toFloat() }
    val minW = (weights.min() - 1f).coerceAtLeast(0f)
    val maxW = weights.max() + 1f
    val range = maxW - minW

    val lineColor = OrangeAccent
    val gridColor = Color(0xFF2A2A2A)

    val dateLabels = entries.map { entry ->
        try {
            OffsetDateTime.parse(entry.loggedAt)
                .format(DateTimeFormatter.ofPattern("d MMM"))
        } catch (e: Exception) { "" }
    }

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height - 24.dp.toPx()  // leave room for labels
        val n = entries.size

        // Horizontal grid lines
        listOf(0f, 0.33f, 0.66f, 1f).forEach { frac ->
            val y = h - h * frac
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
        }

        if (n < 2) return@Canvas

        val pts = weights.mapIndexed { i, wt ->
            val x = if (n == 1) w / 2f else w * i / (n - 1).toFloat()
            val y = h - h * ((wt - minW) / range).coerceIn(0f, 1f)
            Offset(x, y)
        }

        // Gradient fill under line
        val visiblePts = pts.map { Offset(it.x, it.y + (h - it.y) * (1f - animProgress)) }
        val path = Path().apply {
            moveTo(visiblePts.first().x, h)
            visiblePts.forEach { lineTo(it.x, it.y) }
            lineTo(visiblePts.last().x, h)
            close()
        }
        drawPath(path, brush = Brush.verticalGradient(
            colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
            startY = 0f, endY = h
        ))

        // Line
        val linePath = Path().apply {
            moveTo(visiblePts.first().x, visiblePts.first().y)
            visiblePts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(linePath, color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Dots
        visiblePts.forEachIndexed { i, pt ->
            drawCircle(lineColor, radius = 4.dp.toPx(), center = pt)
            drawCircle(Color(0xFF1C1C1E), radius = 2.dp.toPx(), center = pt)
        }
    }
}

@Composable
private fun WeightRow(entry: WeightEntry) {
    val date = try {
        OffsetDateTime.parse(entry.loggedAt).format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm"))
    } catch (e: Exception) { entry.loggedAt.take(10) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape)
                    .background(OrangeContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("⚖", fontSize = 16.sp)
            }
            Text(date, style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant))
        }
        Text("%.1f kg".format(entry.weightKg),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = OnSurface, fontWeight = FontWeight.SemiBold))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightInputSheet(onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var input by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface1,
        dragHandle = {
            Box(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp).height(4.dp)
                    .clip(CircleShape).background(Color(0xFF3A3A3A))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Log Weight", style = MaterialTheme.typography.titleMedium.copy(
                color = OnSurface, fontWeight = FontWeight.Bold))

            OutlinedTextField(
                value = input,
                onValueChange = { input = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Weight (kg)") },
                suffix = { Text("kg", color = OnSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = Surface2,
                    focusedLabelColor = OrangeAccent,
                    unfocusedLabelColor = OnSurfaceVariant,
                    cursorColor = OrangeAccent,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    focusedContainerColor = Surface2,
                    unfocusedContainerColor = Surface1
                )
            )

            Button(
                onClick = { input.toDoubleOrNull()?.let { onSave(it) } },
                enabled = input.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeAccent,
                    disabledContainerColor = OrangeAccent.copy(alpha = 0.3f)
                )
            ) {
                Text("Save", style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp))
            }
        }
    }
}
