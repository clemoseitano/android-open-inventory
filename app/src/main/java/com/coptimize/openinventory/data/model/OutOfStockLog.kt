package com.coptimize.openinventory.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "out_of_stock_log")
data class OutOfStockLog(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "product_id") val productId: Long,
    @ColumnInfo(name = "product_name") val productName: String,
    @ColumnInfo(name = "attempted_quantity") val attemptedQuantity: Int,
    @ColumnInfo(name = "available_quantity") val availableQuantity: Int,
    val resolved: Boolean,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String,
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    val updatedAt: String,
    @ColumnInfo(name = "user_id") val userId: String? = null
)