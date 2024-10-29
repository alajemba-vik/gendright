package com.alaje.gendright.data.models

sealed class DataResponse<T> {
    data class Success<T>(val data: T) : DataResponse<T>()
    data class APIError<T>(val message: String) : DataResponse<T>()
    data class NetworkError<T>(val message: String) : DataResponse<T>()
}