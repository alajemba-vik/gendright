package com.alaje.gendright

import android.app.Application
import com.alaje.gendright.di.AppContainer

class GendrightApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initAppContainer(this)
    }
}