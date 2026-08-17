package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen(
        route = "home",
        title = "Vakitler",
        selectedIcon = Icons.Filled.AccessTime,
        unselectedIcon = Icons.Outlined.AccessTime
    )

    object Qibla : Screen(
        route = "qibla",
        title = "Kıble",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore
    )

    object QuranDua : Screen(
        route = "quran_dua",
        title = "Kur'an & Dua",
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook
    )

    object Analytics : Screen(
        route = "analytics",
        title = "Analitik",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )

    object DhikrKaza : Screen(
        route = "dhikr_kaza",
        title = "Zikir & Kaza",
        selectedIcon = Icons.Filled.TouchApp,
        unselectedIcon = Icons.Outlined.TouchApp
    )

    object GeminiAI : Screen(
        route = "gemini_ai",
        title = "İkizler Zekası",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    )

    object Settings : Screen(
        route = "settings",
        title = "Ayarlar",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}
