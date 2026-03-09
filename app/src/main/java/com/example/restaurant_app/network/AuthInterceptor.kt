package com.example.restaurant_app.network

import com.example.restaurant_app.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Si es una request de login o register, no agregar token
        val url = originalRequest.url.toString()
        if (url.contains("/auth/login") || url.contains("/auth/register")) {
            return chain.proceed(originalRequest)
        }

        // Obtener el token de manera síncrona
        val token = runBlocking {
            try {
                tokenManager.getAccessToken().first()
            } catch (e: Exception) {
                null
            }
        }

        // Si no hay token, proceder sin authorization header
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        // Agregar el token al header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(authenticatedRequest)

        // Si el servidor responde 401, el token expiró — limpiar y notificar
        if (response.code == 401) {
            runBlocking { tokenManager.clearTokens() }
            sessionManager.notifySessionExpired()
        }

        return response
    }
}