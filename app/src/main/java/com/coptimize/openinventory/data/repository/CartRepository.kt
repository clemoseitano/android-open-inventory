package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.model.Cart
import com.coptimize.openinventory.data.model.CartItem
import com.coptimize.openinventory.data.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Ensures there is only one instance of the cart in the app
class CartRepository @Inject constructor() {

    // The single source of truth for the cart's contents.
    // The key is the Product ID (String).
    private val _items = MutableStateFlow<Map<String, CartItem>>(emptyMap())

    // --- Public, read-only StateFlows derived from _items ---

    /**
     * A publicly exposed flow of the cart items as a simple list.
     * This automatically updates whenever the underlying map changes.
     */
    val items: StateFlow<List<CartItem>> = _items.map { it.values.toList().sortedBy { item -> item.name } }
        .stateIn(
            scope = CoroutineScope(Dispatchers.Default), // Calculations on a background thread
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * A flow that automatically calculates the subtotal whenever the cart items change.
     */
    val subtotal: StateFlow<Double> = items.map { list ->
        list.sumOf { it.price * it.quantity }
    }.stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * A flow that automatically calculates the total tax whenever the cart items change.
     * It correctly handles both flat rate and percentage-based taxes.
     */
    val tax: StateFlow<Double> = items.map { list ->
        list.sumOf { item ->
            if (item.isFlatRate) {
                item.tax * item.quantity // Flat tax per item
            } else {
                (item.price * item.quantity) * (item.tax / 100.0) // Percentage of item total
            }
        }
    }.stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * A flow that combines subtotal and tax to get the final total.
     * This will automatically update if either subtotal or tax changes.
     */
    val total: StateFlow<Double> = combine(subtotal, tax) { sub, tx ->
        sub + tx
    }.stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.WhileSubscribed(5000), 0.0)


    // --- Cart Action Methods ---

    fun addProductToCart(product: Product, quantity: Int = 1) {
        _items.update { currentItems ->
            val mutableItems = currentItems.toMutableMap()
            val existingItem = mutableItems[product.id]

            if (existingItem != null) {
                // Product already in cart, update quantity, respecting max stock
                val newQuantity = existingItem.quantity + quantity
                if (newQuantity <= existingItem.maxStock) {
                    mutableItems[product.id] = existingItem.copy(quantity = newQuantity)
                }
            } else {
                // New product, add it to the cart
                mutableItems[product.id] = CartItem(
                    productId = product.id,
                    name = product.name,
                    quantity = quantity,
                    price = product.price,
                    maxStock = product.quantity,
                    tax = product.tax ?: 0.0,
                    isFlatRate = product.isTaxFlatRate
                )
            }
            mutableItems
        }
    }

    fun updateItemQuantity(productId: String, newQuantity: Int) {
        _items.update { currentItems ->
            val mutableItems = currentItems.toMutableMap()
            val item = mutableItems[productId]
            if (item != null && newQuantity > 0 && newQuantity <= item.maxStock) {
                mutableItems[productId] = item.copy(quantity = newQuantity)
            } else if (newQuantity <= 0) {
                // If quantity is zero or less, remove the item
                mutableItems.remove(productId)
            }
            mutableItems
        }
    }

    fun removeItem(productId: String) {
        _items.update { currentItems ->
            currentItems.toMutableMap().apply { remove(productId) }
        }
    }

    fun clearCart() {
        _items.value = emptyMap()
    }

    /**
     * Creates a snapshot of the current cart state using the latest values from the reactive flows.
     * This is called right before completing a sale.
     */
    fun getCartSnapshot(paidAmount: Double, discount: Double): Cart {
        val currentTotal = total.value
        val changeAmount = paidAmount - (currentTotal - discount)

        return Cart(
            items = items.value, // Get the latest list from the flow
            subtotal = subtotal.value,
            tax = tax.value,
            discount = discount,
            total = currentTotal - discount,
            paidAmount = paidAmount,
            changeAmount = changeAmount
        )
    }
}