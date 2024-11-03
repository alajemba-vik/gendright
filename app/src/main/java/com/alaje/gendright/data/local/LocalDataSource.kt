package com.alaje.gendright.data.local

import android.content.Context

class LocalDataSource(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences(appSharedPrefFileName, Context.MODE_PRIVATE)

    fun checkHasOnboardedUser(): Boolean {
        return sharedPreferences.getBoolean(hasOnboardedUser, false)
    }
    fun setUserHasOnboarded() {
        sharedPreferences.edit().putBoolean(hasOnboardedUser, true).apply()
    }

    fun checkUserHasSeenWalkthroughUIOnQuickTest(): Boolean {
        return sharedPreferences.getBoolean(userHasSeenWalkthroughUIOnQuickTest, false)
    }

    fun setUserHasSeenWalkthroughUIOnQuickTest() {
        sharedPreferences.edit().putBoolean(userHasSeenWalkthroughUIOnQuickTest, true).apply()
    }

    fun checkUserHasSeenWalkthroughUIOnSettings(): Boolean {
        return sharedPreferences.getBoolean(userHasSeenWalkthroughUIOnSettings, false)
    }

    fun setUserHasSeenWalkthroughUIOnSettings() {
        sharedPreferences.edit().putBoolean(userHasSeenWalkthroughUIOnSettings, true).apply()
    }

    fun checkHasAutoSelectMostRelevant(): Boolean {
        return sharedPreferences.getBoolean(autoSelectMostRelevantKey, false)
    }

    fun setAutoSelectMostRelevant(shouldEnable: Boolean) {
        sharedPreferences.edit().putBoolean(autoSelectMostRelevantKey, shouldEnable).apply()
    }

}

private const val hasOnboardedUser = "hasOnboardedUser"
private const val userHasSeenWalkthroughUIOnQuickTest = "userHasSeenWalkthroughUIOnQuickTest"
private const val userHasSeenWalkthroughUIOnSettings = "userHasSeenWalkthroughUIOnSettings"
private const val autoSelectMostRelevantKey = "autoSelectMostRelevant"
private const val appSharedPrefFileName = "gendright"