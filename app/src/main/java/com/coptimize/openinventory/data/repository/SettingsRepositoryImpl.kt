package com.coptimize.openinventory.data.repository

import com.coptimize.openinventory.data.PreferenceManager
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferenceManager: PreferenceManager
) : SettingsRepository {
    override fun getCurrencySymbol(): String {
        return preferenceManager.getCurrencySymbol()
    }

    override fun setCurrencySymbol(symbol: String) {
        preferenceManager.setCurrencySymbol(symbol)
    }

    /**
     * Tries to infer the local currency symbol from the device's locale.
     * Falls back to "$" if inference fails.
     */
    override fun getDefaultCurrencySymbol(): String {
        return try {
            Currency.getInstance(Locale.getDefault()).symbol
        } catch (e: Exception) {
            "$" // Default fallback
        }
    }
}