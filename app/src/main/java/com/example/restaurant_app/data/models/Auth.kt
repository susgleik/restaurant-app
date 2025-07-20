package com.example.restaurant_app.data.models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class LoginResponse(
    val user: User,
    val access_token: String,
    val token_type: String = "bearer"
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    val created_at: String,
    val updated_at: String
)

// Si necesitas estos modelos adicionales para otras funciones
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class RefreshTokenRequest(
    val refresh_token: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class TokenResponse(
    val access_token: String,
    val token_type: String = "bearer",
    val refresh_token: String? = null
)