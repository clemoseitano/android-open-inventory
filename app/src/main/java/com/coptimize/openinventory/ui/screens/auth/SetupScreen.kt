package com.coptimize.openinventory.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    // This flag determines if the back arrow is shown
    showAppBar: Boolean,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalActivity.current as Activity

    // Handle the "needsRestart" event from the ViewModel
    if (uiState.needsRestart) {
        AlertDialog(
            onDismissRequest = { /* Don't allow dismissal */ },
            title = { Text("Setup Successful") },
            text = { Text("Security has been enabled. The app will now close. Please reopen it to log in.") },
            confirmButton = {
                TextButton(onClick = { context.finish() }) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            // Only show the app bar if navigating from Settings
            if (showAppBar) {
                TopAppBar(
                    title = { Text("Enable Security") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SetupForm(
                isLoading = uiState.isLoading,
                error = uiState.setupError,
                onSetupClick = viewModel::runMigration
            )
        }
    }
}

// This is the reusable form component, unchanged
@Composable
fun SetupForm(
    isLoading: Boolean,
    error: String?,
    onSetupClick: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp)) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Create Superuser Account", style = MaterialTheme.typography.headlineMedium)
            Text("This administrator account will secure your data and enable multi-user features.", textAlign = TextAlign.Center)
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Admin Username") })
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = { onSetupClick(username, password) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Create Account & Secure Data")
                }
            }
        }
    }
}