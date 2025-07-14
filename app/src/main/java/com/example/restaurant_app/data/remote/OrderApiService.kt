// data/remote/OrderApiService.kt
package com.example.restaurant_app.data.remote

import com.example.restaurant_app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApiService {
    @GET("orders/")
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<OrderList>

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: String): Response<Order>

    @POST("orders/")
    suspend fun createOrder(@Body order: OrderCreate): Response<Order>

    @PATCH("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Query("status") status: String
    ): Response<Order>

    @POST("orders/from-cart")
    suspend fun createOrderFromCart(@Body notes: Map<String, String?>): Response<Order>

    // Para admin - ver todos los pedidos
    @GET("orders/admin/all")
    suspend fun getAllOrdersAdmin(
        @Query("status") status: String? = null,
        @Query("user_id") userId: String? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<OrderList>
}