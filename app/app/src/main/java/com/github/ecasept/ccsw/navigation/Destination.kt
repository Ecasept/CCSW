package com.github.ecasept.ccsw.navigation

sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object Settings : Destination("settings")
    data object Login : Destination("login")
}
