package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.model.Cart
import com.coptimize.openinventory.data.model.CartItem
import com.coptimize.openinventory.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Ensures there is only one instance of the cart in the app
class CartRepository @Inject constructor() {

    private val _items = MutableStateFlow<Map<String, CartItem>>(emptyMap())
    val items: StateFlow<List<CartItem>> = MutableStateFlow(emptyList()) // We'll derive this later

    // TODO: Add StateFlows for subtotal, tax, total, etc. that are calculated automatically
    // whenever the `_items` flow changes.

    fun addProductToCart(product: Product, quantity: Int = 1) {
        _items.update { currentItems ->
            val mutableItems = currentItems.toMutableMap()
            val existingItem = mutableItems[product.id]

            if (existingItem != null) {
                // Product already in cart, just update the quantity
                mutableItems[product.id] = existingItem.copy(
                    quantity = existingItem.quantity + quantity
                )
            } else {
                // New product, add it to the cart
                mutableItems[product.id] = CartItem(
                    productId = product.id,
                    quantity = quantity,
                    price = product.price,
                    name = product.name,
                    maxStock = product.quantity,
                    tax = product.tax ?: 0.0,
                    isFlatRate = product.isTaxFlatRate // Capture the price at the time of adding
                )
            }
            mutableItems
        }
    }

    fun removeItem(productId: String) {
        _items.update { currentItems ->
            currentItems.toMutableMap().apply {
                remove(productId)
            }
        }
    }

    fun clearCart() {
        _items.value = emptyMap()
    }

    /**
     * Creates a snapshot of the current cart state, ready to be passed
     * to the SaleRepository for processing.
     */
    fun getCartSnapshot(): Cart {
        val currentItems = _items.value.values.toList()

        // In a real app, these calculations would be more complex
        val subtotal = currentItems.sumOf { it.price * it.quantity }
        val tax = subtotal * 0.20
        val total = subtotal + tax

        return Cart(
            items = currentItems,
            subtotal = subtotal,
            tax = tax,
            discount = 0.0,
            total = total,
            paidAmount = total,
            changeAmount = 0.0
        )
    }
}