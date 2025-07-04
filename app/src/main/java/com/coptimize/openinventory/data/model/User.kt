package com.coptimize.openinventory.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey val id: String,
    val username: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    val role: String,
    @ColumnInfo(name = "last_login") val lastLogin: String?,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String,
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)