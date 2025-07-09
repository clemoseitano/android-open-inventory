package com.coptimize.openinventory.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.coptimize.openinventory.data.auth.AuthDb
import com.coptimize.openinventory.data.auth.GetSalesWithItemsInRange
import com.coptimize.openinventory.data.model.Cart
import com.coptimize.openinventory.data.model.Sale
import com.coptimize.openinventory.data.model.SaleWithItems
import com.coptimize.openinventory.data.model.parseAggregatedItems
import com.coptimize.openinventory.data.model.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class AuthSaleRepositoryImpl @Inject constructor(
    private val db: AuthDb
) : SaleRepository {
    fun GetSalesWithItemsInRange.toSaleWithItems(): SaleWithItems {
        val sale = Sale(
            id = this.id,
            customerId = this.customer_id.toString(),
            // We add customerName to the Sale model for convenience
            customerName = if (!this.customer_name.isNullOrBlank()) this.customer_name else "Walk-in Customer",
            date = this.date?:"",
            subtotal = this.subtotal,
            tax = this.tax,
            discount = this.discount,
            total = this.total,
            paidAmount = this.paid_amount,
            changeAmount = this.change_amount,
            createdAt = this.created_at?:"",
            updatedAt = this.updated_at?:"",
            userId = this.user_id
        )
        val items = parseAggregatedItems(this.aggregated_items)
        return SaleWithItems(sale, items)
    }
    override fun getAllSales(): Flow<List<Sale>> {
        return db.saleQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { sales -> sales.map { it.toDomain() } }
    }

    override fun getSalesWithItemsInRange(startDate: String, endDate: String): Flow<List<SaleWithItems>> {
        return db.saleQueries.getSalesWithItemsInRange(startDate, endDate)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { results ->
                results.map { it.toSaleWithItems() }
            }
    }

    override suspend fun recordSale(cart: Cart, customerId: String?, userId: String?): Result<String> {
        requireNotNull(userId) { "User ID is required to record a sale in Auth mode" }

        return withContext(Dispatchers.IO) {
            try {
                val newSaleId = UUID.randomUUID().toString()

                db.transaction {
                    // Step 1: Validate stock (no change here)
                    cart.items.forEach { cartItem ->
                        val productInDb = db.productQueries.selectById(cartItem.productId).executeAsOne()
                        if (productInDb.quantity < cartItem.quantity) {
                            throw IllegalStateException("Not enough stock for ${productInDb.name}")
                        }
                    }

                    // Step 2: Insert the sale record using data from the Cart object
                    db.saleQueries.insert(
                        customer_id = customerId?.toLong(),
                        subtotal = cart.subtotal,
                        tax = cart.tax,
                        discount = cart.discount,
                        total = cart.total,
                        paid_amount = cart.paidAmount,
                        change_amount = cart.changeAmount,
                        user_id = userId,
                    )

                    // Step 3: Insert sale items and decrement stock (no change here)
                    cart.items.forEach { cartItem ->
                        db.saleItemQueries.insert(
                            sale_id = newSaleId.toLong(),
                            product_id = cartItem.productId.toLong(),
                            quantity = cartItem.quantity.toLong(),
                            price = cartItem.price
                        )
                        db.productQueries.decrement(quantity_change=cartItem.quantity.toLong(), productId=cartItem.productId, userId=userId)
                    }

                    // Step 4: Handle saved cart status update
                    if (cart.savedCartId != null) {
                        // Assuming you have a query named 'updateStatus' in your SavedCartQueries
                        db.savedCartQueries.updateStatus(
                            id = cart.savedCartId,
                            status = "completed",
                            userId=userId
                        )
                    }
                }
                Result.success(newSaleId)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
}