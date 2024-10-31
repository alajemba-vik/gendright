package com.alaje.gendright.data.models

sealed class DataResponse<T> {
    open val data: T? = null

    class Idle<T> : DataResponse<T>() {
        override fun equals(other: Any?): Boolean {
            return other.hashCode() == hashCode()
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    class Loading<T> : DataResponse<T>() {
        override fun equals(other: Any?): Boolean {
            return other.hashCode() == hashCode()
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    data class Success<T>(override val data: T?) : DataResponse<T>()
    data class APIError<T>(val message: String) : DataResponse<T>()
    data class NetworkError<T>(val message: String) : DataResponse<T>()
}