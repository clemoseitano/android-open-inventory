package com.coptimize.openinventory.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.coptimize.openinventory.data.api.AiInferenceService
import com.coptimize.openinventory.data.api.OcrRequest
import com.coptimize.openinventory.data.api.PollStatusResponse
import com.coptimize.openinventory.ui.util.ImageUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductAnalysisRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: AiInferenceService
) : ProductAnalysisRepository {

    override suspend fun pollInferenceResults(taskId: String): PollStatusResponse {
        return withContext(Dispatchers.IO) {
            val response = apiService.getTaskStatus(taskId)
            if (response.isSuccessful && response.body() != null) {
                Log.i("CLEMENT", "RESPONSE RECEIVED")
                Log.i("CLEMENT", (response.body().toString()))
                response.body()!!
            } else {
                throw Exception("Failed to poll status: ${response.code()} ${response.message()}")
            }
        }
    }

    // Task 2: Compress Multiple Images -> Upload List -> Return Task ID
    override suspend fun performRemoteOcrAndInference(uris: List<Uri>): String {
        Log.e("CLEMENT", "ALL URIS: ${uris}")
        return withContext(Dispatchers.IO) {
            if (uris.isEmpty()) throw Exception("No images provided")

            // Create a list of MultipartBody.Part
            val parts = uris.mapIndexedNotNull { index, uri ->
                val compressedBytes = ImageUtils.getCompressedImageBytes(context, uri, 2_097_152) // 2MiB max

                if (compressedBytes != null) {
                    val requestFile = compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    // "images" is the key the server expects for the array
                    MultipartBody.Part.createFormData("images", "product_$index.jpg", requestFile)
                } else {
                    null // Skip images that failed to compress
                }
            }

            if (parts.isEmpty()) throw Exception("Failed to compress any images")

            val response = apiService.uploadImagesForInference(parts)

            if (response.isSuccessful && response.body() != null) {
                response.body()!!.taskId
            } else {
                throw Exception("Image upload failed: ${response.code()}")
            }
        }
    }

    // Task 3: Local OCR on Multiple Images -> Concat Text -> Send -> Return Task ID
    override suspend fun performLocalOcrAndRemoteInference(uris: List<Uri>): String {
        return withContext(Dispatchers.Default) {
            if (uris.isEmpty()) throw Exception("No images provided")

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val combinedTextBuilder = StringBuilder()

            // Iterate through images sequentially to save memory
            for (uri in uris) {
                try {
                    val image = InputImage.fromFilePath(context, uri)
                    val visionText = recognizer.process(image).await()

                    if (visionText.text.isNotBlank()) {
                        combinedTextBuilder.append(visionText.text).append("\n\n")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Continue to next image even if one fails
                }
            }

            val finalExtractedText = combinedTextBuilder.toString().trim()

            if (finalExtractedText.isBlank()) {
                throw Exception("No text detected in any of the images")
            }

            // Send concatenated text to server
            val response = apiService.sendOcrResults(OcrRequest(finalExtractedText))

            if (response.isSuccessful && response.body() != null) {
                response.body()!!.taskId
            } else {
                throw Exception("Failed to send OCR text: ${response.code()}")
            }
        }
    }
}