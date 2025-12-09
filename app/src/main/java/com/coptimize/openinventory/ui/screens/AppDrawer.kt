package com.coptimize.openinventory.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.coptimize.openinventory.navigation.Screen

data class DrawerItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

@Composable
fun AppDrawerContent(
    navController: NavController,
    closeDrawer: () -> Unit,
    viewModel: DrawerViewModel = hiltViewModel()
) {
    val passwordPromptState by viewModel.passwordPromptState.collectAsState()

    // Show the password prompt dialog
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

    val drawerItems = remember(viewModel.isAuthModeEnabled, viewModel.currentUserRole) {
        val allItems = mutableListOf(
            DrawerItem(Screen.MainSale, "Point of Sale", Icons.Default.PointOfSale)
        )

        if (viewModel.isAuthModeEnabled) {
            val role = viewModel.currentUserRole
            if (role == "admin" || role == "superadmin") {
                allItems.add(DrawerItem(Screen.ProductManagement, "Product Management", Icons.Default.Inventory))
            } else if (role == "staff") {
                allItems.add(DrawerItem(Screen.ProductManagement, "Product List", Icons.Default.Inventory))
            }

            allItems.add(DrawerItem(Screen.SalesReport, "Sales Report", Icons.Default.Assessment))

            if (role == "superadmin") {
                allItems.add(DrawerItem(Screen.UserManagement, "Manage Users", Icons.Default.People))
            }

        } else {
            // In non-auth mode, the user is effectively an admin without a password.
            allItems.add(DrawerItem(Screen.ProductManagement, "Product Management", Icons.Default.Inventory))
            allItems.add(DrawerItem(Screen.SalesReport, "Sales Report", Icons.Default.Assessment))
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
                        // Password prompt for sensitive screens in auth mode
                        val isSensitive = viewModel.isAuthModeEnabled &&
                                ((item.screen == Screen.ProductManagement && role != "staff") ||
                                        (item.screen == Screen.UserManagement))

                        if (isSensitive) {
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