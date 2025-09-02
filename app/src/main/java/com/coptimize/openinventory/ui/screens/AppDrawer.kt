package com.coptimize.openinventory.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
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
    viewModel: DrawerViewModel = hiltViewModel() // Inject the new ViewModel
) {
    // Build the list of drawer items dynamically based on the auth mode.
    val drawerItems = remember(viewModel.isAuthModeEnabled) {
        val allItems = mutableListOf(
            DrawerItem(Screen.MainSale, "Point of Sale", Icons.Default.PointOfSale),
            DrawerItem(Screen.ProductManagement, "Product Management", Icons.Default.Inventory),
            DrawerItem(Screen.SalesReport, "Sales Report", Icons.Default.Assessment)
        )
        // Conditionally add the "Manage Users" item
        if (viewModel.isAuthModeEnabled) {
            allItems.add(DrawerItem(Screen.UserManagement, "Manage Users", Icons.Default.People))
        }
        // Settings is always available
        allItems.add(DrawerItem(Screen.Settings, "Settings", Icons.Default.Settings))

        allItems.toList() // Return an immutable list
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalDrawerSheet {
        Column(Modifier.padding(16.dp)) {
            Text(
                "OpenInventory",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            HorizontalDivider()

            drawerItems.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(item.title) },
                    selected = currentRoute == item.screen.route,
                    onClick = {
                        navController.navigate(item.screen.route) {
                            launchSingleTop = true
                        }
                        closeDrawer()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}