package com.coptimize.openinventory.data.repository

interface MigrationRepository {
    suspend fun performMigration(username: String, passwordHash: String): Result<Unit>
}