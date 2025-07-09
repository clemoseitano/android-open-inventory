package com.coptimize.openinventory.data.model

data class Customer(
    val id: String,
    val name: String?,
    val contact: String?,
    val paymentMethod: String?,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null
)