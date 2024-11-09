package com.alaje.gendright

import android.app.Application
import com.alaje.gendright.di.AppContainer
import com.google.android.material.color.DynamicColors

class GendrightApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initAppContainer(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}