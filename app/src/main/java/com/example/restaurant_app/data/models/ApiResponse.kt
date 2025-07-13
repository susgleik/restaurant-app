package com.example.restaurant_app.data.models

import kotlinx.serialization.Serializable

// data/models/ApiResponse.kt
@Serializable
data class ApiError(
    val detail: String
)

@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val error: ApiError? = null,
    val success: Boolean = true
)