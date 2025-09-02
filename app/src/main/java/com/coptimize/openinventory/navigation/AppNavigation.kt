package com.coptimize.openinventory.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coptimize.openinventory.ui.screens.RouterScreen
import com.coptimize.openinventory.ui.screens.auth.LoginScreen
import com.coptimize.openinventory.ui.screens.auth.SetupScreen
import com.coptimize.openinventory.ui.screens.product.ProductEditScreen
import com.coptimize.openinventory.ui.screens.product.ProductManagementScreen
import com.coptimize.openinventory.ui.screens.report.SalesReportScreen
import com.coptimize.openinventory.ui.screens.sale.MainSaleScreen
import com.coptimize.openinventory.ui.screens.settings.SettingsScreen
import com.coptimize.openinventory.ui.screens.user.UserEditScreen
import com.coptimize.openinventory.ui.screens.user.UserManagementScreen

@Composable
fun AppNavigation(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()

    // The start destination is always the RouterScreen.
    NavHost(navController = navController, startDestination = Screen.Router.route) {

        // 1. Router Screen: Determines where to go next.
        composable(Screen.Router.route) {
            RouterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Router.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.MainSale.route) {
                        popUpTo(Screen.Router.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Login Screen: Only shown if auth is already enabled.
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.MainSale.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Setup Screen: ONLY reachable from the Settings menu.
        composable(Screen.Setup.route) {
            SetupScreen(
                showAppBar = true, // Always show the app bar with back button
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // --- Main Application Screens ---
        composable(Screen.MainSale.route) {
            MainSaleScreen(windowSizeClass = windowSizeClass, navController = navController)
        }
        composable(Screen.ProductManagement.route) {
            ProductManagementScreen(windowSizeClass = windowSizeClass, navController = navController)
        }
        composable(Screen.ProductEdit.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
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
        composable(Screen.UserEdit.route) {
            UserEditScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(windowSizeClass = windowSizeClass, navController = navController)
        }
    }
}