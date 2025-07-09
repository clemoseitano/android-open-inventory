package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.auth.AuthDb
import com.coptimize.openinventory.data.model.Cart
import com.coptimize.openinventory.data.model.CartItem
import com.coptimize.openinventory.data.model.SavedCart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString // Import the extension function
import kotlinx.serialization.json.Json     // Import the Json object
import kotlinx.serialization.decodeFromString
import javax.inject.Inject

class AuthSavedCartRepositoryImpl @Inject constructor(
    private val db: AuthDb,
) : SavedCartRepository {

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
        // TODO: Implement the 'selectActive' query and mapping
        return kotlinx.coroutines.flow.flowOf(emptyList())
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