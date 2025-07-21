// data/models/OrderStatus.kt - Enum para estados de pedidos
package com.example.restaurant_app.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus {
    PENDING,
    IN_PREPARATION,
    READY,
    DELIVERED,
    CANCELLED
}