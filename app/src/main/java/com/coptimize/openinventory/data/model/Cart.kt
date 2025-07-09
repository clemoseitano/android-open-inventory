package com.coptimize.openinventory.data.model

import kotlinx.serialization.Serializable

/**
 * Represents the entire state of the shopping cart at the moment of checkout.
 * This class mirrors the data structure managed by the in-memory CartModel
 * and passed as `saleData` and `cartItems` in the Qt C++ code.
 *
 * @param items A list of all CartItem objects in the cart.
 * @param subtotal The total price before taxes and discounts.
 * @param tax The total tax amount for the sale.
 * @param discount The total discount amount for the sale.
 * @param total The final amount to be paid (subtotal + tax - discount).
 * @param paidAmount The amount of cash or payment received from the customer.
 * @param changeAmount The change to be given back to the customer.
 * @param savedCartId An optional ID if this sale originated from a saved cart.
 */
@Serializable
data class Cart(
    val items: List<CartItem>,
    val subtotal: Double,
    val tax: Double,
    val discount: Double,
    val total: Double,
    val paidAmount: Double,
    val changeAmount: Double,
    val savedCartId: String? = null
)