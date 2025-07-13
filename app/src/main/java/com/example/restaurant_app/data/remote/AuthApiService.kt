package com.example.restaurant_app.data.remote

import com.example.restaurant_app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService{
    @POST("auth/register")
    suspend fun register(@Body request:UserCreate): Response<TokenResponse>

    @POST("auth/login")
    suspend fun login(@Body request: UserLogin): Response<TokenResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}