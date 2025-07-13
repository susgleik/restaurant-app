package com.example.restaurant_app.data.models

import kotlinx.serialization.Serializable

// data/models/Category.kt
@Serializable
data class Category(
    val id: String,
    val name: String,
    val description: String?,
    val active: Boolean,
    val created_at: String
)

@Serializable
data class CategoryCreate(
    val name: String,
    val description: String? = null,
    val active: Boolean = true
)

@Serializable
data class CategoryUpdate(
    val name: String? = null,
    val description: String? = null,
    val active: Boolean? = null
)

@Serializable
data class CategoryList(
    val categories: List<Category>,
    val total: Int
)