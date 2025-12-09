package com.coptimize.openinventory.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// Simple data class for sending local OCR results
data class OcrRequest(val extractedText: String)

interface AiInferenceService {
    // Task 1: Upload Image
    @Multipart
    @POST("process-images/")
    suspend fun uploadImagesForInference(
        @Part images: List<MultipartBody.Part>
    ): Response<UploadTaskResponse>

    // Task 2: Send Local OCR Text
    @POST("process-text/")
    suspend fun sendOcrResults(
        @Body request: OcrRequest
    ): Response<UploadTaskResponse>

    // Task 3: Send retrieve response
    @GET("inference-response/{taskId}")
    suspend fun getTaskStatus(
        @Path("taskId") taskId: String
    ): Response<PollStatusResponse>
}