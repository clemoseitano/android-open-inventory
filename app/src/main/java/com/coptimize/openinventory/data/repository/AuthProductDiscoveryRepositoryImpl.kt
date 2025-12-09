package com.coptimize.openinventory.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.coptimize.openinventory.data.auth.AuthDb
import com.coptimize.openinventory.data.auth.Product_discovery_tasks
import com.coptimize.openinventory.data.model.ProductDiscoveryTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthProductDiscoveryRepositoryImpl @Inject constructor(
    private val db: AuthDb
) : ProductDiscoveryRepository {

    private fun toDomain(entity: Product_discovery_tasks): ProductDiscoveryTask {
        return ProductDiscoveryTask(
            id = entity.id.toInt(),
            productId = entity.product_id.toString(),
            taskId = entity.task_id,
            status = entity.status,
            stockId = entity.stock_id?.toString(),
            taskResult = entity.task_result,
            createdAt = entity.created_at ?: "",
            updatedAt = entity.updated_at ?: ""
        )
    }

    override suspend fun saveTask(productId: String, taskId: String, stockId: String?): String {
        return withContext(Dispatchers.IO) {
            db.productDiscoveryTaskQueries.insert(
                product_id = productId.toLong(),
                task_id = taskId,
                status = "pending",
                stock_id = stockId?.toLong()
            )
            // Assuming your table generates an ID or you use the task_id as reference
            // Returning taskId for now as it's the unique identifier for the API
            taskId
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: String, result: String?) {
        withContext(Dispatchers.IO) {
            db.productDiscoveryTaskQueries.update(
                status = status,
                task_result = result,
                task_id = taskId
            )
        }
    }

    override fun getActiveTasks(): Flow<List<ProductDiscoveryTask>> {
        return db.productDiscoveryTaskQueries.selectActive()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { toDomain(it) } }
    }
}