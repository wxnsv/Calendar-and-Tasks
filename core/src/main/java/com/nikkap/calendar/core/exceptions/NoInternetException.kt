package com.nikkap.calendar.core.exceptions

import kotlinx.serialization.Serializable
import java.io.IOException

sealed class NetworkException(message: String) : IOException(message) {
    @Serializable
    class NoInternetException : NetworkException("No internet connection available")

    @Serializable
    class UnauthorizedException : NetworkException("Google authorization expired")

    @Serializable
    class ServerException(val code: Int, message: String) :
        NetworkException("Server error $code: $message")
}