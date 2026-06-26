package com.calorai.app.ui.screens.log

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorai.app.data.remote.models.FoodItem
import com.calorai.app.ui.theme.*

@Composable
fun LogMealScreen(
    onMealLogged: () -> Unit,
    viewModel: LogMealViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.step) {
        if (uiState.step == LogStep.SUCCESS) {
            kotlinx.coroutines.delay(1400)
            onMealLogged()
            viewModel.reset()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(paddingValues)
            .imePadding()
    ) {
        AnimatedContent(
            targetState = uiState.step,
            transitionSpec = {
                when (targetState) {
                    LogStep.RESULT -> fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 4 } togetherWith
                            fadeOut(tween(200))
                    LogStep.SUCCESS -> fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                    else -> fadeIn(tween(280)) togetherWith fadeOut(tween(200))
                }
            },
            label = "logStep"
        ) { step ->
            when (step) {
                LogStep.INPUT -> InputStep(
                    mealText = uiState.mealText,
                    onTextChange = viewModel::updateMealText,
                    isLoading = uiState.isEstimating,
                    errorMessage = uiState.errorMessage,
                    onSubmit = { focusManager.clearFocus(); viewModel.estimateMeal() }
                )
                LogStep.RESULT -> ResultStep(
                    estimate = uiState.estimate,
                    mealText = uiState.mealText,
                    errorMessage = uiState.errorMessage,
                    onConfirm = { viewModel.confirmAndLog() },
                    onEdit = { viewModel.goBack() }
                )
                LogStep.LOGGING -> AnalyzingStep(message = "Saving your meal…", cycling = false)
                LogStep.SUCCESS -> SuccessStep()
            }
        }

        // ANALYZING overlay — shown while isEstimating is true but step is still INPUT
        if (uiState.isEstimating) {
            AnalyzingStep(message = null, cycling = true)
        }
    }
}

@Composable
private fun InputStep(
    mealText: String,
    onTextChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit
) {
    var fieldFocused by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "underline")
    val underlineAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "underlineAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Log a Meal",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
        )

        Text(
            text = "Describe what you ate and we'll estimate the calories.",
            style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface1),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = mealText,
                    onValueChange = onTextChange,
                    placeholder = {
                        Text(
                            "e.g. \"Two scrambled eggs, toast with butter, orange juice\"",
                            style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceDim)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp)
                        .onFocusChanged { fieldFocused = it.isFocused },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        unfocusedBorderColor = Surface3,
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        cursorColor = OrangeAccent,
                        focusedContainerColor = Surface2,
                        unfocusedContainerColor = Surface1
                    ),
                    maxLines = 8
                )

                // Pulsing orange underline when focused
                if (fieldFocused) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(OrangeAccent.copy(alpha = underlineAlpha))
                    )
                }
            }
        }

        InputQualityHint(mealText = mealText)

        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = mealText.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangeAccent,
                contentColor = Color.White,
                disabledContainerColor = OrangeAccent.copy(alpha = 0.3f),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Analyze",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
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

@Composable
private fun InputQualityHint(mealText: String) {
    val level = inputQualityLevel(mealText)

    val animatedLevel by animateIntAsState(
        targetValue = level,
        animationSpec = tween(300),
        label = "qualityLevel"
    )

    val (label, tip, color) = when {
        mealText.isBlank() -> Triple("", "More detail = more accurate estimate. Include portions, cooking method, and specific foods.", OnSurfaceDim)
        animatedLevel <= 1 -> Triple("Basic", "Try adding portion size — e.g. \"large bowl\" or \"200g\"", Color(0xFFFF6B35).copy(alpha = 0.8f))
        animatedLevel <= 2 -> Triple("Good", "Add cooking method or brand for a tighter range", Color(0xFFFFB347))
        animatedLevel <= 3 -> Triple("Detailed", "Great detail — the AI can estimate this precisely", Color(0xFF66BB6A))
        else -> Triple("Very detailed", "Excellent — this will give the most accurate result", Color(0xFF00C48C))
    }

    val dotCount = if (mealText.isBlank()) 0 else animatedLevel.coerceAtLeast(1)

    AnimatedContent(
        targetState = mealText.isBlank(),
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
        label = "hintVisibility"
    ) { isBlank ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Surface1),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = if (isBlank) OnSurfaceDim else color,
                    modifier = Modifier.size(16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    if (!isBlank && label.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(4) { i ->
                                Box(
                                    modifier = Modifier
                                        .size(width = 18.dp, height = 4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (i < dotCount) color else Surface3)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = color,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyzingStep(message: String?, cycling: Boolean) {
    val messages = listOf(
        "Reading your meal…",
        "Estimating portions…",
        "Calculating calories…"
    )
    var messageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(cycling) {
        if (cycling) {
            while (true) {
                kotlinx.coroutines.delay(1200)
                messageIndex = (messageIndex + 1) % messages.size
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "analyzingArc")
    val arcRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "arcRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = 8.dp.toPx()
                    val inset = strokePx / 2f
                    val arcSize = Size(size.width - strokePx, size.height - strokePx)

                    // Track
                    drawArc(
                        color = Surface2,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                    // Spinning arc
                    drawArc(
                        color = OrangeAccent,
                        startAngle = arcRotation,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            AnimatedContent(
                targetState = if (cycling) messages[messageIndex] else (message ?: ""),
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                },
                label = "analyzingText"
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ResultStep(
    estimate: com.calorai.app.data.remote.models.EstimateResponse?,
    mealText: String,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.clip(CircleShape).background(Surface2).size(40.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = OnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "AI Estimate",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Meal description chip
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = OrangeContainer.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mealText,
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurface),
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        estimate?.let { est ->
            val avgCalories = est.totalCalorieRange.takeIf { it.size >= 2 }
                ?.let { ((it[0] + it[1]) / 2).toInt() } ?: est.totalCalories.toInt()
            val rangeText = est.totalCalorieRange.takeIf { it.size >= 2 }
                ?.let { "${it[0].toInt()}–${it[1].toInt()} kcal range" } ?: ""

            // Big calorie display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface1),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ESTIMATED TOTAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OnSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Animated count-up
                    val animatedCalories by animateIntAsState(
                        targetValue = avgCalories,
                        animationSpec = tween(durationMillis = 600),
                        label = "calorieCountUp"
                    )
                    Text(
                        text = "$animatedCalories",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = OrangeAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 56.sp,
                            letterSpacing = (-2).sp
                        )
                    )
                    Text(
                        text = "kcal",
                        style = MaterialTheme.typography.titleMedium.copy(color = OnSurfaceVariant)
                    )
                    if (rangeText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rangeText,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceDim)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Surface2)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = OnSurfaceDim,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "AI estimates can vary. The range above reflects uncertainty.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnSurfaceDim,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface1),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    est.items.forEachIndexed { index, item ->
                        AnimatedFoodItemRow(item = item, index = index)
                        if (index < est.items.lastIndex) {
                            HorizontalDivider(
                                color = Surface2,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Surface3)
            ) {
                Text("Edit", color = OnSurface)
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeAccent,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Log Meal",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AnimatedFoodItemRow(item: FoodItem, index: Int) {
    val animatedCalories by animateIntAsState(
        targetValue = item.calories.toInt(),
        animationSpec = tween(durationMillis = 600, delayMillis = index * 80),
        label = "foodCalories$index"
    )
    val rangeText = item.calorieRange.takeIf { it.size >= 2 }
        ?.let { "${it[0].toInt()}–${it[1].toInt()}" } ?: "$animatedCalories"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(OrangeAccent)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium.copy(color = OnSurface),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = rangeText,
            style = MaterialTheme.typography.labelMedium.copy(
                color = OrangeAccent,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = " kcal",
            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant)
        )
    }
}

@Composable
private fun SuccessStep() {
    var scaleTarget by remember { mutableStateOf(0f) }
    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "successScale"
    )
    var textVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scaleTarget = 1f
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        textVisible = true
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(OrangeAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(tween(300))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Meal Logged!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Returning to home…",
                        style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant)
                    )
                }
            }
        }
    }
}
