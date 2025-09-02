package com.coptimize.openinventory.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.repository.AppSetupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Simplified destinations: Loading, Login, or Main
sealed class StartDestination {
    object Loading : StartDestination()
    object Login : StartDestination()
    object Main : StartDestination()
}

@HiltViewModel
class RouterViewModel @Inject constructor(
    private val appSetupRepository: AppSetupRepository
) : ViewModel() {
    private val _startDestination = MutableStateFlow<StartDestination>(StartDestination.Loading)
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            _startDestination.value = appSetupRepository.getInitialStartDestination()
        }
    }
}

@Composable
fun RouterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: RouterViewModel = hiltViewModel()
) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()

    LaunchedEffect(startDestination) {
        when (startDestination) {
            StartDestination.Login -> onNavigateToLogin()
            StartDestination.Main -> onNavigateToMain()
            StartDestination.Loading -> { /* Wait */ }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}