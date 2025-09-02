package com.coptimize.openinventory.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.coptimize.openinventory.data.auth.AuthDb
import com.coptimize.openinventory.data.auth.Users
import com.coptimize.openinventory.data.model.User
import com.coptimize.openinventory.ui.hashPasswordSha256
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class AuthUserRepositoryImpl @Inject constructor(
    private val db: AuthDb
) : UserRepository {

    // Helper to map the SQLDelight-generated class to your clean domain model
    private fun toDomain(user: Users): User {
        return User(
            id = user.id,
            username = user.username,
            passwordHash = user.password_hash, // It's good practice to map the hash
            role = user.role,
            lastLogin = user.last_login,
            createdAt = user.created_at ?: "",
            updatedAt = user.updated_at ?: "",
            deletedAt = user.deleted_at,
        )
    }

    override fun getAllUsers(): Flow<List<User>> {
        return db.userQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { users -> users.map { toDomain(it) } }
    }

    override suspend fun deleteUser(userId: String) {
        withContext(Dispatchers.IO) {
            db.userQueries.softDelete(userId)
        }
    }

    override suspend fun updateLastLogin(userId: String) {
        withContext(Dispatchers.IO) {
            db.userQueries.updateLastLogin(userId)
        }
    }

    override suspend fun getUserById(userId: String): User? {
        return withContext(Dispatchers.IO) {
            db.userQueries.selectById(userId = userId).executeAsOneOrNull()?.let { toDomain(it) }
        }
    }

    override suspend fun authenticate(username: String, passwordRaw: String): Result<User> {
        require(username.isNotBlank()) { "Username cannot be blank." }
        require(passwordRaw.isNotBlank()) { "Password cannot be blank." }

        return withContext(Dispatchers.IO) {
            try {
                val userFromDb = db.userQueries.selectForLogin(username).executeAsOneOrNull()
                    ?: return@withContext Result.failure(Exception("Invalid username or password."))

                val providedPasswordHash = hashPasswordSha256(passwordRaw)

                if (userFromDb.password_hash == providedPasswordHash) {
                    db.userQueries.updateLastLogin(userFromDb.id)
                    val authUserFromDb = db.userQueries.selectById(userFromDb.id).executeAsOneOrNull()
                    assert(authUserFromDb!=null)
                    Result.success(toDomain(authUserFromDb!!))
                } else {
                    // If passwords don't match, return a clear failure.
                    Result.failure(Exception("Invalid username or password."))
                }
            } catch (e: Exception) {
                // Catch any other potential exceptions (e.g., database connection issues).
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    override suspend fun addUser(user: User): Result<User> {
        // Using runCatching is a concise way to handle potential exceptions.
        return runCatching {
            withContext(Dispatchers.IO) {
                db.userQueries.insert(
                    username = user.username,
                    password_hash = user.passwordHash,
                    role = user.role
                )
                val newUser = db.userQueries.selectForLogin(user.username).executeAsOne()
                user.copy(id= newUser.id)
            }
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.IO) {
                db.transaction {
                    db.userQueries.update(
                        username = user.username,
                        role = user.role,
                        id = user.id
                    )
                    // Only update the password if a new one was provided (it's not blank)
                    if (user.passwordHash.isNotBlank()) {
                        db.userQueries.updatePassword(
                            password_hash = user.passwordHash,
                            id = user.id
                        )
                    }
                }
            }
        }
    }
}