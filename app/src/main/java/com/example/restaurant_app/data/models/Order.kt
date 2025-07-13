package com.example.restaurant_app.data.models

import kotlinx.serialization.Serializable

// data/models/Order.kt
enum class OrderStatus {
    PENDING,
    IN_PREPARATION,
    READY,
    DELIVERED,
    CANCELLED
}

@Serializable
data class OrderItem(
    val menu_item_id: String,
    val menu_item_name: String,
    val quantity: Int,
    val unit_price: Double,
    val subtotal: Double,
    val special_instructions: String?
)

@Serializable
data class Order(
    val id: String,
    val user_id: String,
    val items: List<OrderItem>,
    val total: Double,
    val status: OrderStatus,
    val notes: String?,
    val created_at: String,
    val updated_at: String
)

@Serializable
data class OrderCreate(
    val items: List<OrderItemCreate>,
    val notes: String? = null
)

@Serializable
data class OrderItemCreate(
    val menu_item_id: String,
    val quantity: Int,
    val special_instructions: String? = null
)

@Serializable
data class OrderList(
    val orders: List<Order>,
    val total: Int
)