package com.coptimize.openinventory.ui.screens.product

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.coptimize.openinventory.ui.screens.sale.EmbeddedScanner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private enum class EnrollmentStep { Barcode, Photos }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEnrollmentCameraScreen(
    onDismiss: () -> Unit,
    onComplete: (String?, List<Uri>) -> Unit
) {
    var step by remember { mutableStateOf(EnrollmentStep.Barcode) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var capturedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (step == EnrollmentStep.Barcode) "Step 1: Scan Barcode" else "Step 2: Take Photos") },
                navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close") } }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (step) {
                EnrollmentStep.Barcode -> {
                    // Ensure EmbeddedScanner has its own onDispose logic to release the camera
                    EmbeddedScanner { barcode ->
                        scannedBarcode = barcode
                        step = EnrollmentStep.Photos
                    }
                    Button(
                        onClick = { step = EnrollmentStep.Photos },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                    ) {
                        Text("Skip")
                    }
                }
                EnrollmentStep.Photos -> {
                    CameraCaptureView(
                        imageUris = capturedImageUris,
                        onImageCaptured = { uri ->
                            if (capturedImageUris.size < 6) {
                                capturedImageUris = capturedImageUris + uri
                            }
                        },
                        onComplete = { onComplete(scannedBarcode, capturedImageUris) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraCaptureView(
    imageUris: List<Uri>,
    onImageCaptured: (Uri) -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // We remember these to persist across recompositions
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = remember { CameraSelector.DEFAULT_BACK_CAMERA }

    // This effect handles the CameraX lifecycle within the Composable
    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Unbind any previous use cases before rebinding
            cameraProvider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, executor)

        // CLEANUP: When this Composable is removed (dialog closed or step changed), unbind the camera
        onDispose {
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                // Provider might not have initialized yet, suppress
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thumbnails of captured images
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                items(imageUris) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Captured image",
                        modifier = Modifier
                            .size(64.dp)
                            .padding(4.dp)
                            .border(2.dp, Color.White),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Spacer(modifier = Modifier.size(64.dp))
                IconButton(
                    onClick = {
                        if (imageUris.size < 6) {
                            takePhoto(context, imageCapture, onImageCaptured)
                        }
                    },
                    modifier = Modifier.size(64.dp).background(Color.White, CircleShape),
                    enabled = imageUris.size < 6
                ) {
                    Icon(Icons.Default.Camera, "Take Photo", tint = Color.Black)
                }

                Button(onClick = onComplete, enabled = imageUris.isNotEmpty()) {
                    Text("Done")
                }
            }
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    // Create specific output directory
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
        File(it, "OpenInventory").apply { mkdirs() }
    }

    // Fallback to internal storage if external media dir unavailable
    val outputDirectory = if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir

    val photoFile = File(
        outputDirectory,
        "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageCaptured(Uri.fromFile(photoFile))
            }

            override fun onError(exc: ImageCaptureException) {
                exc.printStackTrace()
            }
        }
    )
}