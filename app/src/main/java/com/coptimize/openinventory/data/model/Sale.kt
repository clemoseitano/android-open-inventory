package com.coptimize.openinventory.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Sale(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "customer_id", index = true) val customerId: String?,
    @ColumnInfo(name = "date") val date: String,
    val subtotal: Double,
    val tax: Double,
    val discount: Double,
    val total: Double,
    @ColumnInfo(name = "paid_amount") val paidAmount: Double,
    @ColumnInfo(name = "change_amount") val changeAmount: Double,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String,
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    val updatedAt: String,
    @ColumnInfo(name = "user_id") val userId: String? = null // Nullable for non-auth mode
)