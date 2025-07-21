// data/remote/OrderApiService.kt
package com.example.restaurant_app.data.remote

import com.example.restaurant_app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApiService {

    @GET("orders/")
    suspend fun getOrders(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<OrderList>

    @GET("orders/{order_id}")
    suspend fun getOrderById(@Path("order_id") orderId: String): Response<Order>

    @POST("orders/")
    suspend fun createOrder(@Body order: OrderCreate): Response<Order>

    @POST("orders/from-cart")
    suspend fun createOrderFromCart(@Query("notes") notes: String? = null): Response<Order>

    @DELETE("orders/{order_id}")
    suspend fun cancelOrder(@Path("order_id") orderId: String): Response<Unit>

    // ============== ENDPOINT AGREGADO PARA ADMIN ==============

    /**
     * Actualizar estado de un pedido (para administradores)
     */
    @PATCH("orders/{order_id}/status")
    suspend fun updateOrderStatus(
        @Path("order_id") orderId: String,
        @Body statusUpdate: OrderStatusUpdate
    ): Response<Order>
}