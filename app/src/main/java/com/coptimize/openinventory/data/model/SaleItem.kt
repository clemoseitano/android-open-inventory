package com.coptimize.openinventory.data.model

data class SaleItem(
    val id: String,
    val saleId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val createdAt: String,
    val updatedAt: String
)