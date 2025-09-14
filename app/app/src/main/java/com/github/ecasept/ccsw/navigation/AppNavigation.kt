package com.github.ecasept.ccsw.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.ecasept.ccsw.data.preferences.PDSRepo
import com.github.ecasept.ccsw.ui.screens.home.HomeScreen
import com.github.ecasept.ccsw.ui.screens.login.LoginScreen
import com.github.ecasept.ccsw.ui.screens.settings.SettingsScreen
import org.koin.compose.koinInject

const val DURATION = 300

@Composable
fun AppNavigation(
    dataStore: PDSRepo = koinInject()
) {
    val navController = rememberNavController()
    val isLoggedIn = dataStore.isLoggedIn.collectAsState(initial = null).value

    // Background
    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
    ) {
        when (isLoggedIn) {
            null -> Text("Loading...")
            else -> Navigation(
                startDestination = when (isLoggedIn) {
                    true -> Destination.Home
                    false -> Destination.Login
                }, navController = navController
            )
        }
    }
}

@Composable
fun Navigation(startDestination: Destination, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(DURATION),
                initialOffset = { it / 2 }) + fadeIn(
                initialAlpha = 0f, animationSpec = tween(DURATION)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(DURATION),
                targetOffset = { it / 2 }) + fadeOut(
                targetAlpha = 0f, animationSpec = tween(DURATION)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(DURATION),
                initialOffset = { it / 2 }) + fadeIn(
                initialAlpha = 0f, animationSpec = tween(DURATION)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(DURATION),
                targetOffset = { it / 2 }) + fadeOut(
                targetAlpha = 0f, animationSpec = tween(DURATION)
            )
        }) {
        composable(
            Destination.Home.route,
        ) {
            HomeScreen(onSettingsNav = {
                navController.navigate(Destination.Settings.route)
            }, onLogoutNav = {
                navController.navigate(Destination.Login.route) {
                    popUpTo(Destination.Home.route) { inclusive = true }
                }
            })
        }
        composable(
            Destination.Settings.route,

            ) {
            SettingsScreen(onBackNav = {
                navController.navigateUp()
            })
        }
        composable(
            Destination.Login.route
        ) {
            LoginScreen(onLoginNav = {
                navController.navigate(Destination.Home.route) {
                    popUpTo(Destination.Login.route) { inclusive = true }
                }
            })
        }
    }
}