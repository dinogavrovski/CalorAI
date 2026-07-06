@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.calorai.app.ui.screens.barcode

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorai.app.data.remote.models.BarcodeProduct
import com.calorai.app.data.remote.models.LoggedBarcodeItem
import com.calorai.app.ui.theme.*
import kotlinx.coroutines.delay

private val CardBorder = BorderStroke(1.dp, Color(0x14FFFFFF))

@Composable
fun ProductDetailScreen(
    barcode: String,
    source: String = "nav",
    onDismiss: () -> Unit,
    onProductLogged: () -> Unit,
    onAddToNote: (LoggedBarcodeItem) -> Unit = {},
    viewModel: BarcodeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(barcode) {
        viewModel.lookupBarcode(barcode)
    }

    LaunchedEffect(uiState.logSuccess) {
        if (uiState.logSuccess) {
            delay(900)
            onProductLogged()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF111111)), endY = 1200f))
    ) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator(color = OrangeAccent, strokeWidth = 2.dp)
                        Text("Looking up product…", color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(32.dp)) {
                        Text("Product not found", color = OnSurface, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(uiState.error ?: "", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)) {
                            Text("Go Back")
                        }
                    }
                }
            }
            uiState.product != null -> {
                ProductContent(
                    product = uiState.product!!,
                    servings = uiState.servings,
                    onServingsChange = viewModel::setServings,
                    isLogging = uiState.isLogging,
                    logSuccess = uiState.logSuccess,
                    source = source,
                    onLog = { viewModel.logProduct(it) },
                    onAddToNote = onAddToNote,
                    onDismiss = onDismiss
                )
            }
        }

        // Top bar (always visible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = OnSurface, modifier = Modifier.size(18.dp))
            }
            Text(
                "Nutrition Info",
                style = MaterialTheme.typography.titleMedium.copy(color = OnSurface, fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.size(40.dp))
        }
    }
}

@Composable
private fun ProductContent(
    product: BarcodeProduct,
    servings: Float,
    onServingsChange: (Float) -> Unit,
    isLogging: Boolean,
    logSuccess: Boolean,
    source: String = "nav",
    onLog: (Float) -> Unit,
    onAddToNote: (LoggedBarcodeItem) -> Unit = {},
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    var servingsText by remember { mutableStateOf("1") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 72.dp, bottom = 24.dp)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Product header ──────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (product.brand != null) {
                Text(
                    product.brand.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OrangeAccent, letterSpacing = 1.sp, fontWeight = FontWeight.Bold
                    )
                )
            }
            Text(
                product.name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = OnSurface, fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ── Per serving hero card ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E1E1E))
                .border(CardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${product.caloriesPerServing.toInt()}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = OrangeAccent, fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        "kcal per serving (${product.servingSizeG.toInt()}g)",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    MacroHero("Protein", product.proteinG, Color(0xFF4FC3F7))
                    MacroHero("Carbs", product.carbsG, Color(0xFFFFB74D))
                    MacroHero("Fat", product.fatG, Color(0xFFEF9A9A))
                }
            }
        }

        // ── Serving section ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E1E1E))
                .border(CardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "SERVING",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = OnSurfaceVariant, letterSpacing = 1.sp, fontWeight = FontWeight.Bold
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Serving size", style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant))
                Text(
                    product.servingDescription ?: "${product.servingSizeG.toInt()}g",
                    style = MaterialTheme.typography.bodyMedium.copy(color = OnSurface, fontWeight = FontWeight.SemiBold)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Number of servings", style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant))
                BasicTextField(
                    value = servingsText,
                    onValueChange = { raw ->
                        servingsText = raw
                        raw.toFloatOrNull()?.let { v ->
                            if (v > 0f) onServingsChange(v.coerceAtMost(99f))
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = OnSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    ),
                    cursorBrush = SolidColor(OrangeAccent),
                    modifier = Modifier
                        .width(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2A2A2A))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

        }

        // ── Nutrition facts per serving ──────────────────────────────
        val servingLabel = product.servingDescription ?: "${product.servingSizeG.toInt()}g"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E1E1E))
                .border(CardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                "NUTRITION FACTS · PER $servingLabel".uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = OnSurfaceVariant, letterSpacing = 1.sp, fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF2A2A2A))
            Spacer(Modifier.height(8.dp))

            NutritionRow("Calories", "${product.caloriesPerServing.toInt()} kcal", isHeader = true)
            NutritionDivider()
            NutritionRow("Protein", "%.1fg".format(product.proteinG), color = Color(0xFF4FC3F7))
            NutritionDivider()
            NutritionRow("Carbohydrates", "%.1fg".format(product.carbsG), color = Color(0xFFFFB74D))
            NutritionDivider()
            NutritionRow("Fat", "%.1fg".format(product.fatG), color = Color(0xFFEF9A9A))
        }

        // ── Action button ────────────────────────────────────────────
        Spacer(Modifier.height(4.dp))
        if (source == "log") {
            // Came from LogMeal note — hand the exact scanned item back to the note (bypasses AI)
            Button(
                onClick = {
                    val servingLabel = if (servings == 1f) ""
                        else "${if (servings == servings.toLong().toFloat()) servings.toLong().toString() else "%.1f".format(servings)}x "
                    onAddToNote(
                        LoggedBarcodeItem(
                            name = "$servingLabel${product.name}",
                            calories = product.caloriesPerServing * servings,
                            proteinG = product.proteinG * servings,
                            carbsG = product.carbsG * servings,
                            fatG = product.fatG * servings
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent, contentColor = Color.White)
            ) {
                Text("Add to Note", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
        } else {
            Button(
                onClick = { onLog(servings) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLogging && !logSuccess,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (logSuccess) Color(0xFF4CAF50) else OrangeAccent,
                    contentColor = Color.White,
                    disabledContainerColor = if (logSuccess) Color(0xFF4CAF50) else OrangeAccent.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                if (isLogging) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (logSuccess) "✓ Added to diary!" else "Add to Diary",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroHero(label: String, value: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            "%.0fg".format(value),
            style = MaterialTheme.typography.titleMedium.copy(color = color, fontWeight = FontWeight.Bold)
        )
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceDim, fontSize = 10.sp))
    }
}


@Composable
private fun NutritionRow(
    label: String,
    value: String,
    isHeader: Boolean = false,
    color: Color = OnSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!isHeader && color != OnSurface) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            }
            Text(
                label,
                style = if (isHeader)
                    MaterialTheme.typography.bodyLarge.copy(color = OnSurface, fontWeight = FontWeight.Bold)
                else
                    MaterialTheme.typography.bodyMedium.copy(color = OnSurface)
            )
        }
        Text(
            value,
            style = if (isHeader)
                MaterialTheme.typography.bodyLarge.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
            else
                MaterialTheme.typography.bodyMedium.copy(color = OnSurface, fontWeight = FontWeight.SemiBold),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun NutritionDivider() {
    HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 0.5.dp)
}
