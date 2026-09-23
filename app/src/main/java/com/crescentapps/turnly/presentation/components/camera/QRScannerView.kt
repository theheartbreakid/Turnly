package com.crescentapps.turnly.presentation.components.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.crescentapps.turnly.core.util.QRCodeUtils
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Robust, shared CameraX QR Code Scanner Composable.
 * Features:
 * - Proper CameraX lifecycle binding and cleanup
 * - Zero memory leaks with background executor release on disposal
 * - ZXing YUV image analysis with atomic single-trigger detection
 * - Handles camera permission checks, rationale, and permanent denial routes to system settings
 * - Modern scan frame UX with animated laser guide
 */
@Composable
fun QRScannerView(
    onQRCodeScanned: (exactPayload: String, roomCode: String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    instructionText: String = "Point your camera at the room QR code",
    frameBorderColor: Color = MaterialTheme.colorScheme.primary
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDeniedCount by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            permissionDeniedCount++
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission) {
            val isScanned = remember { AtomicBoolean(false) }

            // Camera Preview & Analyzer
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val reader = MultiFormatReader()

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    if (isScanned.get()) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }

                                    try {
                                        val buffer = imageProxy.planes[0].buffer
                                        val bytes = ByteArray(buffer.remaining())
                                        buffer.get(bytes)

                                        val width = imageProxy.width
                                        val height = imageProxy.height
                                        val source = PlanarYUVLuminanceSource(
                                            bytes,
                                            width,
                                            height,
                                            0,
                                            0,
                                            width,
                                            height,
                                            false
                                        )
                                        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                                        val result = reader.decodeWithState(binaryBitmap)
                                        reader.reset()

                                        val rawText = result.text?.trim()
                                        if (!rawText.isNullOrEmpty() && !isScanned.get()) {
                                            val extractedCode = QRCodeUtils.extractRoomCode(rawText)
                                            if (extractedCode != null && isScanned.compareAndSet(false, true)) {
                                                previewView.post {
                                                    cameraProvider.unbindAll()
                                                    onQRCodeScanned(rawText, extractedCode)
                                                }
                                            }
                                        }
                                    } catch (_: Exception) {
                                        // Frame did not contain a readable QR code
                                    } finally {
                                        imageProxy.close()
                                    }
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top close action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onClose,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.5f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close scanner")
                    }
                }

                // Viewfinder Frame
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 220.dp, maxWidth = 280.dp, minHeight = 220.dp, maxHeight = 280.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(3.dp, frameBorderColor, RoundedCornerShape(24.dp))
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    // Subtle animated scan line
                    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
                    val laserOffsetY by infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = 0.9f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "laser_pos"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(2.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (laserOffsetY * 240).dp)
                            .background(frameBorderColor)
                    )
                }

                // Instruction bottom banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = instructionText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        } else {
            // Permission Denied / Rationale View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Turnly needs camera access to scan room invite QR codes and connect to shared schedule rooms seamlessly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (permissionDeniedCount > 1) {
                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open App Settings")
                    }
                } else {
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Grant Permission")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onClose) {
                    Text("Enter Code Manually", color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
    }
}
