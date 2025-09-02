package com.coptimize.openinventory.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.PreferenceManager
import com.coptimize.openinventory.data.repository.AppSetupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Represents the state of the settings screen
data class SettingsUiState(
    val isAuthModeEnabled: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkAuthMode()
    }

    private fun checkAuthMode() {
        viewModelScope.launch {
            val isAuth = preferenceManager.isAuthModeEnabled()
            _uiState.update {
                it.copy(
                    isAuthModeEnabled = isAuth,
                    isLoading = false
                )
            }
        }
    }
}