package com.nikkap.calendar.data.remote.interceptor

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: suspend () -> String?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            tokenProvider()
        }
        val original = chain.request()
        val request = original.newBuilder()
            .apply {
                token?.let { addHeader("Authorization", "Bearer $it") }
            }
            .build()
        return chain.proceed(request)
    }
}