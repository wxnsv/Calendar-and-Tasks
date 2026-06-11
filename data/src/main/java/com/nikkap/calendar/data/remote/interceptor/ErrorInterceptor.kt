package com.nikkap.calendar.data.remote.interceptor

import com.nikkap.calendar.core.exceptions.NetworkException
import okhttp3.Interceptor
import okhttp3.Response
import java.net.ConnectException
import java.net.UnknownHostException

class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            if (e is UnknownHostException || e is ConnectException) {

                throw NetworkException.NoInternetException()
            }
            throw e
        }

        when (response.code) {
            401 -> throw NetworkException.UnauthorizedException()
            in 400..499 -> {
                throw NetworkException.ServerException(response.code, "Client Error")
            }

            in 500..599 -> throw NetworkException.ServerException(
                response.code,
                "Server Internal Error"
            )
        }

        return response
    }
}