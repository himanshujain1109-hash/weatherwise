package com.weatherwise.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Forecast : Screen("forecast", "Forecast", Icons.Default.DateRange)
    data object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Search : Screen("search", "Search")

    companion object {
        val bottomNavItems = listOf(Home, Forecast, Favorites, Settings)
    }
}
