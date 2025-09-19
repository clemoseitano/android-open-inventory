package com.coptimize.openinventory.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.coptimize.openinventory.data.repository.UserSessionRepository
import com.coptimize.openinventory.navigation.Screen

// DrawerItem data class is unchanged
data class DrawerItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

@Composable
fun AppDrawerContent(
    navController: NavController,
    closeDrawer: () -> Unit,
    viewModel: DrawerViewModel = hiltViewModel()) {
    val passwordPromptState by viewModel.passwordPromptState.collectAsState()

    // Show the password prompt dialog based on the ViewModel's state
    if (passwordPromptState is PasswordPromptState.Visible || passwordPromptState is PasswordPromptState.Failure) {
        PasswordPromptDialog(
            title = "Admin Access Required",
            error = (passwordPromptState as? PasswordPromptState.Failure)?.message,
            onDismiss = { viewModel.hidePasswordPrompt() },
            onConfirm = { password ->
                val screenToNavigate = viewModel.verifyPasswordAndGetNavigation(password)
                if (screenToNavigate != null) {
                    navController.navigate(screenToNavigate.route) { launchSingleTop = true }
                    closeDrawer()
                }
            }
        )
    }
    // Build the list of drawer items dynamically based on the auth mode.
    val drawerItems = remember(viewModel.currentUserRole, viewModel.isAuthModeEnabled) {
        val allItems = mutableListOf(
            DrawerItem(Screen.MainSale, "Point of Sale", Icons.Default.PointOfSale)
        )

        // Product Management / Product List
        val role = viewModel.currentUserRole
        if (role == "admin" || role == "superadmin") {
            allItems.add(DrawerItem(Screen.ProductManagement, "Product Management", Icons.Default.Inventory))
        } else if (role == "staff") {
            allItems.add(DrawerItem(Screen.ProductManagement, "Product List", Icons.Default.Inventory))
        }

        allItems.add(DrawerItem(Screen.SalesReport, "Sales Report", Icons.Default.Assessment))

        // User Management is for superadmin only
        if (role == "superadmin") {
            allItems.add(DrawerItem(Screen.UserManagement, "Manage Users", Icons.Default.People))
        }

        allItems.add(DrawerItem(Screen.Settings, "Settings", Icons.Default.Settings))

        allItems.toList()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalDrawerSheet {
        Column(Modifier.padding(16.dp)) {
            Text("OpenInventory", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
            HorizontalDivider()
            drawerItems.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(item.title) },
                    selected = currentRoute == item.screen.route,
                    onClick = {
                        val role = viewModel.currentUserRole
                        val isSensitive = (item.screen == Screen.ProductManagement && role != "staff") ||
                                (item.screen == Screen.UserManagement)

                        if (isSensitive) {
                            // Trigger the dialog via the ViewModel
                            viewModel.showPasswordPrompt(item.screen)
                        } else {
                            navController.navigate(item.screen.route) { launchSingleTop = true }
                            closeDrawer()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}