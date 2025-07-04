package com.coptimize.openinventory.data.dao

import androidx.room.*
import com.coptimize.openinventory.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE deleted_at IS NULL ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    // Using a query for soft delete
    @Query("UPDATE products SET deleted_at = CURRENT_TIMESTAMP WHERE id = :productId")
    suspend fun softDeleteProduct(productId: String)
}