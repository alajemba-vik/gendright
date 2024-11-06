package com.alaje.gendright

import android.app.Application
import com.alaje.gendright.di.AppContainer
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp

class GendrightApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
        AppContainer.initAppContainer(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}