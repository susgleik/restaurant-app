package com.example.restaurant_app.data.models

import kotlinx.serialization.Serializable

enum class UserRole {
    CLIENT,
    ADMIN_STAFF
}

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val role: UserRole,
    val created_at: String,
    val updated_at: String
)

@Serializable
data class UserCreate(
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.CLIENT
)

@Serializable
data class UserLogin(
    val email: String,
    val password: String
)