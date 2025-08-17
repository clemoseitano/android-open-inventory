package com.coptimize.openinventory.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.coptimize.openinventory.data.auth.Saved_carts
import com.coptimize.openinventory.data.auth.AuthDb
import com.coptimize.openinventory.data.model.Cart
import com.coptimize.openinventory.data.model.CartItem
import com.coptimize.openinventory.data.model.Customer
import com.coptimize.openinventory.data.model.SavedCart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString // Import the extension function
import kotlinx.serialization.json.Json     // Import the Json object
import kotlinx.serialization.decodeFromString
import javax.inject.Inject

class AuthSavedCartRepositoryImpl @Inject constructor(
    private val db: AuthDb,
) : SavedCartRepository {
    fun toDomain(savedCart: Saved_carts): SavedCart {
        return SavedCart(
            id = savedCart.id,
            customerId = if(savedCart.customer_id!=null) savedCart.customer_id.toString() else "",
            items = savedCart.items,
            status = if (savedCart.status.isNullOrBlank()) "pending" else savedCart.status,
            createdAt = if(savedCart.created_at.isNullOrBlank())"" else savedCart.created_at,
            updatedAt = if(savedCart.updated_at.isNullOrBlank())"" else savedCart.updated_at,
            userId = if(savedCart.user_id!=null) savedCart.user_id.toString() else "",
        )
    }
    /**
     * Parses a JSON string from the saved_carts table into a List of CartItem objects
     * using kotlinx.serialization.
     */
    fun parseCartItemsFromString(itemsJsonString: String?): List<CartItem> {
        if (itemsJsonString.isNullOrBlank()) {
            return emptyList()
        }
        return try {
            // --- Elegant JSON Decoding ---
            Json.decodeFromString<List<CartItem>>(itemsJsonString)
            // --- Done! ---
        } catch (e: Exception) {
            println("Error decoding cart items from JSON: ${e.message}")
            emptyList() // Return empty list on failure
        }
    }

    override fun getActiveCarts(): Flow<List<SavedCart>> {
        return db.savedCartQueries.selectActive()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { savedCarts -> savedCarts.map { toDomain(it) } }
    }

    override suspend fun restoreCartItems(cartId: String): Pair<List<CartItem>, Customer?>? {
        return withContext(Dispatchers.IO) {
            val savedCart = db.savedCartQueries.selectActive().executeAsList().find { it.id == cartId }
            savedCart?.let {
                val items = parseCartItemsFromString(it.items)
                val customerId = it.customer_id?.toString()
                var cs: Customer? = null
                if(customerId!=null){
                val customer = db.customerQueries.fetch(customerId).executeAsOne()
                cs = Customer(
                    id = customer.id,
                    name = customer.name,
                    contact = customer.contact,
                    paymentMethod = customer.payment_method,
                    createdAt = if(customer.created_at.isNullOrBlank())"" else customer.created_at,
                    updatedAt = if(customer.updated_at.isNullOrBlank())"" else customer.updated_at,
                    deletedAt = customer.deleted_at
                )}
                Pair(items, cs)
            }
        }
    }

    override suspend fun updateCart(cartId: String, cart: Cart, customerId: String?, userId: String?) {
        requireNotNull(userId)
        return withContext(Dispatchers.IO) {
            val itemsJson = Json.encodeToString(cart.items)
            db.savedCartQueries.update(
                id = cartId,
                items = itemsJson,
                customer_id = customerId?.toLong(),
                user_id = userId
            )
        }
    }

    override suspend fun updateCartStatus(cartId: String, status: String, userId:String?) {
        requireNotNull(userId)
        return withContext(Dispatchers.IO) {
            db.savedCartQueries.updateStatus(status, userId, cartId)
        }
    }

    override suspend fun saveCart(cart: Cart, customerId: String?, userId: String?): Result<String> {
        requireNotNull(userId) { "User ID is required to save a cart in Auth mode" }

        return withContext(Dispatchers.IO) {
            try {
                // Convert the list of CartItem objects to a JSON string for storage
                val itemsJson = Json.encodeToString(cart.items)

                db.savedCartQueries.insert(
                    customer_id = customerId?.toLong(),
                    items = itemsJson,
                    status = "pending",
                    user_id = userId
                )
                Result.success("OK")
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
}