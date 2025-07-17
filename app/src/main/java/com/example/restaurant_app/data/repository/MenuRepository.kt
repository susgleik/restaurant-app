package com.example.restaurant_app.data.repository

import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.remote.MenuApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor(
    private val menuApiService: MenuApiService
) {

    fun getCategories(
        activeOnly: Boolean? = null,
        skip: Int = 0,
        limit: Int = 50
    ): Flow<MenuResult<CategoryList>> = flow {
        try {
            emit(MenuResult.Loading("Cargando categorías..."))

            val response = menuApiService.getCategories(activeOnly, skip, limit)

            if (response.isSuccessful) {
                response.body()?.let { categoryList ->
                    emit(MenuResult.Success(categoryList))
                } ?: emit(MenuResult.Error("Respuesta vacía del servidor"))
            } else {
                emit(MenuResult.Error("Error al obtener categorías: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun getMenuItems(
        categoryId: String? = null,
        available: Boolean? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        search: String? = null,
        skip: Int = 0,
        limit: Int = 50
    ): Flow<MenuResult<MenuItemList>> = flow {
        try {
            emit(MenuResult.Loading("Cargando menú..."))

            val response = menuApiService.getMenuItems(
                categoryId, available, minPrice, maxPrice, search, skip, limit
            )

            if (response.isSuccessful) {
                response.body()?.let { menuItemList ->
                    emit(MenuResult.Success(menuItemList))
                } ?: emit(MenuResult.Error("Respuesta vacía del servidor"))
            } else {
                emit(MenuResult.Error("Error al obtener items del menú: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun getMenuItemsByCategory(
        categoryId: String,
        availableOnly: Boolean = true,
        skip: Int = 0,
        limit: Int = 50
    ): Flow<MenuResult<MenuItemList>> = flow {
        try {
            emit(MenuResult.Loading("Cargando productos..."))

            val response = menuApiService.getMenuItemsByCategory(categoryId, availableOnly, skip, limit)

            if (response.isSuccessful) {
                response.body()?.let { menuItemList ->
                    emit(MenuResult.Success(menuItemList))
                } ?: emit(MenuResult.Error("Respuesta vacía del servidor"))
            } else {
                emit(MenuResult.Error("Error al obtener items de la categoría: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun getMenuItem(id: String): Flow<MenuResult<MenuItemWithCategory>> = flow {
        try {
            emit(MenuResult.Loading("Cargando producto..."))

            val response = menuApiService.getMenuItem(id)

            if (response.isSuccessful) {
                response.body()?.let { menuItem ->
                    emit(MenuResult.Success(menuItem))
                } ?: emit(MenuResult.Error("Item no encontrado"))
            } else {
                emit(MenuResult.Error("Error al obtener item: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun searchMenuItems(
        query: String,
        categoryId: String? = null
    ): Flow<MenuResult<MenuItemList>> = flow {
        try {
            emit(MenuResult.Loading("Buscando..."))

            val response = menuApiService.getMenuItems(
                categoryId = categoryId,
                search = query,
                available = true
            )

            if (response.isSuccessful) {
                response.body()?.let { menuItemList ->
                    emit(MenuResult.Success(menuItemList))
                } ?: emit(MenuResult.Error("No se encontraron resultados"))
            } else {
                emit(MenuResult.Error("Error en la búsqueda: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }
}