package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the state of the currently logged-in user session.
 * This is a singleton that lives for the entire application lifecycle.
 */
@Singleton
class UserSessionRepository @Inject constructor() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    /**
     * Starts a new session after a successful login.
     * @param user The User object returned from the authentication process.
     */
    fun startSession(user: User) {
        _currentUser.value = user
    }

    /**
     * Ends the current session upon logout.
     */
    fun endSession() {
        _currentUser.value = null
    }

    /**
     * A simple synchronous way to get the current user's ID.
     * This is what other repositories will use.
     * @return The current user's ID, or null if no one is logged in.
     */
    fun getCurrentUserId(): String? {
        return _currentUser.value?.id
    }

    fun getCurrentUserRole(): String? {
        return _currentUser.value?.role
    }

    fun getCurrentUserPasswordHash(): String? {
        return _currentUser.value?.passwordHash
    }
}