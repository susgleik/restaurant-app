// data/models/MenuItem.kt - Archivo completo con todos los modelos de MenuItem
package com.example.restaurant_app.data.models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

// Modelos principales para consulta
@SuppressLint("UnsafeOptInUsageError")
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

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MenuItemList(
    val items: List<MenuItem>,
    val total: Int
)

@SuppressLint("UnsafeOptInUsageError")
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

// Modelos para administración (CRUD)
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MenuItemCreate(
    val category_id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val image_url: String? = null,
    val available: Boolean = true
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MenuItemUpdate(
    val category_id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val image_url: String? = null,
    val available: Boolean? = null
)