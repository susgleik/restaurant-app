package com.example.restaurant_app.data.models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OrderItem(
    val menu_item_id: String,
    val menu_item_name: String,
    val quantity: Int,
    val unit_price: String,
    val subtotal: String,
    val special_instructions: String? = null
) {
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
    val notes: String? = null,
    val created_at: String,
    val updated_at: String,
    // Campos opcionales que el backend actual no está enviando
    val username: String? = null,
    val user_email: String? = null
) {
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

    // Campos derivados para mostrar información del cliente
    val displayCustomerName: String
        get() = username ?: "Cliente #${user_id.takeLast(8)}"

    val displayCustomerEmail: String
        get() = user_email ?: "No disponible"

    // Validaciones útiles
    val hasSpecialInstructions: Boolean
        get() = items.any { !it.special_instructions.isNullOrBlank() }

    val itemCount: Int
        get() = items.sumOf { it.quantity }

    // Formateo de fechas mejorado
    val formattedCreatedAt: String
        get() = try {
            // Intentar parsear la fecha del backend y formatearla mejor
            val cleanDate = created_at.replace("T", " ").substringBefore(".")
            cleanDate
        } catch (e: Exception) {
            created_at
        }

    val formattedUpdatedAt: String
        get() = try {
            val cleanDate = updated_at.replace("T", " ").substringBefore(".")
            cleanDate
        } catch (e: Exception) {
            updated_at
        }
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