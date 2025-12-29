package com.smartkitch.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(val route: String, val icon: ImageVector, val title: String) {
    object Dashboard : NavigationItem("dashboard", Icons.Outlined.Home, "Home")
    object Inventory : NavigationItem("inventory", Icons.Outlined.Kitchen, "Inventory") // Kitchen (Fridge) is closer to "Bottle/Container" than ShoppingCart
    object Scan : NavigationItem("scan", Icons.Outlined.QrCodeScanner, "Scan")
    object Recipes : NavigationItem("recipes", Icons.Outlined.Book, "Recipes") // Book matches the image better than MenuBook (open)
    object Settings : NavigationItem("settings", Icons.Outlined.Settings, "Settings")
}
