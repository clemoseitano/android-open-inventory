package com.coptimize.openinventory.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.repository.MigrationRepository
import com.coptimize.openinventory.ui.hashPasswordSha256
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isSetupMode: Boolean = false,
    val isLoading: Boolean = false,
    val setupError: String? = null,
    val needsRestart: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val migrationRepository: MigrationRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkIfSetupIsNeeded()
    }

    private fun checkIfSetupIsNeeded() {
        val nonAuthDbFile = context.getDatabasePath("inventory.db")
        val authDbFile = context.getDatabasePath("inventory.db.auth")
        // Setup is needed if the old DB exists and the new one doesn't.
        _uiState.update { it.copy(isSetupMode = nonAuthDbFile.exists() && !authDbFile.exists()) }
    }

    fun runMigration(username: String, password_raw: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, setupError = null) }

            // Basic validation
            if (username.length < 4 || password_raw.length < 6) {
                _uiState.update { it.copy(isLoading = false, setupError = "Username must be 4+ chars, password 6+.") }
                return@launch
            }

            val passwordHash = hashPasswordSha256(password_raw)

            val result = migrationRepository.performMigration(username, passwordHash)
            result.fold(
                onSuccess = {
                    // Success! Tell the UI the app needs to restart.
                    _uiState.update { it.copy(isLoading = false, needsRestart = true) }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, setupError = "Migration failed: $it") }
                }
            )
        }
    }
}