package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Create : Screen("create", "Create", Icons.Default.AddCircle)
    object Customize : Screen("customize", "Customize", Icons.Default.AddCircle)
    object Preview : Screen("preview", "Preview", Icons.Default.AddCircle)
    object Scan : Screen("scan", "Scan", Icons.Default.QrCodeScanner)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Create,
    Screen.Scan,
    Screen.History,
    Screen.Settings
)
