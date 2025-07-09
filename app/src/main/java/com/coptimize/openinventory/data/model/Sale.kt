package com.coptimize.openinventory.data.model

import com.coptimize.openinventory.data.Sales
import com.coptimize.openinventory.data.auth.Sales as AuthSales

data class Sale(
    val id: String,
    val customerId: String?,
    val customerName: String?="Walk-in Customer",
    val date: String,
    val subtotal: Double,
    val tax: Double,
    val discount: Double,
    val total: Double,
    val paidAmount: Double,
    val changeAmount: Double,
    val createdAt: String,
    val updatedAt: String,
    val userId: String? = null
)

/**
 * Maps the SQLDelight-generated 'Sales' data class (from the non-auth schema)
 * to the clean 'Sale' domain model.
 */
fun Sales.toDomain(): Sale {
    return Sale(
        id = this.id,
        customerId = this.customer_id.toString(),
        date = this.date?:"",
        subtotal = this.subtotal,
        tax = this.tax,
        discount = this.discount,
        total = this.total,
        paidAmount = this.paid_amount,
        changeAmount = this.change_amount,
        createdAt = this.created_at?:"",
        updatedAt = this.updated_at?:"",
        userId = null
    )
}

/**
 * Maps the SQLDelight-generated 'Sales' data class (from the auth schema)
 * to the clean 'Sale' domain model.
 */
fun AuthSales.toDomain(): Sale {
    return Sale(
        id = this.id,
        customerId = this.customer_id.toString(),
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
}

// Parses the aggregated string from the new SQL query
fun parseAggregatedItems(itemsString: String?): List<SaleItem> {
    if (itemsString.isNullOrBlank()) return emptyList()
    return itemsString.split("|||").mapNotNull { itemPart ->
        try {
            val parts = itemPart.split('|')
            SaleItem(
                id = parts[0],
                saleId = "", // Not needed here
                productId = parts[1],
                productName = parts[2],
                quantity = parts[3].toInt(),
                price = parts[4].toDouble(),
                createdAt = "",
                updatedAt = ""
            )
        } catch (e: Exception) {
            null // Gracefully skip malformed item parts
        }
    }
}