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
import androidx.compose.animation.core.*
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.calorai.app.ui.theme.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(
    onDismiss: () -> Unit,
    onBarcodeFound: (String) -> Unit
) {
    val context = LocalContext.current
    var navigated by remember { mutableStateOf(false) }

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
                    if (!navigated) {
                        navigated = true
                        onBarcodeFound(barcode)
                    }
                }
            )
            ScanOverlay()
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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

        // Hint label
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(200.dp))
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

        drawRect(Color.Black.copy(alpha = 0.55f))
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(frameW, frameH),
            cornerRadius = CornerRadius(16.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        val cornerLen = 28.dp.toPx()
        val strokeW = 3.dp.toPx()
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

        val lineY = top + scanY * frameH
        drawLine(
            color = OrangeAccent.copy(alpha = 0.7f),
            start = Offset(left + 8.dp.toPx(), lineY),
            end = Offset(left + frameW - 8.dp.toPx(), lineY),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}
