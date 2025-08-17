package com.coptimize.openinventory.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.coptimize.openinventory.navigation.Screen

// A data class to represent each item in our drawer
data class DrawerItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

@Composable
fun AppDrawerContent(
    navController: NavController,
    closeDrawer: () -> Unit
) {
    // List of all screens we want to show in the drawer
    val drawerItems = listOf(
        DrawerItem(Screen.MainSale, "Point of Sale", Icons.Default.PointOfSale),
        DrawerItem(Screen.ProductManagement, "Product Management", Icons.Default.Inventory),
        DrawerItem(Screen.SalesReport, "Sales Report", Icons.Default.Assessment),
        DrawerItem(Screen.UserManagement, "Manage Users", Icons.Default.People),
        DrawerItem(Screen.Settings, "Settings", Icons.Default.Settings)
    )

    // Get the current route to highlight the selected item
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
                        // Navigate to the screen and then close the drawer
                        navController.navigate(item.screen.route) {
                            // Avoid building up a large back stack
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