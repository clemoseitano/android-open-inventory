package com.coptimize.openinventory.data.model

data class OutOfStockLog(
    val id: String,
    val productId: Long,
    val productName: String,
    val attemptedQuantity: Int,
    val availableQuantity: Int,
    val resolved: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val userId: String? = null
)