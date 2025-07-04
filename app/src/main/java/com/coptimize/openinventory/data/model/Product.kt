package com.coptimize.openinventory.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"], unique = true)]
)
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val manufacturer: String?,
    val barcode: String?,
    val price: Double,
    val tax: Double?,
    @ColumnInfo(name = "tax_is_flat_rate")
    val isTaxFlatRate: Boolean,
    val quantity: Int,
    @ColumnInfo(name = "image_path") val imagePath: String?,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String,
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null,
    @ColumnInfo(name = "user_id") val userId: String? = null // Nullable for non-auth mode
)