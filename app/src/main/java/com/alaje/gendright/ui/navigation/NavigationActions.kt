package com.alaje.gendright.ui.navigation

import androidx.navigation.NavController

object NavigationActions {

    fun NavController.navigateToSettings() {
        navigate(settings)
    }

    fun NavController.navigateToQuickTest() {
        navigate(quickTest)
    }

    const val onboarding = "onboarding"
    const val settings = "settings"
    const val quickTest = "quickText"
}