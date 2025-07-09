package com.coptimize.openinventory.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.coptimize.openinventory.data.auth.AuthDb // Import AuthDb
import com.coptimize.openinventory.data.auth.Products
import com.coptimize.openinventory.data.auth.SelectDeleted
import com.coptimize.openinventory.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthProductRepositoryImpl @Inject constructor(
    private val db: AuthDb
): ProductRepository {

    // Mapper for the Auth DB's generated class
    fun toDomain(t: Any /*Union[Products, SelectDeleted]*/): Product {
        when (t) {
            is Products -> {
                return Product(
                    id = t.id,
                    name = t.name,
                    category = t.category,
                    manufacturer = t.manufacturer,
                    barcode = t.barcode,
                    price = t.price,
                    tax = t.tax,
                    isTaxFlatRate = t.tax_is_flat_rate == 1L,
                    quantity = t.quantity.toInt(),
                    imagePath = t.image_path,
                    userId = t.user_id,
                    createdAt = t.created_at,
                    updatedAt = t.updated_at,
                    deletedAt = t.deleted_at
                )
            }
            is SelectDeleted -> {
                return Product(
                    id = t.id,
                    name = t.name,
                    category = t.category,
                    manufacturer = t.manufacturer,
                    barcode = t.barcode,
                    price = t.price,
                    tax = t.tax,
                    isTaxFlatRate = t.tax_is_flat_rate == 1L,
                    quantity = t.quantity.toInt(),
                    imagePath = t.image_path,
                    userId = t.user_id,
                    createdAt = t.created_at,
                    updatedAt = t.updated_at,
                    deletedAt = t.deleted_at
                )
            }

            else -> throw IllegalArgumentException("Unknown type passed to mapToDomainProduct: ${t::class.java}")
        }
    }


    override fun getAllActiveProducts(): Flow<List<Product>> {
        return db.productQueries.selectAllActive()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { products -> products.map { toDomain(it) } }
    }

    override fun getArchivedProducts(): Flow<List<Product>> {
        return db.productQueries.selectDeleted()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { products -> products.map { toDomain(it) } }
    }

    override suspend fun getProduct(id: String): Product? {
        return withContext(Dispatchers.IO) {
            // The query name is the same, but it's called on the AuthDb instance.
            val productFromDb = db.productQueries.selectById(productId = id).executeAsOneOrNull()

            // The mapper is smart enough to handle the auth-specific generated class.
            productFromDb?.let { toDomain(it)}
        }
    }

    override suspend fun addProduct(product: Product) {
        requireNotNull(product.userId) { "User ID cannot be null in Auth mode" }
        db.productQueries.insert(
            name = product.name,
            price = product.price,
            quantity = product.quantity.toLong(),
            category = product.category,
            barcode = product.barcode,
            manufacturer = product.manufacturer,
            tax = product.tax,
            tax_is_flat_rate = if (product.isTaxFlatRate) 1L else 0L,
            image_path = product.imagePath,
            user_id = product.userId
        )
    }

    override suspend fun updateProduct(product: Product) {
        requireNotNull(product.userId) { "User ID cannot be null in Auth mode" }
        db.productQueries.update(
            id = product.id,
            name = product.name,
            price = product.price,
            category = product.category,
            barcode = product.barcode,
            manufacturer = product.manufacturer,
            tax = product.tax,
            taxIsFlatRate = if (product.isTaxFlatRate) 1L else 0L,
            imagePath = product.imagePath,
            userId = product.userId,
            quantity_change = product.quantity.toLong()
        )
    }

    override suspend fun deleteProduct(productId: String, userId: String?) {
        requireNotNull(userId) { "User ID is required for deletion in Auth mode" }
        db.productQueries.softDelete(productId, userId)
    }

    override suspend fun restoreProduct(productId: String, userId: String?) {
        db.productQueries.restore(userId=userId, productId=productId)
    }
}