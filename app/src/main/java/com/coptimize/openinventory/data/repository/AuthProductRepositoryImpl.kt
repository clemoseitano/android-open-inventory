package com.coptimize.openinventory.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.coptimize.openinventory.data.auth.AuthDb // Import AuthDb
import com.coptimize.openinventory.data.auth.Products
import com.coptimize.openinventory.data.auth.SelectDeleted
import com.coptimize.openinventory.data.auth.Stocks
import com.coptimize.openinventory.data.model.Product
import com.coptimize.openinventory.data.model.Stock
import com.coptimize.openinventory.ui.formatAsDateForDatabaseQuery
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

    fun toDomain(stock: Stocks): Stock {
        return Stock(
            id = stock.id,
            productId = stock.product_id.toString(),
            supplier = stock.supplier,
            supplierContact = stock.supplier_contact,
            purchasePrice = stock.purchase_price,
            purchaseDate = stock.purchase_date,
            expiryDate = stock.expiry_date,
            quantity = stock.quantity.toInt(),
            userId = stock.user_id,
            unitPrice = stock.unit_price,
        )
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

    override suspend fun getLastStockForProduct(productId: String): Stock? {
        return withContext(Dispatchers.IO) {
            // The query name is the same, but it's called on the AuthDb instance.
            val stock = db.stockQueries.lastStockForProduct(pid = productId.toLong()).executeAsOneOrNull()

            stock?.let { toDomain(stock) }
        }
    }

    override suspend fun addProduct(product: Product): String {
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
        return  db.productQueries.getLastCreatedId().executeAsOneOrNull()?:""
    }

    override suspend fun addStock(stock: Stock) {
        db.stockQueries.insert(
            product_id = stock.productId.toLong(),
            supplier = stock.supplier,
            supplier_contact = stock.supplierContact,
            unit_price = stock.unitPrice,
            purchase_price = stock.purchasePrice,
            purchase_date = stock.purchaseDate,
            expiry_date = stock.expiryDate,
            quantity = stock.quantity.toLong(),
            user_id = stock.userId
        )
    }

    override suspend fun updateProductAndStock(product: Product, stock: Stock) {
        requireNotNull(product.userId) { "User ID cannot be null in Auth mode" }
        db.transaction {
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
                quantity_change = stock.quantity.toLong()
            )
            db.stockQueries.insert(
                product_id = stock.productId.toLong(),
                supplier = stock.supplier,
                supplier_contact = stock.supplierContact,
                unit_price = stock.unitPrice,
                purchase_price = stock.purchasePrice,
                purchase_date = stock.purchaseDate,
                expiry_date = stock.expiryDate,
                quantity = stock.quantity.toLong(),
                user_id = stock.userId
            )
        }
    }

    override suspend fun deleteProduct(productId: String, userId: String?) {
        requireNotNull(userId) { "User ID is required for deletion in Auth mode" }
        db.productQueries.softDelete(productId, userId)
    }

    override suspend fun restoreProduct(productId: String, userId: String?) {
        db.productQueries.restore(userId=userId, productId=productId)
    }
}