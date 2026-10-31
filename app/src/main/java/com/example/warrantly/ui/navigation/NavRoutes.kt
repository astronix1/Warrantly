package com.example.warrantly.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Items : Screen("items")
    object Documents : Screen("documents")
    object Settings : Screen("settings")
    object AddProduct : Screen("add_product")
    object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: Long) = "edit_product/$productId"
    }
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Long) = "product_detail/$productId"
    }
}
