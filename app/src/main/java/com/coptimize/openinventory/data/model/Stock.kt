package com.coptimize.openinventory.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "stocks",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Stock(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "product_id", index = true) val productId: String,
    val supplier: String?,
    @ColumnInfo(name = "supplier_contact") val supplierContact: String?,
    @ColumnInfo(name = "unit_price") val unitPrice: Double,
    @ColumnInfo(name = "purchase_price") val purchasePrice: Double,
    @ColumnInfo(name = "purchase_date") val purchaseDate: String?,
    @ColumnInfo(name = "expiry_date") val expiryDate: String?,
    val quantity: Int,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String,
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null,
    @ColumnInfo(name = "user_id") val userId: String? = null
)