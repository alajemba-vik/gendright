package com.alaje.gendright.utils

import android.util.Log
import com.alaje.gendright.data.googleAIClient.models.AIClientAPIResponse
import com.alaje.gendright.data.models.DataResponse
import com.alaje.gendright.di.AppContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BiasReader {
    var textFieldsCache: MutableMap<String, AIClientAPIResponse> = mutableMapOf()

    private val _response: MutableStateFlow<DataResponse<AIClientAPIResponse>> =
        MutableStateFlow(DataResponse.Idle())
    val response: StateFlow<DataResponse<AIClientAPIResponse>> = _response

    suspend fun readText(text: String) {
        _response.value = DataResponse.Loading()

        delay(1000)

        Log.d("GendRightService", "Processing text: $text")

        val cachedData = textFieldsCache[text]

        if (cachedData != null) {
            // Set the cached data
            _response.value = DataResponse.Success(cachedData)
        } else {
            val response = AppContainer.instance?.aiClientAPIService?.processText(text)

            if (response != null) {
                _response.value = response

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
                _response.value = DataResponse.APIError("No response")

                Log.d("BiasReader", "No response")
            }
        }
    }
}

