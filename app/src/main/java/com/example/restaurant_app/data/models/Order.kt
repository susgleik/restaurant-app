package com.example.restaurant_app.data.models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

// data/models/Order.kt
enum class OrderStatus {
    PENDING,
    IN_PREPARATION,
    READY,
    DELIVERED,
    CANCELLED
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OrderItem(
    val menu_item_id: String,
    val menu_item_name: String,
    val quantity: Int,
    val unit_price: String,
    val subtotal: String,
    val special_instructions: String?
){
    val unitPriceAmount: Double
        get() = unit_price.toDoubleOrNull() ?: 0.0

    val subtotalAmount: Double
        get() = subtotal.toDoubleOrNull() ?: 0.0
}


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Order(
    val id: String,
    val user_id: String,
    val items: List<OrderItem>,
    val total: String,
    val status: OrderStatus,
    val notes: String?,
    val created_at: String,
    val updated_at: String,
    val username: String? = null,
    val user_email: String? = null
){
    val totalAmount: Double
        get() = total.toDoubleOrNull() ?: 0.0

    val formattedStatus: String
        get() = when (status) {
            OrderStatus.PENDING -> "Pendiente"
            OrderStatus.IN_PREPARATION -> "En preparación"
            OrderStatus.READY -> "Listo"
            OrderStatus.DELIVERED -> "Entregado"
            OrderStatus.CANCELLED -> "Cancelado"
        }

    val canBeCancelled: Boolean
        get() = status == OrderStatus.PENDING || status == OrderStatus.IN_PREPARATION
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OrderCreate(
    val items: List<OrderItemCreate>,
    val notes: String? = null
)
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OrderItemCreate(
    val menu_item_id: String,
    val quantity: Int,
    val special_instructions: String? = null
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OrderList(
    val orders: List<Order>,
    val total: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OrderFromCart(
    val notes: String? = null
)