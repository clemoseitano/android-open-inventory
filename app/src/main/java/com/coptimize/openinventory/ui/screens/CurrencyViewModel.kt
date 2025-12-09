package com.coptimize.openinventory.ui

import androidx.lifecycle.ViewModel
import com.coptimize.openinventory.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _currencySymbol = MutableStateFlow(settingsRepository.getCurrencySymbol())
    val currencySymbol = _currencySymbol.asStateFlow()
}