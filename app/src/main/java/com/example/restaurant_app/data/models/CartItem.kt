package com.example.restaurant_app.data.models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

// data/models/CartItem.kt
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CartItem(
    val id: String,
    val user_id: String,
    val menu_item_id: String,
    val menu_item_name: String,
    val menu_item_price: Double,
    val quantity: Int,
    val subtotal: Double,
    val created_at: String,
    val updated_at: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CartList(
    val items: List<CartItem>,
    val total_items: Int,
    val total_quantity: Int,
    val subtotal: String,
    val estimated_tax: String,
    val estimated_total: String,
    val is_empty: Boolean,
    val last_updated: String?
){
    // Propiedades calculadas para facilitar el uso
    val totalAmount: Double
        get() = estimated_total.toDoubleOrNull() ?: 0.0

    val subtotalAmount: Double
        get() = subtotal.toDoubleOrNull() ?: 0.0

    val taxAmount: Double
        get() = estimated_tax.toDoubleOrNull() ?: 0.0
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CartItemCreate(
    val menu_item_id: String,
    val quantity: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CartItemUpdate(
    val quantity: Int
)