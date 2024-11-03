package com.alaje.gendright.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alaje.gendright.di.AppContainer
import com.alaje.gendright.ui.navigation.NavigationActions.navigateToQuickTest
import com.alaje.gendright.ui.navigation.NavigationActions.navigateToSettings
import com.alaje.gendright.ui.onboarding.OnboardingScreen
import com.alaje.gendright.ui.quicktest.QuickTestScreen
import com.alaje.gendright.ui.settings.SettingsScreen

@Composable
fun GendrightNavHost() {
    val navController = rememberNavController()
    val startDestination =
        if (AppContainer.instance?.localDataSource?.checkHasOnboardedUser() == true) {
            NavigationActions.settings
        } else {
            NavigationActions.onboarding
        }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            ) + slideIntoContainer(
                animationSpec = tween(300, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            ) + slideOutOfContainer(
                animationSpec = tween(300, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        },
        popExitTransition = {
            fadeOut(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            ) + slideOutOfContainer(
                animationSpec = tween(300, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            ) + slideIntoContainer(
                animationSpec = tween(300, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        }
    ) {
        composable(NavigationActions.onboarding) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigateToSettings()
                    AppContainer.instance?.localDataSource?.setUserHasOnboarded()
                }
            )
        }

        composable(NavigationActions.settings) {
            SettingsScreen(
                onQuickTest = {
                    navController.navigateToQuickTest()
                }
            )
        }

        composable(NavigationActions.quickTest) {
            QuickTestScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}