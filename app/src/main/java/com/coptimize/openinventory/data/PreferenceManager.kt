package com.coptimize.openinventory.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "app_settings"
private const val KEY_AUTH_MODE_ENABLED = "auth_mode_enabled"
private const val KEY_CURRENCY_SYMBOL = "currency_symbol"

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Get the stored currency symbol, defaulting to "$" if not set.
    fun getCurrencySymbol(): String {
        return prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$"
    }

    // Save the chosen currency symbol.
    fun setCurrencySymbol(symbol: String) {
        prefs.edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply()
    }

    fun isAuthModeEnabled(): Boolean {
        // Default to 'false' if the key doesn't exist.
        return prefs.getBoolean(KEY_AUTH_MODE_ENABLED, false)
    }

    fun setAuthModeEnabled(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTH_MODE_ENABLED, isEnabled).apply()
    }
}
