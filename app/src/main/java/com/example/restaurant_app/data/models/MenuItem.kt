package com.example.restaurant_app.data.models

import kotlinx.serialization.Serializable


// data/models/MenuItem.kt
@Serializable
data class MenuItem(
    val id: String,
    val category_id: String,
    val name: String,
    val description: String?,
    val price: Double,
    val image_url: String?,
    val available: Boolean,
    val created_at: String,
    val updated_at: String
)

@Serializable
data class MenuItemWithCategory(
    val id: String,
    val category_id: String,
    val name: String,
    val description: String?,
    val price: Double,
    val image_url: String?,
    val available: Boolean,
    val created_at: String,
    val updated_at: String,
    val category_name: String?,
    val category_active: Boolean?
)

@Serializable
data class MenuItemCreate(
    val category_id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val image_url: String? = null,
    val available: Boolean = true
)

@Serializable
data class MenuItemUpdate(
    val category_id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val image_url: String? = null,
    val available: Boolean? = null
)

@Serializable
data class MenuItemList(
    val items: List<MenuItem>,
    val total: Int
)