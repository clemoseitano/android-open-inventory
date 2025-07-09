package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.model.Cart
import com.coptimize.openinventory.data.model.Sale
import com.coptimize.openinventory.data.model.SaleWithItems
import kotlinx.coroutines.flow.Flow

interface SaleRepository {

    /**
     * Gets a continuous stream of all sales records.
     */
    fun getAllSales(): Flow<List<Sale>>

    /**
     * Records a new sale as a single, atomic transaction.
     * This involves:
     * 1. Validating stock for all items in the cart.
     * 2. Inserting a new record into the 'sales' table.
     * 3. Inserting a record for each item into the 'sale_items' table.
     * 4. Decrementing the stock for each product in the 'products' table.
     *
     * @return A Result wrapper containing the new Sale ID on success, or an Exception on failure.
     */
    suspend fun recordSale(cart: Cart, customerId: String?, userId: String?): Result<String>

    fun getSalesWithItemsInRange(startDate: String, endDate: String):Flow<List<SaleWithItems>>
}