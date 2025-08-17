package com.coptimize.openinventory.data.model

// This is the one and only "Product" class your UI and ViewModels will ever see.
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val manufacturer: String?,
    val barcode: String?,
    val price: Double,
    val tax: Double?,
    val isTaxFlatRate: Boolean,
    val quantity: Int,
    val imagePath: String?,
    val section: String?,
    val shelf: String?,
    val createdAt: String?=null,
    val updatedAt: String?=null,
    val deletedAt: String?=null,
    val userId: String? // Nullable to support both modes
)