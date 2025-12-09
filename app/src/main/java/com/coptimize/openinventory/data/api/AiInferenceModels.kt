package com.coptimize.openinventory.data.api

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// 1. The immediate response after uploading the image
@Serializable
data class UploadTaskResponse(
    @SerializedName("task_id") val taskId: String
)

// 2. The response when checking the status of a task
@Serializable
data class PollStatusResponse(
    val status: String, // "PENDING", "PROCESSING", "SUCCESS", "FAILED"
    val result: InferenceResponse? // The actual data, null if not ready
)

// 3. The final data
@Serializable
data class InferenceResponse(
    val name: String?,
    val description: String?,
    val category: String?,
    val manufacturer: String?,
    @SerializedName("production_date") val productionDate: String?,
    @SerializedName("expiry_date") val expiryDate: String?,
    val distributor: String?,
    val barcode: String?,
    val metadata: InferenceMetadata?
)

@Serializable
data class InferenceMetadata(
    @SerializedName("net_weight") val netWeight: String?,
    val volume: String?,
    @SerializedName("quantity_in_package") val quantityInPackage: String?,
    val size: String?,
    @SerializedName("part_number") val partNumber: String?,
    @SerializedName("age_rating") val ageRating: String?,
    @SerializedName("country_of_origin") val countryOfOrigin: String?,
    @JsonAdapter(StringOrListDeserializer::class)
    val ingredients: List<String>?,
    @JsonAdapter(StringOrListDeserializer::class)
    val materials: List<String>?,
    @JsonAdapter(StringOrListDeserializer::class)
    val warnings: List<String>?,
    @SerializedName("usage_directions")
    @JsonAdapter(StringOrListDeserializer::class)
    val usageDirections: List<String>?,
    @SerializedName("additional_info")
    @JsonAdapter(StringOrListDeserializer::class)
    val additionalInfo: List<String>?,
)