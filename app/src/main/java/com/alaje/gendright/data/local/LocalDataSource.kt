package com.alaje.gendright.data.local

import android.content.Context

class LocalDataSource(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("gendright", Context.MODE_PRIVATE)

    fun checkhasOnboardedUser(): Boolean {
        return sharedPreferences.getBoolean("isFirstTimeUser", true)
    }
    fun setUserHasOnboarded() {
        sharedPreferences.edit().putBoolean("isFirstTimeUser", false).apply()
    }

    fun checkHasAutoSelectMostRelevant(): Boolean {
        return sharedPreferences.getBoolean("autoSelectMostRelevant", false)
    }

    fun setAutoSelectMostRelevant(shouldEnable: Boolean) {
        sharedPreferences.edit().putBoolean("autoSelectMostRelevant", shouldEnable).apply()
    }

}