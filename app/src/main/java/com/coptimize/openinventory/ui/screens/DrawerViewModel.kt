package com.coptimize.openinventory.ui.screens

import androidx.lifecycle.ViewModel
import com.coptimize.openinventory.data.PreferenceManager
import com.coptimize.openinventory.data.repository.UserSessionRepository
import com.coptimize.openinventory.navigation.Screen
import com.coptimize.openinventory.ui.hashPasswordSha256
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// Represents the state of the password prompt dialog
sealed class PasswordPromptState {
    abstract val pendingScreen: Screen? // Add this to the base class

    object Hidden : PasswordPromptState() {
        override val pendingScreen: Screen? = null
    }
    data class Visible(override val pendingScreen: Screen) : PasswordPromptState()
    data class Verifying(override val pendingScreen: Screen) : PasswordPromptState()
    data class Failure(override val pendingScreen: Screen, val message: String) : PasswordPromptState()
}

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    val isAuthModeEnabled: Boolean = preferenceManager.isAuthModeEnabled()
    val currentUserRole: String? = userSessionRepository.getCurrentUserRole()

    private val _passwordPromptState = MutableStateFlow<PasswordPromptState>(PasswordPromptState.Hidden)
    val passwordPromptState = _passwordPromptState.asStateFlow()

    fun showPasswordPrompt(screen: Screen) {
        _passwordPromptState.value = PasswordPromptState.Visible(screen)
    }

    fun hidePasswordPrompt() {
        _passwordPromptState.value = PasswordPromptState.Hidden
    }

    /**
     * Verifies the provided password against the current user's stored hash.
     * @param password The raw password string entered by the user.
     * @return The Screen to navigate to on success, or null on failure.
     */
    fun verifyPasswordAndGetNavigation(password: String): Screen? {
        // --- THIS IS THE CORRECTED LOGIC ---

        // 1. Get the current state and the screen we need to navigate to.
        val currentPendingScreen = _passwordPromptState.value.pendingScreen
        if (currentPendingScreen == null) {
            // Should not happen if the dialog is visible, but a good safety check.
            _passwordPromptState.value = PasswordPromptState.Hidden
            return null
        }

        // 2. Immediately update the UI state to "Verifying".
        _passwordPromptState.value = PasswordPromptState.Verifying(currentPendingScreen)

        // 3. Perform the verification logic.
        val storedHash = userSessionRepository.getCurrentUserPasswordHash()
        if (storedHash == null) {
            _passwordPromptState.value = PasswordPromptState.Failure(currentPendingScreen, "User session not found.")
            return null
        }

        val enteredPasswordHash = hashPasswordSha256(password)

        // 4. Update the state based on the result and return the navigation target.
        return if (enteredPasswordHash == storedHash) {
            _passwordPromptState.value = PasswordPromptState.Hidden // Reset on success
            currentPendingScreen // Return the screen to navigate to
        } else {
            _passwordPromptState.value = PasswordPromptState.Failure(currentPendingScreen, "Incorrect password.")
            null // Return null on failure
        }
    }
}