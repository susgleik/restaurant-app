package com.example.restaurant_app.data.models

import kotlinx.serialization.Serializable

// data/models/CartItem.kt
@Serializable
data class CartItem(
    val id: String,
    val user_id: String,
    val menu_item_id: String,
    val menu_item_name: String,
    val menu_item_price: Double,
    val quantity: Int,
    val created_at: String,
    val updated_at: String
)

@Serializable
data class CartItemCreate(
    val menu_item_id: String,
    val quantity: Int = 1
)

@Serializable
data class CartItemUpdate(
    val quantity: Int
)

@Serializable
data class CartList(
    val items: List<CartItem>,
    val total: Int,
    val total_amount: Double
)
