package com.coptimize.openinventory.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth_screen")
    object MainSale : Screen("main_sale_screen")
    object ProductManagement : Screen("product_management_screen")
    object ProductEdit : Screen("product_edit_screen/{productId}") {
        fun createRoute(productId: Long?) = "product_edit_screen/${productId ?: -1}"
    }
    object SalesReport : Screen("sales_report_screen")
    object UserManagement : Screen("user_management_screen")
    object Settings : Screen("settings_screen")
}