package com.coptimize.openinventory.ui.screens.user

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.model.User
import com.coptimize.openinventory.data.repository.UserRepository
import com.coptimize.openinventory.ui.hashPasswordSha256
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class UserEditUiState(
    val isLoading: Boolean = true,
    val isNewUser: Boolean = true,
    val saveSuccess: Boolean = false,
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedRole: String = "staff", // Default role
    val error: String? = null
)

@HiltViewModel
class UserEditViewModel @Inject constructor(
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String? = savedStateHandle["userId"]

    private val _uiState = MutableStateFlow(UserEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        if (userId != null && userId != "-1") {
            loadUser(userId)
        } else {
            _uiState.update { it.copy(isLoading = false, isNewUser = true) }
        }
    }

    private fun loadUser(id: String) {
        viewModelScope.launch {
            val user = userRepository.getUserById(id)
            if (user != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isNewUser = false,
                        username = user.username,
                        selectedRole = user.role
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User not found") }
            }
        }
    }

    fun onUsernameChange(name: String) = _uiState.update { it.copy(username = name) }
    fun onPasswordChange(pass: String) = _uiState.update { it.copy(password = pass) }
    fun onConfirmPasswordChange(pass: String) = _uiState.update { it.copy(confirmPassword = pass) }
    fun onRoleChange(role: String) = _uiState.update { it.copy(selectedRole = role) }

    fun saveUser() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.username.isBlank()) {
                _uiState.update { it.copy(error = "Username cannot be empty.") }; return@launch
            }
            if (state.isNewUser && state.password.isBlank()) {
                _uiState.update { it.copy(error = "Password is required for new users.") }; return@launch
            }
            if (state.password != state.confirmPassword) {
                _uiState.update { it.copy(error = "Passwords do not match.") }; return@launch
            }

            val passwordHash = hashPasswordSha256(state.password)

            val user = User(
                id = if (state.isNewUser) UUID.randomUUID().toString() else userId!!,
                username = state.username,
                passwordHash = passwordHash,
                role = state.selectedRole,
                // Other fields are not set from this screen
                lastLogin = null, createdAt = "", updatedAt = "", deletedAt = null
            )

            val result = if (state.isNewUser) {
                userRepository.addUser(user)
            } else {
                userRepository.updateUser(user)
            }

            result.fold(
                onSuccess = { _uiState.update { it.copy(saveSuccess = true) } },
                onFailure = { _uiState.update { it.copy(error = it.error ?: "Failed to save user.") } }
            )
        }
    }
}