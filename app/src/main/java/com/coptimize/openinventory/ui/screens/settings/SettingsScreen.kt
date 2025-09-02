package com.coptimize.openinventory.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.coptimize.openinventory.navigation.Screen
import com.coptimize.openinventory.ui.screens.report.VerticalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isHandset = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    var selectedCategory by remember { mutableStateOf("General") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (isHandset) {
                SettingsHandsetLayout(
                    modifier = Modifier.padding(paddingValues),
                    isAuthModeEnabled = uiState.isAuthModeEnabled,
                    navController = navController
                )
            } else {
                SettingsTabletLayout(
                    modifier = Modifier.padding(paddingValues),
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    isAuthModeEnabled = uiState.isAuthModeEnabled,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun SettingsHandsetLayout(
    modifier: Modifier = Modifier,
    isAuthModeEnabled: Boolean,
    navController: NavController // Pass NavController here
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { SettingItem(Icons.Default.Business, "Business Info", "Name, Address, Contact") {} }
        item { SettingItem(Icons.Default.Receipt, "Receipt Template", "Header and footer notes") {} }
        item { SettingItem(Icons.Default.Print, "Printer", "Setup Bluetooth/Wi-Fi printer") {} }

        // Conditionally display the security option based on the ViewModel's state
        if (!isAuthModeEnabled) {
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingItem(
                    icon = Icons.Default.Security,
                    title = "Enable Security & Multi-User",
                    subtitle = "Set up a password and enable user accounts",
                    onClick = { navController.navigate(Screen.Setup.route) } // Navigate to the Setup screen
                )
            }
        }
    }
}

@Composable
fun SettingsTabletLayout(
    modifier: Modifier = Modifier,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    isAuthModeEnabled: Boolean,
    navController: NavController
) {
    Row(modifier.fillMaxSize()) {
        // Left Pane: Category List
        LazyColumn(
            modifier = Modifier.weight(0.3f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item { SettingsCategoryItem("General", selectedCategory == "General") { onCategorySelected("General") } }
            item { SettingsCategoryItem("Receipt", selectedCategory == "Receipt") { onCategorySelected("Receipt") } }
            item { SettingsCategoryItem("Printer", selectedCategory == "Printer") { onCategorySelected("Printer") } }
        }
        VerticalDivider()
        // Right Pane: Detail View
        Box(
            modifier = Modifier
                .weight(0.7f)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedCategory) {
                "General" -> GeneralSettingsDetailView(isAuthModeEnabled, navController)
                "Receipt" -> ReceiptSettingsDetailView()
                "Printer" -> PrinterSettingsDetailView()
            }
        }
    }
}

// A new detail view for general settings to hold the security option on tablets
@Composable
fun GeneralSettingsDetailView(isAuthModeEnabled: Boolean, navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("General Settings", style = MaterialTheme.typography.titleLarge)
        // Conditionally show the security option
        if (!isAuthModeEnabled) {
            Card(onClick = { navController.navigate(Screen.Setup.route) }) {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Security, null) },
                    headlineContent = { Text("Enable Security & Multi-User") },
                    supportingContent = { Text("Set up a password to protect your data and enable multiple user accounts.") }
                )
            }
        } else {
            Text("Security is enabled. Manage users from the main menu.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


@Composable
fun ReceiptSettingsDetailView() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Receipt Settings", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = "My Awesome Store", onValueChange = {}, label = { Text("Business Name") })
        OutlinedTextField(
            value = "123 Main Street\nAnytown, USA 12345",
            onValueChange = {},
            label = { Text("Business Address") },
            minLines = 3
        )
        // Live Preview
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "*************************\n" +
                        "****** RECEIPT ******\n" +
                        "  My Awesome Store\n" +
                        "*************************",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@Composable
fun PrinterSettingsDetailView() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Printer Settings", style = MaterialTheme.typography.titleLarge)
        Text("Connect to a Bluetooth or Wi-Fi thermal printer.")
        Button(onClick = { /* TODO: Open BT device picker */ }) {
            Text("Scan for Printers")
        }
    }
}


@Composable
fun SettingItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { Icon(icon, contentDescription = null) },
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) }
    )
    HorizontalDivider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCategoryItem(title: String, isSelected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(title) },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
}