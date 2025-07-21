// data/models/Category.kt - Solo modelos de categorías
package com.example.restaurant_app.data.models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Category(
    val id: String,
    val name: String,
    val description: String,
    val active: Boolean,
    val created_at: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CategoryList(
    val categories: List<Category>,
    val total: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CategoryCreate(
    val name: String,
    val description: String,
    val active: Boolean = true
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CategoryUpdate(
    val name: String? = null,
    val description: String? = null,
    val active: Boolean? = null
)