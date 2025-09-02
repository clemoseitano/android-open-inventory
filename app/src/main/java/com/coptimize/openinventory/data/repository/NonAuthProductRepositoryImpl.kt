package com.coptimize.openinventory.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.coptimize.openinventory.data.NonAuthDb
import com.coptimize.openinventory.data.Products
import com.coptimize.openinventory.data.SelectDeleted
import com.coptimize.openinventory.data.Stocks
import com.coptimize.openinventory.data.model.Product
import com.coptimize.openinventory.data.model.Stock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NonAuthProductRepositoryImpl @Inject constructor(
    private val db: NonAuthDb
) : ProductRepository {
    // Mapper for the Non-Auth DB's generated class
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
                    section = t.section,
                    shelf = t.shelf,
                    userId = null,
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
                    section = t.section,
                    shelf = t.shelf,
                    userId = null,
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
            userId = null,
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
            productFromDb?.let { toDomain(it) }
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
        db.productQueries.insert(
            name = product.name,
            price = product.price,
            quantity = product.quantity.toLong(),
            category = product.category,
            barcode = product.barcode,
            image_path = product.imagePath,
            manufacturer = product.manufacturer,
            tax = product.tax,
            tax_is_flat_rate = if (product.isTaxFlatRate) 1L else 0L,
            section = product.section,
            shelf = product.shelf
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
                section = product.section,
                shelf = product.shelf,
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
            )
        }
    }

    override suspend fun deleteProduct(productId: String, userId: String?) {
        // userId is ignored in non-auth mode
        db.productQueries.softDelete(productId)
    }

    override suspend fun restoreProduct(productId: String, userId: String?) {
        db.productQueries.restore(productId)
    }
}