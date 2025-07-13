package com.example.restaurant_app.data.models

import kotlinx.serialization.Serializable

// data/models/Auth.kt
@Serializable
data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val user: User
)

@Serializable
data class RefreshTokenRequest(
    val refresh_token: String
)