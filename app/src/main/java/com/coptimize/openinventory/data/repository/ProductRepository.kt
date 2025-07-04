package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.dao.ProductDao
import com.coptimize.openinventory.data.model.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(private val productDao: ProductDao) {

    fun getAllActiveProducts(): Flow<List<Product>> {
        return productDao.getAllActiveProducts()
    }

    suspend fun getProduct(id: String): Product? {
        return productDao.getProductById(id)
    }

    suspend fun addProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(productId: String) {
        productDao.softDeleteProduct(productId)
    }
}