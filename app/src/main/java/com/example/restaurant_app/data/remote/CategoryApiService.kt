package com.example.restaurant_app.data.remote

import com.example.restaurant_app.data.models.*
import retrofit2.Response
import retrofit2.http.*

// API Service para Categories
interface CategoryApiService {
    @GET("categories/")
    suspend fun getCategories(
        @Query("active_only") activeOnly: Boolean? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<CategoryList>

    @GET("categories/{id}")
    suspend fun getCategory(@Path("id") id: String): Response<Category>

    @POST("categories/")
    suspend fun createCategory(@Body category: CategoryCreate): Response<Category>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body category: CategoryUpdate
    ): Response<Category>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<Unit>
}