package com.coptimize.openinventory.ui.screens.sale

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

@Composable
fun EmbeddedScanner(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    // Request permission on launch
    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            val barcodeView = remember {
                DecoratedBarcodeView(context).apply {
                    initializeFromIntent(Intent())
                    setStatusText("") // Clear the default status text
                }
            }

            // This effect handles the lifecycle of the camera
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> barcodeView.resume()
                        Lifecycle.Event.ON_PAUSE -> barcodeView.pause()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    barcodeView.pause() // Ensure camera is released
                }
            }

            AndroidView(
                factory = { barcodeView },
                modifier = Modifier.fillMaxSize()
            ) { view ->
                view.decodeContinuous(object : BarcodeCallback {
                    override fun barcodeResult(result: BarcodeResult?) {
                        result?.text?.let {
                            if (it.isNotBlank()) {
                                // Pause scanning once a result is found
                                view.pause()
                                onBarcodeScanned(it)
                            }
                        }
                    }

                    override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {}
                })
            }
        } else {
            // Show a message and a button to request permission again
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Camera Permission")
                }
            }
        }
    }
}