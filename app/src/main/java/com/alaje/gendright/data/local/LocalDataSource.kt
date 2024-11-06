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

    fun checkUserSeenWalkthroughOnQuickTest(): Boolean {
        return sharedPreferences.getBoolean(userHasSeenWalkthroughUIOnQuickTest, false)
    }

    fun setUserSeenWalkthroughOnQuickTest() {
        sharedPreferences.edit().putBoolean(userHasSeenWalkthroughUIOnQuickTest, true).apply()
    }

}

private const val hasOnboardedUser = "hasOnboardedUser"
private const val userHasSeenWalkthroughUIOnQuickTest = "userHasSeenWalkthroughUIOnQuickTest"
private const val appSharedPrefFileName = "gendright"