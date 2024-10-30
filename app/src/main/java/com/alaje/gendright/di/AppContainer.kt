package com.alaje.gendright.di

import android.content.Context
import com.alaje.gendright.data.googleAIClient.AIClientAPIService
import com.alaje.gendright.data.local.LocalDataSource

class AppContainer(
    context: Context
) {
    val aiClientAPIService = AIClientAPIService()
    val localDataSource = LocalDataSource(context)

    companion object {
        var instance: AppContainer? = null

        fun initAppContainer(context: Context) {
            instance = AppContainer(context)
        }
    }
}