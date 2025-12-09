package com.coptimize.openinventory

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.coptimize.openinventory.data.repository.ProductAnalysisRepository
import com.coptimize.openinventory.data.repository.ProductDiscoveryRepository
import com.coptimize.openinventory.worker.DiscoveryScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DebugReceiver : BroadcastReceiver() {

    @Inject
    lateinit var analysisRepository: ProductAnalysisRepository
    @Inject
    lateinit var discoveryRepository: ProductDiscoveryRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Your hardcoded URIs
    private val testUris = listOf(
        "file:///storage/emulated/0/Android/media/com.coptimize.openinventory/OpenInventory/IMG_20251206_131913.jpg",
        "file:///storage/emulated/0/Android/media/com.coptimize.openinventory/OpenInventory/IMG_20251206_131927.jpg"
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "com.coptimize.DEBUG_ACTION") return
        val productId = "296711983039"

        val pendingResult = goAsync() // Keep the receiver alive for the coroutine
        val command = intent.getStringExtra("cmd")
        val index = intent.getStringExtra("index")?.toIntOrNull() ?: 0 // Default to first image

        Log.d(TAG, ">>> RECEIVED COMMAND: $command (Index: $index) <<<")

        scope.launch {
            try {
                when (command) {
                    "remote_inference" -> {
                        // Task 2: Compress -> Upload -> Get Task ID
                        // 1. Convert the list of Strings to a list of Android Uris
                        val uris = testUris.map { Uri.parse(it) }
                        Log.d(TAG, "Starting Remote Inference on: $uris")
                        val taskId = analysisRepository.performRemoteOcrAndInference(uris)
                        Log.i(TAG, "✅ UPLOAD SUCCESS. Task ID: $taskId")
                        discoveryRepository.saveTask(
                            productId = productId,
                            taskId = taskId,
                            stockId = null
                        )

                        // C. Schedule Worker
                        DiscoveryScheduler.scheduleTaskMonitoring(
                            context = context,
                            taskId = taskId,
                            productId = productId,
                            stockId = null
                        )

                        Log.i("CLEMENT", "Discovery started for Product $productId with Task $taskId using ${uris.size} images")

                    }

                    "local_ocr" -> {
                        // Task 3: Local OCR -> Send Text -> Get Task ID
                        val uris = testUris.map { Uri.parse(it) }
                        Log.d(TAG, "Starting Local Inference on: $uris")
                        val taskId = analysisRepository.performLocalOcrAndRemoteInference(uris)
                        Log.i(TAG, "✅ OCR SEND SUCCESS. Task ID: $taskId")
                    }

                    "poll" -> {
                        // Task 1: Poll Status
                        val taskId = intent.getStringExtra("taskId")
                        if (taskId.isNullOrBlank()) {
                            Log.e(TAG, "❌ Poll failed: Missing 'taskId' extra")
                        } else {
                            Log.d(TAG, "Polling status for: $taskId")
                            val result = analysisRepository.pollInferenceResults(taskId)
                            Log.i(TAG, "✅ POLL RESULT: $result")
                        }
                    }

                    else -> Log.w(TAG, "Unknown command: $command")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ OPERATION FAILED", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "OpenInventoryDebug"
    }
}