package com.example.restaurant_app.data.remote

import com.example.restaurant_app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface MenuApiService {
    //Categories
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

    // Menu Items
    @GET("menu-items/")
    suspend fun getMenuItems(
        @Query("category_id") categoryId: String? = null,
        @Query("available") available: Boolean? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
        @Query("search") search: String? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<MenuItemList>

    @GET("menu-items/{id}")
    suspend fun getMenuItem(@Path("id") id: String): Response<MenuItemWithCategory>

    @POST("menu-items/")
    suspend fun createMenuItem(@Body menuItem: MenuItemCreate): Response<MenuItem>

    @PUT("menu-items/{id}")
    suspend fun updateMenuItem(
        @Path("id") id: String,
        @Body menuItem: MenuItemUpdate
    ): Response<MenuItem>

    @DELETE("menu-items/{id}")
    suspend fun deleteMenuItem(@Path("id") id: String): Response<Unit>

    @GET("menu-items/category/{categoryId}")
    suspend fun getMenuItemsByCategory(
        @Path("categoryId") categoryId: String,
        @Query("available_only") availableOnly: Boolean = true,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<MenuItemList>

    @PATCH("menu-items/{id}/availability")
    suspend fun toggleItemAvailability(
        @Path("id") id: String,
        @Query("available") available: Boolean
    ): Response<MenuItem>

}