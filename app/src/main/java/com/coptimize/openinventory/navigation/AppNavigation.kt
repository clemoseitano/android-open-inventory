package com.coptimize.openinventory.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coptimize.openinventory.ui.screens.auth.AuthScreen
import com.coptimize.openinventory.ui.screens.sale.MainSaleScreen
import com.coptimize.openinventory.ui.screens.product.ProductEditScreen
import com.coptimize.openinventory.ui.screens.product.ProductManagementScreen
import com.coptimize.openinventory.ui.screens.report.SalesReportScreen
import com.coptimize.openinventory.ui.screens.settings.SettingsScreen
import com.coptimize.openinventory.ui.screens.user.UserManagementScreen

@Composable
fun AppNavigation(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Auth.route) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.MainSale.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.MainSale.route) {
            MainSaleScreen(windowSizeClass = windowSizeClass, navController = navController)
        }
        composable(Screen.ProductManagement.route) {
            ProductManagementScreen(windowSizeClass = windowSizeClass, navController = navController)
        }
        composable(Screen.ProductEdit.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull()
            ProductEditScreen(
                productId = productId,
                windowSizeClass = windowSizeClass,
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(Screen.SalesReport.route) {
            SalesReportScreen(windowSizeClass = windowSizeClass, navController = navController)
        }
        composable(Screen.UserManagement.route) {
            UserManagementScreen(windowSizeClass = windowSizeClass, navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(windowSizeClass = windowSizeClass, navController = navController)
        }
    }
}