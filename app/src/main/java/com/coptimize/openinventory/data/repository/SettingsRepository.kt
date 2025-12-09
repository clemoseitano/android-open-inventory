package com.coptimize.openinventory.data.repository

interface SettingsRepository {
    fun getCurrencySymbol(): String
    fun setCurrencySymbol(symbol: String)
    fun getDefaultCurrencySymbol(): String
}