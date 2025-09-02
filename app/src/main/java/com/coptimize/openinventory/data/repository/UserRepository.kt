package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAllUsers(): Flow<List<User>>
    suspend fun deleteUser(userId: String)
    suspend fun updateLastLogin(userId: String)
    suspend fun getUserById(userId: String): User?
    suspend fun authenticate(username: String, passwordRaw:String): Result<User>
    suspend fun addUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<Unit>
}