package com.coptimize.openinventory.data.model

data class SavedCart(
    val id: String,
    val customerId: String?,
    val items: String?, // JSON string
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val userId: String? = null
)