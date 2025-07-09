package com.coptimize.openinventory.data.model

data class User(
    val id: String,
    val username: String,
    val passwordHash: String,
    val role: String,
    val lastLogin: String?,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null
)