package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.model.Product
import kotlinx.coroutines.flow.Flow

// This is the contract. The ViewModel only knows about this.
interface ProductRepository {
    fun getAllActiveProducts(): Flow<List<Product>>

    fun getArchivedProducts(): Flow<List<Product>>

    suspend fun getProduct(id: String): Product?

    suspend fun addProduct(product: Product)

    suspend fun updateProduct(product: Product)

    suspend fun deleteProduct(productId: String, userId: String? = null) // userId is optional

    suspend fun restoreProduct(productId: String, userId: String? = null) // userId is optional
}