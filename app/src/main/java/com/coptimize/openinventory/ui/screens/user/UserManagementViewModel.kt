package com.coptimize.openinventory.ui.screens.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.model.User
import com.coptimize.openinventory.data.repository.UserRepository
import com.coptimize.openinventory.data.repository.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {
    val currentUserId: String? = userSessionRepository.getCurrentUserId()
    val users: StateFlow<List<User>> = userRepository.getAllUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteUser(userId: String) {
        if (userId == currentUserId) {
            println("You cannot remove your own account.")
            return
        }
        viewModelScope.launch {
            userRepository.deleteUser(userId)
        }
    }
}