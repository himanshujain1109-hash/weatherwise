package com.weatherwise.utils

/**
 * Sealed class representing a generic resource state with data and error messaging.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T, val isOffline: Boolean = false) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null, val isNetworkError: Boolean = false) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}
