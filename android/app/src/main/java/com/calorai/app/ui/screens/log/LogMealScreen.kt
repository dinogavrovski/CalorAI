package com.calorai.app.ui.screens.log

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorai.app.data.remote.models.EstimateResponse
import com.calorai.app.data.remote.models.FoodItem
import com.calorai.app.data.remote.models.SavedMeal
import com.calorai.app.ui.components.AppIcons
import com.calorai.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMealScreen(
    onMealLogged: () -> Unit,
    onScanBarcode: () -> Unit = {},
    viewModel: LogMealViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bias by viewModel.calorieBias.collectAsStateWithLifecycle()
    var showSavedMeals by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.step) {
        if (uiState.step == LogStep.SUCCESS) {
            delay(1400)
            onMealLogged()
            viewModel.reset()
        }
    }
    LaunchedEffect(uiState.mealSavedSuccess) {
        if (uiState.mealSavedSuccess) {
            delay(2000)
            viewModel.clearMealSavedSuccess()
        }
    }

    // Saved meals bottom sheet
    if (showSavedMeals) {
        ModalBottomSheet(
            onDismissRequest = { showSavedMeals = false },
            containerColor = Color(0xFF1C1C1E),
            contentColor = OnSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF48484A))
                )
            }
        ) {
            SavedMealsSheet(
                savedMeals = uiState.savedMeals,
                onLog = { meal ->
                    showSavedMeals = false
                    viewModel.quickLogSavedMeal(meal)
                },
                onDelete = { viewModel.deleteSavedMeal(it.id) }
            )
        }
    }

    // Result bottom sheet
    if (uiState.step == LogStep.RESULT && uiState.estimate != null) {
        val resultSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.goBack() },
            sheetState = resultSheetState,
            containerColor = Color(0xFF1C1C1E),
            contentColor = OnSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF48484A))
                )
            }
        ) {
            ResultSheet(
                estimate = uiState.estimate!!,
                mealText = uiState.mealText,
                bias = bias,
                isSavingMeal = uiState.isSavingMeal,
                mealSavedSuccess = uiState.mealSavedSuccess,
                onConfirm = { viewModel.confirmAndLog() },
                onEdit = { viewModel.goBack() },
                onSaveMeal = { viewModel.saveCurrentMeal() }
            )
        }
    }

    // Main screen — always the notes-style input
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(paddingValues)
            .imePadding()
    ) {
        when (uiState.step) {
            LogStep.SUCCESS -> SuccessStep()
            LogStep.LOGGING -> LoggingStep()
            else -> NotesInputScreen(
                mealText = uiState.mealText,
                onTextChange = viewModel::updateMealText,
                isThinking = uiState.isEstimating,
                errorMessage = uiState.errorMessage,
                onSubmit = { viewModel.estimateMeal() },
                onOpenSavedMeals = { showSavedMeals = true },
                hasSavedMeals = uiState.savedMeals.isNotEmpty(),
                onScanBarcode = onScanBarcode
            )
        }
    }
}

// ── Notes-style input screen ─────────────────────────────────────────────────

@Composable
private fun NotesInputScreen(
    mealText: String,
    onTextChange: (String) -> Unit,
    isThinking: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit,
    onOpenSavedMeals: () -> Unit,
    hasSavedMeals: Boolean,
    onScanBarcode: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val qualityLevel = inputQualityLevel(mealText)

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "What did you eat?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = OnSurface,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            )
            if (hasSavedMeals) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Surface1)
                        .clickable { onOpenSavedMeals() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Saved",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Just write it like a note — the more detail, the better.",
            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceDim)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // The notes text field — full width, no border, looks like writing
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (mealText.isEmpty()) {
                Text(
                    text = "e.g. 2/3 chicken bowl with sour cream, cheese, lettuce from Chipotle",
                    style = TextStyle(
                        color = OnSurfaceDim,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 28.sp
                    )
                )
            }
            BasicTextField(
                value = mealText,
                onValueChange = onTextChange,
                textStyle = TextStyle(
                    color = OnSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 28.sp
                ),
                cursorBrush = SolidColor(OrangeAccent),
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                maxLines = 20
            )
        }

        // Thinking indicator — shown inline while processing
        AnimatedVisibility(
            visible = isThinking,
            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
        ) {
            ThinkingIndicator()
        }

        // Quality dots + error
        Column(modifier = Modifier.fillMaxWidth()) {
            if (mealText.isNotBlank() && !isThinking) {
                QualityDots(level = qualityLevel)
                Spacer(modifier = Modifier.height(8.dp))
            }
            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Analyze + Scan row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = mealText.isNotBlank() && !isThinking,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeAccent,
                    contentColor = Color.White,
                    disabledContainerColor = OrangeAccent.copy(alpha = 0.25f),
                    disabledContentColor = Color.White.copy(alpha = 0.4f)
                )
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface1)
                    .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(16.dp))
                    .clickable(onClick = onScanBarcode),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    AppIcons.Barcode,
                    contentDescription = "Scan barcode",
                    tint = OrangeAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse, initialStartOffset = StartOffset(0)),
        label = "d1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse, initialStartOffset = StartOffset(170)),
        label = "d2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse, initialStartOffset = StartOffset(340)),
        label = "d3"
    )

    Row(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Thinking",
            style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(dot1, dot2, dot3).forEach { alpha ->
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent.copy(alpha = alpha))
                )
            }
        }
    }
}

@Composable
private fun QualityDots(level: Int) {
    val colors = listOf(
        Color(0xFFFF6B35), Color(0xFFFFB347), Color(0xFF66BB6A), Color(0xFF00C48C)
    )
    val labels = listOf("Basic", "Good", "Detailed", "Very detailed")
    val idx = (level - 1).coerceIn(0, 3)
    val color = colors[idx]

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        repeat(4) { i ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (i < level) color else Surface3)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = labels[idx],
            style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.SemiBold)
        )
    }
}

// ── Result bottom sheet ──────────────────────────────────────────────────────

@Composable
private fun ResultSheet(
    estimate: EstimateResponse,
    mealText: String,
    bias: Float,
    isSavingMeal: Boolean,
    mealSavedSuccess: Boolean,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onSaveMeal: () -> Unit
) {
    val rawAvg = estimate.totalCalorieRange.takeIf { it.size >= 2 }
        ?.let { (it[0] + it[1]) / 2 } ?: estimate.totalCalories
    val biasedCalories = (rawAvg * (1f + bias)).toInt().coerceAtLeast(1)
    val rawRange = estimate.totalCalorieRange.takeIf { it.size >= 2 }

    // Confidence score 0–100 based on range width relative to total
    val rangeWidth = rawRange?.let { it[1] - it[0] } ?: 0.0
    val confidenceScore = (100 - (rangeWidth / rawAvg * 100).coerceIn(0.0, 100.0)).toInt()
    val confidenceLabel = when {
        confidenceScore >= 75 -> "High"
        confidenceScore >= 50 -> "Medium"
        else -> "Low"
    }
    val confidenceColor = when (confidenceLabel) {
        "High" -> Color(0xFF4CAF50)
        "Medium" -> Color(0xFFFFB347)
        else -> Color(0xFFFF7043)
    }

    val totalProtein = estimate.totalProteinG ?: estimate.items.sumOf { it.proteinG ?: 0.0 }
    val totalCarbs = estimate.totalCarbsG ?: estimate.items.sumOf { it.carbsG ?: 0.0 }
    val totalFat = estimate.totalFatG ?: estimate.items.sumOf { it.fatG ?: 0.0 }
    val hasMacros = totalProtein > 0 || totalCarbs > 0 || totalFat > 0

    // Collect the full thought process from all items that have one
    val allAssumptions = estimate.items.filter { !it.assumption.isNullOrBlank() }

    // Defer heavy content until the sheet finishes sliding up
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(600)
        contentVisible = true
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.88f)
            .verticalScroll(scrollState)
            .navigationBarsPadding()
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Nutrition Details",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                if (estimate.webGrounded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1A3A2A))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Text(
                            text = "Web grounded",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF4CAF50),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Save bookmark
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C2E))
                        .clickable(enabled = !isSavingMeal && !mealSavedSuccess) { onSaveMeal() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSavingMeal) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = OrangeAccent, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            if (mealSavedSuccess) Icons.Default.BookmarkAdded else Icons.Default.BookmarkAdd,
                            contentDescription = "Save meal",
                            tint = if (mealSavedSuccess) OrangeAccent else OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                // Close / edit
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C2E))
                        .clickable { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Edit",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // ── Meal name ────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600, easing = EaseOutCubic)) { it / 4 }
        ) {
            Text(
                text = mealText,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = OnSurface,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 32.sp
                ),
                modifier = Modifier.padding(horizontal = 20.dp),
                maxLines = 3
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Hero card: calories + macros ─────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // 🔥 calories row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔥", fontSize = 22.sp)
                    val animated by animateIntAsState(
                        targetValue = if (contentVisible) biasedCalories else 0,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        ),
                        label = "heroCalories"
                    )
                    Text(
                        text = "$animated",
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = OnSurface,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        )
                    )
                    Text(
                        text = "total calories",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OnSurfaceVariant,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (hasMacros) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF3A3A3C))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Macros row — identical layout to Amy's screenshot
                    Row(modifier = Modifier.fillMaxWidth()) {
                        MacroCell(
                            value = totalProtein,
                            label = "Protein",
                            dotColor = Color(0xFFFFD54F),
                            modifier = Modifier.weight(1f)
                        )
                        MacroCell(
                            value = totalCarbs,
                            label = "Carbs",
                            dotColor = Color(0xFFE57373),
                            modifier = Modifier.weight(1f)
                        )
                        MacroCell(
                            value = totalFat,
                            label = "Fat",
                            dotColor = Color(0xFFBA68C8),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (bias != 0f) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF3A3A3C))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (bias > 0) "Estimate biased +${(bias * 100).toInt()}% (over)" else "Estimate biased ${(bias * 100).toInt()}% (under)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (bias > 0) Color(0xFFFF7043) else Color(0xFF66BB6A)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Items section ────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Items",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = OnSurfaceVariant,
                    letterSpacing = 0.3.sp
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            estimate.items.forEach { item ->
                ResultItemRow(item = item, bias = bias)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── AI's thought process section ─────────────────────────────────────
        if (allAssumptions.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "AI's thought process",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = OnSurfaceVariant,
                        letterSpacing = 0.3.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Confidence ring + label
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            ConfidenceRing(score = confidenceScore, color = confidenceColor)
                            Column {
                                Text(
                                    text = "Confidence level",
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceDim)
                                )
                                Text(
                                    text = confidenceLabel,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = confidenceColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // All assumptions concatenated
                        Text(
                            text = allAssumptions.joinToString(" ") { it.assumption ?: "" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OnSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Something off? Tap to edit →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.clickable { onEdit() }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── References section ───────────────────────────────────────────────
        ReferencesSection(sources = estimate.sources)

        Spacer(modifier = Modifier.height(16.dp))

        // ── Log button ───────────────────────────────────────────────────────
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent, contentColor = Color.White)
        ) {
            Text(
                "Log Meal",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun MacroCell(value: Double, label: String, dotColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${"%.1f".format(value)} g",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = OnSurface,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant)
            )
        }
    }
}

@Composable
private fun ConfidenceRing(score: Int, color: Color) {
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(750); triggered = true }
    val animatedScore by animateFloatAsState(
        targetValue = if (triggered) score.toFloat() else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "confRing"
    )
    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 5.dp.toPx()
            val inset = strokePx / 2
            // Track
            drawArc(
                color = Color(0xFF3A3A3C),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.width - strokePx, size.height - strokePx),
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            // Progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * (animatedScore / 100f),
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.width - strokePx, size.height - strokePx),
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        Text(
            text = "$score",
            style = MaterialTheme.typography.labelLarge.copy(
                color = OnSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        )
    }
}

@Composable
private fun ResultItemRow(item: FoodItem, bias: Float) {
    var expanded by remember { mutableStateOf(false) }
    val hasDetails = !item.assumption.isNullOrBlank()

    val biasedCalories = (item.calories * (1f + bias)).toInt()
    val rawRange = item.calorieRange.takeIf { it.size >= 2 }
    val calText = rawRange?.let { "${(it[0] * (1f + bias)).toInt()}–${(it[1] * (1f + bias)).toInt()}" }
        ?: "$biasedCalories"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF2A2A2A))
            .then(if (hasDetails) Modifier.clickable { expanded = !expanded } else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = OnSurface,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$calText cal",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            )
            if (hasDetails) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = OnSurfaceDim,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = expanded && hasDetails,
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(180))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                item.estimatedGrams?.let { g ->
                    Text(
                        text = "Estimated: ${g.toInt()}g",
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceDim)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = item.assumption ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OnSurfaceVariant,
                        lineHeight = 19.sp
                    )
                )
            }
        }
    }
}

// ── References section ───────────────────────────────────────────────────────

@Composable
private fun ReferencesSection(sources: List<String>) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    // Parse each source string into title + url.
    // Backend format: "Page Title — https://example.com/path"
    data class SourceLink(val title: String, val url: String, val domain: String)

    val links: List<SourceLink> = sources.mapNotNull { raw ->
        val separator = " — "
        val idx = raw.lastIndexOf(separator)
        if (idx != -1) {
            val title = raw.substring(0, idx).trim()
            val url = raw.substring(idx + separator.length).trim()
            val domain = runCatching {
                java.net.URI(url).host?.removePrefix("www.") ?: url
            }.getOrDefault(url)
            SourceLink(title, url, domain)
        } else null
    }

    if (links.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "References",
            style = MaterialTheme.typography.labelMedium.copy(
                color = OnSurfaceVariant,
                letterSpacing = 0.3.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column {
                links.forEachIndexed { i, link ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri(link.url) }
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = link.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OnSurface,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                ),
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = link.domain,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OrangeAccent,
                                    fontSize = 10.sp
                                )
                            )
                        }
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = "Open link",
                            tint = OnSurfaceDim,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (i < links.lastIndex) {
                        HorizontalDivider(
                            color = Color(0xFF3A3A3C),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Saved meals sheet ────────────────────────────────────────────────────────

@Composable
private fun SavedMealsSheet(
    savedMeals: List<SavedMeal>,
    onLog: (SavedMeal) -> Unit,
    onDelete: (SavedMeal) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = savedMeals.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Saved Meals",
            style = MaterialTheme.typography.titleMedium.copy(
                color = OnSurface,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search meals…", color = OnSurfaceDim) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceDim, modifier = Modifier.size(18.dp))
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = Color(0xFF3A3A3C),
                focusedTextColor = OnSurface,
                unfocusedTextColor = OnSurface,
                cursorColor = OrangeAccent,
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF232323)
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No saved meals yet.\nLog a meal and tap 🔖 to save it.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { meal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2A2A2A))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = meal.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = OnSurface, fontWeight = FontWeight.Medium
                                ),
                                maxLines = 2
                            )
                            Text(
                                text = "${meal.calories.toInt()} cal",
                                style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { onDelete(meal) }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = OnSurfaceDim,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = { onLog(meal) },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) {
                            Text(
                                "Log",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White, fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ── Utility screens ──────────────────────────────────────────────────────────

@Composable
private fun LoggingStep() {
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = OrangeAccent, strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(20.dp))
            Text("Saving meal…", style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant))
        }
    }
}

@Composable
private fun SuccessStep() {
    var scaleTarget by remember { mutableStateOf(0f) }
    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "successScale"
    )
    var textVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scaleTarget = 1f
        delay(300)
        textVisible = true
    }

    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(OrangeAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            }
            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Meal logged!",
                        style = MaterialTheme.typography.titleLarge.copy(color = OnSurface, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your calories have been updated",
                        style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant)
                    )
                }
            }
        }
    }
}

private fun inputQualityLevel(text: String): Int {
    if (text.isBlank()) return 0
    var score = 0
    val lower = text.lowercase()
    if (text.length > 20) score++
    if (Regex("""\d""").containsMatchIn(text)) score++
    if (Regex("""(gram|g\b|ml|oz|cup|slice|piece|bowl|plate|serving|large|small|medium|big|half|whole)""").containsMatchIn(lower)) score++
    if (Regex("""(fried|grilled|baked|boiled|steamed|raw|roasted|scrambled|poached|sautéed|sauteed)""").containsMatchIn(lower)) score++
    if (text.split(",").size > 1 || text.split(" and ").size > 1) score++
    return score.coerceIn(0, 4)
}
