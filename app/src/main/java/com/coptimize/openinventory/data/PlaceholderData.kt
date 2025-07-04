package com.coptimize.openinventory.data

import java.util.Date

// Placeholder data classes to make UI code compile
data class Product(
    val id: Long,
    val name: String,
    val price: Double,
    val stockQuantity: Int,
    val barcode: String,
    val image: String? = null
)

data class CartItem(
    val product: Product,
    var quantity: Int
)

data class User(
    val id: Long,
    val name: String,
    val role: String,
    val lastLogin: Date
)

data class Sale(
    val id: Long,
    val customerName: String,
    val totalAmount: Double,
    val date: Date,
    val items: List<SaleItem>
)

data class SaleItem(
    val productName: String,
    val quantity: Int,
    val unitPrice: Double
)

// Sample data for previews and initial implementation
object SampleData {
    val products = List(20) {
        Product(it.toLong(), "Product Name ${it + 1}", 19.99 + it, 10 + it, "123456789${it}")
    }
    val cartItems = mutableListOf(
        CartItem(products[0], 2),
        CartItem(products[1], 1),
        CartItem(products[3], 5)
    )
    val users = List(10) {
        User(it.toLong(), "User ${it+1}", if (it % 3 == 0) "Admin" else "Staff", Date())
    }
    val sales = List(5) { saleId ->
        Sale(
            id = saleId.toLong(),
            customerName = "Customer ${saleId + 1}",
            totalAmount = (saleId + 1) * 123.45,
            date = Date(),
            items = List(3) { itemIdx ->
                SaleItem("Sold Item ${itemIdx + 1}", itemIdx + 1, 15.50)
            }
        )
    }
}