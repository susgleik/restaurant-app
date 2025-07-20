// data/remote/CartApiService.kt
package com.example.restaurant_app.data.remote

import com.example.restaurant_app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface CartApiService {

    @GET("cart/")
    suspend fun getCart(
        @Query("include_unavailable") includeUnavailable: Boolean = false
    ): Response<CartList>

    @POST("cart/items")
    suspend fun addToCart(@Body item: CartItemCreate): Response<CartItem>

    @PUT("cart/items/{id}")
    suspend fun updateCartItem(
        @Path("id") id: String,
        @Body item: CartItemUpdate
    ): Response<CartItem>

    @DELETE("cart/items/{id}")
    suspend fun removeFromCart(@Path("id") id: String): Response<Unit>

    @DELETE("cart/")
    suspend fun clearCart(): Response<Unit>
}