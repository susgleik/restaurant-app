package com.example.restaurant_app.data.remote

import com.example.restaurant_app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<User>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}