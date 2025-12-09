package com.coptimize.openinventory.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.coptimize.openinventory.data.api.PollStatusResponse
import com.coptimize.openinventory.data.repository.ProductAnalysisRepository
import com.coptimize.openinventory.data.repository.ProductDiscoveryRepository
import com.coptimize.openinventory.data.repository.ProductRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@HiltWorker
class ProductDiscoveryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val discoveryRepository: ProductDiscoveryRepository,
    private val analysisRepository: ProductAnalysisRepository,
    private val productRepository: ProductRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
        val productId = inputData.getString(KEY_PRODUCT_ID) ?: return Result.failure()
        val stockId = inputData.getString(KEY_STOCK_ID) // Nullable
        val isFinalAttempt = inputData.getBoolean(KEY_IS_FINAL_ATTEMPT, false)

        try {
            // 1. Check with server
            val pollResult = analysisRepository.pollInferenceResults(taskId)

            // 2. Handle Status
            when (pollResult.status.uppercase()) {
                "SUCCESS" -> {
                    // Task Done: Update DB and Product
                    handleSuccess(taskId, productId, stockId, pollResult)
                    return Result.success()
                }
                "FAILED" -> {
                    discoveryRepository.updateTaskStatus(taskId, "cancelled", "Analysis failed on server")
                    return Result.failure()
                }
                else -> {
                    // PENDING or PROCESSING
                    if (isFinalAttempt) {
                        discoveryRepository.updateTaskStatus(taskId, "cancelled", "Timeout: No results after final attempt")
                        return Result.failure()
                    } else {
                        // Pass success so the chain continues to the next delayed worker
                        return Result.success()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If it's a network error, we might want to retry, but for this specific flow
            // sticking to the rigid schedule is usually safer.
            if (isFinalAttempt) {
                discoveryRepository.updateTaskStatus(taskId, "cancelled", "Error: ${e.localizedMessage}")
                return Result.failure()
            }
            return Result.success() // Continue chain
        }
    }

    private suspend fun handleSuccess(taskId: String, productId: String, stockId: String?, result: PollStatusResponse) {
        val inference = result.result ?: return

        // 1. Mark task as completed
        // We catch serialization errors to prevent crashing the worker on success
        val jsonResult = try {
            Json.encodeToString(result)
        } catch(e: Exception) {
            "{\"status\":\"SUCCESS\", \"error\":\"Serialization failed\"}"
        }

        discoveryRepository.updateTaskStatus(taskId, "completed", jsonResult)

        // 2. Fetch existing data
        val existingProduct = productRepository.getProduct(productId) ?: return

        // If stockId is null, we try to fetch the latest stock associated with this product
        val existingStock = if (stockId != null) {
            // Assuming you have a method to get stock by ID, or we use the product ID based lookup
            productRepository.getLastStockForProduct(productId)
        } else {
            productRepository.getLastStockForProduct(productId)
        }

        // 3. Conditional Updates (Merge Logic)

        val updatedProduct = existingProduct.copy(
            name = mergeString(existingProduct.name, inference.name),
            category = mergeString(existingProduct.category, inference.category),
            manufacturer = mergeNullableString(existingProduct.manufacturer, inference.manufacturer),
            barcode = mergeNullableString(existingProduct.barcode, inference.barcode)
            // Note: We deliberately do NOT update price or quantity here
        )

        val updatedStock = existingStock?.copy(
            purchaseDate = mergeNullableString(existingStock.purchaseDate, inference.productionDate),
            expiryDate = mergeNullableString(existingStock.expiryDate, inference.expiryDate)
        )

        // 4. Save updates
        if (updatedStock != null) {
            // This is the preferred method as it handles the transaction for both tables
            productRepository.updateProductAndStock(updatedProduct, updatedStock)
        } else {
            productRepository.updateProduct(updatedProduct)
        }
    }

    /**
     * Merges two non-nullable strings.
     * Logic:
     * 1. If AI value is blank, keep User value.
     * 2. If User value is blank, take AI value.
     * 3. If both exist and are NOT equal (case-insensitive), prepend AI value.
     */
    private fun mergeString(userValue: String, aiValue: String?): String {
        if (aiValue.isNullOrBlank()) return userValue
        // If the user value is a placeholder or blank, just take the AI value
        if (userValue.isBlank() || userValue.equals("New Product", ignoreCase = true)) return aiValue

        if (userValue.equals(aiValue, ignoreCase = true)) {
            return userValue
        }

        // Prepend AI value with the user's original text
        return "$userValue [$aiValue]"
    }

    /**
     * Merges nullable strings.
     */
    private fun mergeNullableString(userValue: String?, aiValue: String?): String? {
        if (aiValue.isNullOrBlank()) return userValue
        if (userValue.isNullOrBlank()) return aiValue

        if (userValue.equals(aiValue, ignoreCase = true)) {
            return userValue
        }

        // Prepend AI value with the user's original text
        return "$userValue [$aiValue]"
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_PRODUCT_ID = "product_id"
        const val KEY_STOCK_ID = "stock_id"
        const val KEY_IS_FINAL_ATTEMPT = "is_final"
    }
}