package com.alaje.gendright.utils

import android.util.Log
import com.alaje.gendright.data.googleAIClient.models.AIClientAPIResponse
import com.alaje.gendright.data.models.DataResponse
import com.alaje.gendright.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow

class BiasReader {
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing

    var textFieldsCache: MutableMap<String, AIClientAPIResponse> = mutableMapOf()

    var lastResponse: DataResponse<AIClientAPIResponse> = DataResponse.Success(null)

    suspend fun readText(text: String) {
        Log.d("GendRightService", "Processing text: $text")

        val cachedData = textFieldsCache[text]

        if (cachedData != null) {
            // Set the cached data
            lastResponse = DataResponse.Success(cachedData)
        } else {

            _isProcessing.value = true

            val response = AppContainer.instance?.aiClientAPIService?.processText(text)

            if (response != null) {
                lastResponse = response

                when (response) {
                    is DataResponse.Success -> {
                        Log.d("BiasReader", "Success")
                        // Cache the transformed text
                        response.data?.let { textFieldsCache[text] = it }
                    }

                    is DataResponse.NetworkError -> {
                        Log.d("BiasReader", "Network Error")
                    }

                    else -> {
                        Log.d("BiasReader", "API Error")
                    }
                }
            } else {
                Log.d("BiasReader", "No response")
            }

            _isProcessing.value = false

        }
    }

}