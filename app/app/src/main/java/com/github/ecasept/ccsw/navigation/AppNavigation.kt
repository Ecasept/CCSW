package com.github.ecasept.ccsw.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.ecasept.ccsw.ui.screens.home.HomeScreen
import com.github.ecasept.ccsw.ui.screens.settings.SettingsScreen

const val DURATION = 300

@Composable
fun AppNavigation(
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val startDestination = Destination.Home

    // Background
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        NavHost(
            navController = navController,
            startDestination = startDestination.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(DURATION),
                    initialOffset = { it/2 }
                ) + fadeIn(
                    initialAlpha = 0f,
                    animationSpec = tween(DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(DURATION),
                    targetOffset = { it/2 }
                ) + fadeOut(
                    targetAlpha = 0f,
                    animationSpec = tween(DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(DURATION),
                    initialOffset = { it/2 }
                ) + fadeIn(
                    initialAlpha = 0f,
                    animationSpec = tween(DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(DURATION),
                    targetOffset = { it/2 }
                ) + fadeOut(
                    targetAlpha = 0f,
                    animationSpec = tween(DURATION)
                )
            }
        ) {
            composable(
                Destination.Home.route,

                ) {
                HomeScreen(
                    onPrefClick = {
                        navController.navigate(Destination.Settings.route)
                    },
                    onLogoutClick = onLogout
                )
            }
            composable(
                Destination.Settings.route,

                ) {
                SettingsScreen(onBackClick = {
                    navController.navigateUp()
                })
            }
        }
    }
}