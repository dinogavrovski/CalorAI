@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.calorai.app.ui.screens.barcode

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorai.app.data.remote.models.BarcodeProduct
import com.calorai.app.ui.theme.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(
    onDismiss: () -> Unit,
    onProductLogged: () -> Unit,
    viewModel: BarcodeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            CameraPreview(
                onBarcodeDetected = { barcode ->
                    if (uiState.product == null && !uiState.isLoading) {
                        viewModel.lookupBarcode(barcode)
                    }
                }
            )
            ScanOverlay()
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Camera permission required", color = Color.White, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                    ) { Text("Grant Permission") }
                }
            }
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text(
                "Scan Barcode",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.size(40.dp))
        }

        // Status label below scan frame
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(200.dp))
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = OrangeAccent, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else if (uiState.error != null) {
                    Text(
                        uiState.error!!,
                        color = Color(0xFFFF5252),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                } else {
                    Text(
                        "Point camera at a barcode",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Product result sheet
        AnimatedVisibility(
            visible = uiState.product != null,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut()
        ) {
            uiState.product?.let { product ->
                ProductResultSheet(
                    product = product,
                    servings = uiState.servings,
                    onServingsChange = viewModel::setServings,
                    isLogging = uiState.isLogging,
                    logSuccess = uiState.logSuccess,
                    onLog = { viewModel.logProduct(it) },
                    onDismiss = { viewModel.clearProduct() }
                )
            }
        }

        LaunchedEffect(uiState.logSuccess) {
            if (uiState.logSuccess) {
                kotlinx.coroutines.delay(1000)
                onProductLogged()
            }
        }
    }
}

@Composable
private fun CameraPreview(onBarcodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember { BarcodeScanning.getClient() }
    var lastScanned by remember { mutableStateOf("") }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor) { imageProxy ->
                            processImageProxy(scanner, imageProxy) { rawValue ->
                                if (rawValue != lastScanned) {
                                    lastScanned = rawValue
                                    onBarcodeDetected(rawValue)
                                }
                            }
                        }
                    }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer)
                } catch (e: Exception) {
                    Log.e("BarcodeScan", "Camera bind failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onResult: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull { it.valueType == Barcode.TYPE_PRODUCT || it.rawValue != null }
                    ?.rawValue?.let(onResult)
            }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}

@Composable
private fun ScanOverlay() {
    val scanLineAnim = rememberInfiniteTransition(label = "scanLine")
    val scanY by scanLineAnim.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "scanY"
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val frameW = size.width * 0.72f
        val frameH = frameW * 0.55f
        val left = (size.width - frameW) / 2f
        val top = (size.height - frameH) / 2f - size.height * 0.05f

        // Dark overlay with hole
        drawRect(Color.Black.copy(alpha = 0.55f))
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(frameW, frameH),
            cornerRadius = CornerRadius(16.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // Orange corner brackets
        val cornerLen = 28.dp.toPx()
        val strokeW = 3.dp.toPx()
        val r = 12.dp.toPx()
        val corners = listOf(
            Offset(left, top) to Pair(1f, 1f),
            Offset(left + frameW, top) to Pair(-1f, 1f),
            Offset(left, top + frameH) to Pair(1f, -1f),
            Offset(left + frameW, top + frameH) to Pair(-1f, -1f)
        )
        for ((corner, dir) in corners) {
            val (dx, dy) = dir
            drawLine(OrangeAccent, corner, Offset(corner.x + dx * cornerLen, corner.y), strokeWidth = strokeW)
            drawLine(OrangeAccent, corner, Offset(corner.x, corner.y + dy * cornerLen), strokeWidth = strokeW)
        }

        // Animated scan line
        val lineY = top + scanY * frameH
        drawLine(
            color = OrangeAccent.copy(alpha = 0.7f),
            start = Offset(left + 8.dp.toPx(), lineY),
            end = Offset(left + frameW - 8.dp.toPx(), lineY),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

@Composable
private fun ProductResultSheet(
    product: BarcodeProduct,
    servings: Float,
    onServingsChange: (Float) -> Unit,
    isLogging: Boolean,
    logSuccess: Boolean,
    onLog: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    // Scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss)
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF1C1C1E))
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF48484A))
                    .align(Alignment.CenterHorizontally)
            )
            // Product header
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = OnSurface, fontWeight = FontWeight.Bold
                    )
                )
                if (product.brand != null) {
                    Text(
                        product.brand,
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                }
            }

            // Calorie hero
            val totalCal = (product.caloriesPerServing * servings).toInt()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(OrangeAccent.copy(alpha = 0.12f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "$totalCal",
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = OrangeAccent, fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text("kcal", style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant))
                    }
                    // Macros
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        MacroCol("Protein", "%.0fg".format(product.proteinG * servings), Color(0xFF4FC3F7))
                        MacroCol("Carbs", "%.0fg".format(product.carbsG * servings), Color(0xFFFFB74D))
                        MacroCol("Fat", "%.0fg".format(product.fatG * servings), Color(0xFFEF9A9A))
                    }
                }
            }

            // Serving adjuster
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Servings",
                        style = MaterialTheme.typography.labelMedium.copy(color = OnSurfaceVariant)
                    )
                    Text(
                        "%.1f × ${product.servingDescription ?: "${product.servingSizeG}g"}".format(servings),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = OnSurface, fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (servings > 0.5f) onServingsChange((servings - 0.5f).coerceAtLeast(0.5f)) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Surface1)
                    ) {
                        Text("−", style = MaterialTheme.typography.titleMedium.copy(color = OnSurface))
                    }
                    Slider(
                        value = servings,
                        onValueChange = onServingsChange,
                        valueRange = 0.5f..5f,
                        steps = 8,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = OrangeAccent,
                            activeTrackColor = OrangeAccent,
                            inactiveTrackColor = Surface1
                        )
                    )
                    IconButton(
                        onClick = { if (servings < 5f) onServingsChange((servings + 0.5f).coerceAtMost(5f)) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Surface1)
                    ) {
                        Text("+", style = MaterialTheme.typography.titleMedium.copy(color = OnSurface))
                    }
                }
            }

            // Add to diary button
            Button(
                onClick = { onLog(servings) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLogging && !logSuccess,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (logSuccess) Color(0xFF4CAF50) else OrangeAccent,
                    contentColor = Color.White,
                    disabledContainerColor = OrangeAccent.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                if (isLogging) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (logSuccess) "✓ Added!" else "Add to Diary",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroCol(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.titleSmall.copy(color = color, fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceDim, fontSize = 10.sp))
    }
}
