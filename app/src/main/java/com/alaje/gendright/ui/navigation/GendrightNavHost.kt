package com.alaje.gendright.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alaje.gendright.ui.onboarding.OnboardingScreen
import com.alaje.gendright.ui.quicktest.QuickTestScreen
import com.alaje.gendright.ui.settings.SettingsScreen

@Composable
fun GendrightNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavigationActions.onboarding
    ) {
        composable(NavigationActions.onboarding) {
            OnboardingScreen()
        }

        composable(NavigationActions.settings) {
            SettingsScreen()
        }

        composable(NavigationActions.quickTest) {
            QuickTestScreen()
        }

    }
}