package com.coptimize.openinventory.navigation

sealed class Screen(val route: String) {
    object Router : Screen("router_screen")
    object Login : Screen("login_screen")
    object Setup : Screen("setup_screen")
    object MainSale : Screen("main_sale_screen")
    object ProductManagement : Screen("product_management_screen")
    object ProductEdit : Screen("product_edit_screen/{productId}") {
        fun createRoute(productId: Long?) = "product_edit_screen/${productId ?: -1}"
    }
    object SalesReport : Screen("sales_report_screen")
    object UserManagement : Screen("user_management_screen")
    object UserEdit : Screen("user_edit_screen/{userId}") {
        fun createRoute(userId: String?) = "user_edit_screen/${userId ?: -1}"
    }
    object Settings : Screen("settings_screen")
}