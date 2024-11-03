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
            get() {
                if (field == null) {
                    throw IllegalStateException(
                        "AppContainer has not been initialized. " +
                                "Please call AppContainer.initAppContainer(context) in your Application class."
                    )
                }
                return field
            }

        fun initAppContainer(context: Context): AppContainer {
            return AppContainer(context).also {
                instance = it
            }
        }
    }
}