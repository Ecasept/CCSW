package com.github.ecasept.ccsw.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object Settings : Destination("settings")
}
