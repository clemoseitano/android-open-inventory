package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.model.ProductDiscoveryTask
import kotlinx.coroutines.flow.Flow

interface ProductDiscoveryRepository {
    suspend fun saveTask(productId: String, taskId: String, stockId: String?): String
    suspend fun updateTaskStatus(taskId: String, status: String, result: String?)
    fun getActiveTasks(): Flow<List<ProductDiscoveryTask>>
}