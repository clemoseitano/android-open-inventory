package com.coptimize.openinventory.ui.screens

import androidx.lifecycle.ViewModel
import com.coptimize.openinventory.data.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    // Expose the auth mode status directly.
    // No need for a StateFlow here since this value won't change during the app's session.
    val isAuthModeEnabled: Boolean = preferenceManager.isAuthModeEnabled()
}