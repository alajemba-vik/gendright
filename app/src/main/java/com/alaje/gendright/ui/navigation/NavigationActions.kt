package com.alaje.gendright.ui.navigation

import androidx.navigation.NavController

object NavigationActions {

    fun NavController.navigateToSettings() {
        navigate(SETTINGS)
    }

    fun NavController.navigateToQuickTest() {
        navigate(QUICKTEST)
    }

    const val ONBOARDING = "onboarding"
    const val SETTINGS = "settings"
    const val QUICKTEST = "quickText"
}
