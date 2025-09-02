package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.PreferenceManager
import com.coptimize.openinventory.ui.screens.StartDestination
import javax.inject.Inject
import javax.inject.Singleton

interface AppSetupRepository {
    suspend fun getInitialStartDestination(): StartDestination
}

@Singleton
class AppSetupRepositoryImpl @Inject constructor(
    // Inject the new PreferenceManager
    private val preferenceManager: PreferenceManager
) : AppSetupRepository {

    override suspend fun getInitialStartDestination(): StartDestination {
        // No need for withContext, SharedPreferences is fast enough.
        return if (preferenceManager.isAuthModeEnabled()) {
            StartDestination.Login
        } else {
            StartDestination.Main
        }
    }
}