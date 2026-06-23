package com.calorai.app.ui.screens.log

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
            kotlinx.coroutines.delay(1200)
            onMealLogged()
            viewModel.reset()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0)
            .padding(paddingValues)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.step == LogStep.RESULT) {
                IconButton(
                    onClick = { viewModel.goBack() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Surface2)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurface, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = when (uiState.step) {
                    LogStep.INPUT -> "Log a Meal"
                    LogStep.RESULT -> "AI Estimate"
                    LogStep.LOGGING -> "Saving…"
                    LogStep.SUCCESS -> "Saved!"
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            )
        }

        AnimatedContent(
            targetState = uiState.step,
            transitionSpec = {
                fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 3 } togetherWith
                        fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 3 }
            },
            label = "logStep"
        ) { step ->
            when (step) {
                LogStep.INPUT -> InputStep(
                    mealText = uiState.mealText,
                    onTextChange = viewModel::updateMealText,
                    isLoading = uiState.isEstimating,
                    errorMessage = uiState.errorMessage,
                    onSubmit = {
                        focusManager.clearFocus()
                        viewModel.estimateMeal()
                    }
                )
                LogStep.RESULT -> ResultStep(
                    estimate = uiState.estimate,
                    mealText = uiState.mealText,
                    errorMessage = uiState.errorMessage,
                    onConfirm = { viewModel.confirmAndLog() },
                    onRetry = { viewModel.goBack() }
                )
                LogStep.LOGGING -> LoadingStep(message = "Saving your meal…")
                LogStep.SUCCESS -> SuccessStep()
            }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface1),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "What did you eat?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Describe your meal naturally — our AI will estimate the calories.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                        .heightIn(min = 120.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green400,
                        unfocusedBorderColor = Surface3,
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        cursorColor = Green400,
                        focusedContainerColor = Surface2,
                        unfocusedContainerColor = Surface1
                    ),
                    maxLines = 6
                )
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Example prompts
        Text(
            text = "Try something like:",
            style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceDim)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Chicken salad", "Pasta bolognese", "Avocado toast").forEach { example ->
                SuggestionChip(
                    onClick = { onTextChange(example) },
                    label = {
                        Text(
                            example,
                            style = MaterialTheme.typography.labelSmall.copy(color = Green400)
                        )
                    },
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = Green400.copy(alpha = 0.4f)
                    ),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = GreenContainer.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            enabled = mealText.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Green400,
                contentColor = Color(0xFF003314),
                disabledContainerColor = Green400.copy(alpha = 0.3f),
                disabledContentColor = Color(0xFF003314).copy(alpha = 0.5f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color(0xFF003314),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Analysing…", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Estimate Calories", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ResultStep(
    estimate: com.calorai.app.data.remote.models.EstimateResponse?,
    mealText: String,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // Meal description chip
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GreenContainer.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Green400, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mealText,
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurface),
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        estimate?.let { est ->
            // Total calories card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Surface1),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Estimated Total",
                        style = MaterialTheme.typography.labelMedium.copy(color = OnSurfaceVariant, letterSpacing = 1.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(est.totalMin + est.totalMax) / 2} kcal",
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = Green400,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = "${est.totalMin}–${est.totalMax} kcal range",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items breakdown
            Text(
                text = "Breakdown",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            est.items.forEachIndexed { index, item ->
                FoodItemCard(item = item)
                if (index < est.items.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Surface3)
            ) {
                Text("Re-enter", color = OnSurface)
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green400, contentColor = Color(0xFF003314))
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Meal", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FoodItemCard(item: FoodItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Surface2),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Green400)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium.copy(color = OnSurface),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${item.caloriesMin}–${item.caloriesMax}",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Green400,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = " kcal",
                style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant)
            )
        }
    }
}

@Composable
private fun LoadingStep(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Green400, strokeWidth = 3.dp, modifier = Modifier.size(52.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant))
        }
    }
}

@Composable
private fun SuccessStep() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(GreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Green400, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Meal Logged!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Green400,
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
