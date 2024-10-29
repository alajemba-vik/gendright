package com.alaje.gendright.di

import com.alaje.gendright.data.googleAIClient.AIClientAPIService

class AppContainer {
    val aiClientAPIService = AIClientAPIService()

    companion object {
        val instance = AppContainer()
    }
}