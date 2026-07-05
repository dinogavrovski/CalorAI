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
import androidx.compose.ui.layout.Layout
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
                WeightLineChart(allEntries = state.entries, period = state.period)
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
private fun WeightLineChart(allEntries: List<WeightEntry>, period: String) {
    // Collapse to one point per day, keeping the last value logged that day
    val entries = collapseDaily(allEntries)

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "chartAnim"
    )

    val weights = entries.map { it.weightKg.toFloat() }
    if (weights.isEmpty()) return
    val minW = (weights.min() - 1f).coerceAtLeast(0f)
    val maxW = weights.max() + 1f
    val range = (maxW - minW).coerceAtLeast(1f)

    val lineColor = OrangeAccent
    val gridColor = Color(0xFF2A2A2A)

    // Fixed time axis (MyFitnessPal style): x-position maps to actual date,
    // and tick labels are fixed intervals (days / weeks / 12 months).
    val axis = buildTimeAxis(entries, period)
    val points = axis.points   // list of (x-fraction 0..1, weight)
    val ticks = axis.ticks     // list of (x-fraction 0..1, label)
    val tickFont = if (period == "year") 8.sp else 9.sp

    // Y-axis (kg) reference values, top → bottom
    val yValues = listOf(maxW, minW + range * 0.66f, minW + range * 0.33f, minW)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            // Y-axis kg labels
            Column(
                modifier = Modifier.fillMaxHeight().padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                yValues.forEach { v ->
                    Text(
                        "%.0f".format(v),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OnSurfaceVariant, fontSize = 9.sp
                        )
                    )
                }
            }

            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxHeight().weight(1f)
            ) {
                val w = size.width
                val chartH = size.height

                listOf(0f, 0.33f, 0.66f, 1f).forEach { frac ->
                    val y = chartH - chartH * frac
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
                }

                if (points.isEmpty()) return@Canvas

                val pts = points.map { (fx, wt) ->
                    val x = fx * w
                    val y = chartH - chartH * ((wt - minW) / range).coerceIn(0f, 1f)
                    Offset(x, y)
                }

                // Single data point → centered dot, no line
                if (pts.size == 1) {
                    val p = pts[0]
                    drawCircle(lineColor, radius = 5.dp.toPx(), center = p)
                    drawCircle(Color(0xFF1C1C1E), radius = 2.5.dp.toPx(), center = p)
                    return@Canvas
                }

                val visiblePts = pts.map { Offset(it.x, it.y + (chartH - it.y) * (1f - animProgress)) }

                val fillPath = Path().apply {
                    moveTo(visiblePts.first().x, chartH)
                    visiblePts.forEach { lineTo(it.x, it.y) }
                    lineTo(visiblePts.last().x, chartH)
                    close()
                }
                drawPath(fillPath, brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f, endY = chartH
                ))

                val linePath = Path().apply {
                    moveTo(visiblePts.first().x, visiblePts.first().y)
                    visiblePts.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(linePath, color = lineColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                visiblePts.forEach { pt ->
                    drawCircle(lineColor, radius = 4.dp.toPx(), center = pt)
                    drawCircle(Color(0xFF1C1C1E), radius = 2.dp.toPx(), center = pt)
                }
            }
        }

        if (ticks.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            // start padding must match the y-axis column width so labels align to the canvas
            val labelMod = Modifier.fillMaxWidth().padding(start = 32.dp)
            if (period == "year") {
                // 12 labels → angle them so they never overlap
                AngledXAxisLabels(
                    fractions = ticks.map { it.first },
                    labels = ticks.map { it.second },
                    fontSize = tickFont,
                    modifier = labelMod
                )
            } else {
                XAxisLabels(
                    fractions = ticks.map { it.first },
                    labels = ticks.map { it.second },
                    fontSize = tickFont,
                    modifier = labelMod
                )
            }
        }
    }
}

private class TimeAxis(
    val points: List<Pair<Float, Float>>,   // (x-fraction 0..1, weight kg)
    val ticks: List<Pair<Float, String>>    // (x-fraction 0..1, label)
)

/** Builds a fixed date-based x-axis ending today, MyFitnessPal-style. */
private fun buildTimeAxis(entries: List<WeightEntry>, period: String): TimeAxis {
    val today = java.time.LocalDate.now()
    val daysBetween = { a: java.time.LocalDate, b: java.time.LocalDate ->
        java.time.temporal.ChronoUnit.DAYS.between(a, b).toFloat()
    }

    val start: java.time.LocalDate
    val ticks: List<Pair<Float, String>>

    when (period) {
        "week" -> {
            start = today.minusDays(6)                       // Mon..Sun (last 7 days)
            val span = 6f
            ticks = (0..6).map { i ->
                val d = start.plusDays(i.toLong())
                (i / span) to d.format(DateTimeFormatter.ofPattern("EEE"))
            }
        }
        "month" -> {
            start = today.minusDays(28)                      // last 4 weeks
            val span = 28f
            ticks = (0..4).map { wk ->
                val d = start.plusDays((wk * 7).toLong())
                ((wk * 7) / span) to d.format(DateTimeFormatter.ofPattern("d MMM"))
            }
        }
        else -> {                                            // year: 12 months as M/yy (e.g. 8/25)
            start = today.withDayOfMonth(1).minusMonths(11)
            val span = daysBetween(start, today).coerceAtLeast(1f)
            ticks = (0..11).map { m ->
                val d = start.plusMonths(m.toLong())
                (daysBetween(start, d) / span).coerceIn(0f, 1f) to
                    "${d.monthValue}/${d.format(DateTimeFormatter.ofPattern("yy"))}"  // 8/25
            }
        }
    }

    val span = daysBetween(start, today).coerceAtLeast(1f)
    val points = entries.mapNotNull { e ->
        val d = parseLoggedDate(e.loggedAt) ?: return@mapNotNull null
        val frac = (daysBetween(start, d) / span).coerceIn(0f, 1f)
        frac to e.weightKg.toFloat()
    }

    return TimeAxis(points, ticks)
}

/** Robustly parses a logged_at string down to a LocalDate. */
private fun parseLoggedDate(raw: String): java.time.LocalDate? = try {
    OffsetDateTime.parse(raw).toLocalDate()
} catch (e: Exception) {
    try {
        java.time.LocalDateTime.parse(raw).toLocalDate()
    } catch (e2: Exception) {
        try { java.time.LocalDate.parse(raw.take(10)) } catch (e3: Exception) { null }
    }
}

/** One entry per calendar day (keeping the last logged that day), sorted oldest → newest. */
private fun collapseDaily(entries: List<WeightEntry>): List<WeightEntry> =
    entries
        .sortedBy { it.loggedAt }          // ISO timestamps sort chronologically
        .groupBy { it.loggedAt.take(10) }  // group by yyyy-MM-dd
        .map { (_, sameDay) -> sameDay.last() }

/** Robustly parses the many datetime string shapes the backend may return. */
private fun formatLoggedAt(raw: String, pattern: String): String {
    val fmt = DateTimeFormatter.ofPattern(pattern)
    // Try full offset datetime, then local datetime, then plain date
    return try {
        OffsetDateTime.parse(raw).format(fmt)
    } catch (e: Exception) {
        try {
            java.time.LocalDateTime.parse(raw).format(fmt)
        } catch (e2: Exception) {
            try {
                java.time.LocalDate.parse(raw.take(10)).format(fmt)
            } catch (e3: Exception) { "" }
        }
    }
}

/** Places each label horizontally centered on its data point's x-fraction (0f..1f). */
@Composable
private fun XAxisLabels(
    fractions: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 9.sp
) {
    Layout(
        modifier = modifier,
        content = {
            labels.forEach { text ->
                Text(
                    text,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OnSurfaceVariant, fontSize = fontSize
                    )
                )
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(constraints.maxWidth, height) {
            placeables.forEachIndexed { i, p ->
                val frac = fractions.getOrElse(i) { 0f }
                val x = (frac * constraints.maxWidth - p.width / 2f).toInt()
                    .coerceIn(0, (constraints.maxWidth - p.width).coerceAtLeast(0))
                p.placeRelative(x, 0)
            }
        }
    }
}

/** Like XAxisLabels but each label is rotated, so many labels (e.g. 12 months) never overlap. */
@Composable
private fun AngledXAxisLabels(
    fractions: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    angleDeg: Float = -38f,
    fontSize: androidx.compose.ui.unit.TextUnit = 9.sp
) {
    Layout(
        modifier = modifier,
        content = {
            labels.forEach { text ->
                Text(
                    text,
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OnSurfaceVariant, fontSize = fontSize
                    )
                )
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(androidx.compose.ui.unit.Constraints()) }
        val maxW = placeables.maxOfOrNull { it.width } ?: 0
        val maxH = placeables.maxOfOrNull { it.height } ?: 0
        val rad = Math.toRadians(kotlin.math.abs(angleDeg).toDouble())
        // vertical space the rotated text occupies
        val boxH = (maxW * kotlin.math.sin(rad) + maxH * kotlin.math.cos(rad)).toInt()
        layout(constraints.maxWidth, boxH) {
            placeables.forEachIndexed { i, p ->
                val frac = fractions.getOrElse(i) { 0f }
                val cx = frac * constraints.maxWidth
                // rotate about the label's own center, centered vertically → nothing clipped
                p.placeRelativeWithLayer(
                    x = (cx - p.width / 2f).toInt(),
                    y = ((boxH - p.height) / 2f).toInt()
                ) {
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                    rotationZ = angleDeg
                }
            }
        }
    }
}

@Composable
private fun WeightRow(entry: WeightEntry) {
    val date = formatLoggedAt(entry.loggedAt, "d MMM yyyy").ifEmpty { entry.loggedAt.take(10) }

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
