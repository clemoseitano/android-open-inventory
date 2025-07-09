package com.coptimize.openinventory.data.model

import kotlinx.serialization.Serializable


/**
 * Represents a single item within the shopping cart.
 *
 * @param productId The unique ID of the product.
 * @param name The name of the product, stored for display purposes.
 * @param quantity The number of units of this product in the cart.
 * @param price The unit price of the product at the moment it was added to the cart.
 * @param maxStock The total stock at the time it was added to the cart.
 * @param tax The tax on the item; depends on isFlatRate ? tax : tax*price.
 * @param isFlatRate The tax on the item is fixed(true) or percentage(false).
 */
@Serializable
data class CartItem(
    val productId: String,
    val name: String,
    val quantity: Int,
    val price: Double=0.0,
    val maxStock: Int,
    val tax: Double=0.0,
    val isFlatRate: Boolean=false,
)