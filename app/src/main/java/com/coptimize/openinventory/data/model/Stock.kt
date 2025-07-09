package com.coptimize.openinventory.data.model

data class Stock(
    val id: String,
    val productId: String,
    val supplier: String?,
    val supplierContact: String?,
    val unitPrice: Double,
    val purchasePrice: Double,
    val purchaseDate: String?,
    val expiryDate: String?,
    val quantity: Int,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
    val userId: String? = null
)