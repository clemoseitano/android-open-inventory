package com.coptimize.openinventory.data.model

data class ProductDiscoveryTask(
    val id: Int,
    val productId: String,
    val taskId: String,
    val status: String,
    val stockId: String?,
    val taskResult: String?,
    val createdAt: String,
    val updatedAt: String
)