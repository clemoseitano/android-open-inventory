package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.model.Cart
import com.coptimize.openinventory.data.model.CartItem
import com.coptimize.openinventory.data.model.Customer
import kotlinx.coroutines.flow.Flow

import com.coptimize.openinventory.data.model.SavedCart

interface SavedCartRepository {

    /**
     * Gets a stream of all currently active or pending saved carts.
     */
    fun getActiveCarts(): Flow<List<SavedCart>>

    /**
     * Saves the current state of a cart to the database.
     * @param cart The in-memory cart object to save.
     * @param customerId The optional associated customer.
     * @param userId The optional user performing the action.
     * @return A Result wrapper containing the new Saved Cart ID on success.
     */
    suspend fun saveCart(cart: Cart, customerId: String?, userId: String?): Result<String>

    suspend fun restoreCartItems(cartId: String): Pair<List<CartItem>?, Customer?>?

    suspend fun updateCart(cartId: String, cart: Cart, customerId: String?, userId: String?)

    suspend fun updateCartStatus(cartId: String, status:String, userId:String?)
}