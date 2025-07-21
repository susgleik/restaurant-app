// data/repository/MenuRepository.kt - Versión final extendida
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

    // Métodos existentes (mantener tal como están)
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

    // NUEVOS MÉTODOS PARA ADMINISTRACIÓN
    fun createMenuItem(menuItem: MenuItemCreate): Flow<MenuResult<MenuItem>> = flow {
        try {
            emit(MenuResult.Loading("Creando producto..."))

            val response = menuApiService.createMenuItem(menuItem)

            if (response.isSuccessful) {
                response.body()?.let { newMenuItem ->
                    emit(MenuResult.Success(newMenuItem))
                } ?: emit(MenuResult.Error("No se pudo crear el producto"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos inválidos. Verifica la información ingresada."
                    401 -> "No tienes permisos para crear productos."
                    404 -> "La categoría seleccionada no existe."
                    409 -> "Ya existe un producto con ese nombre en esta categoría."
                    422 -> "Los datos enviados no son válidos."
                    else -> "Error al crear producto: ${response.message()}"
                }
                emit(MenuResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun updateMenuItem(id: String, menuItem: MenuItemUpdate): Flow<MenuResult<MenuItem>> = flow {
        try {
            emit(MenuResult.Loading("Actualizando producto..."))

            val response = menuApiService.updateMenuItem(id, menuItem)

            if (response.isSuccessful) {
                response.body()?.let { updatedMenuItem ->
                    emit(MenuResult.Success(updatedMenuItem))
                } ?: emit(MenuResult.Error("No se pudo actualizar el producto"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos inválidos. Verifica la información ingresada."
                    401 -> "No tienes permisos para modificar productos."
                    404 -> "El producto no existe."
                    409 -> "Ya existe un producto con ese nombre en esta categoría."
                    422 -> "Los datos enviados no son válidos."
                    else -> "Error al actualizar producto: ${response.message()}"
                }
                emit(MenuResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun deleteMenuItem(id: String): Flow<MenuResult<Unit>> = flow {
        try {
            emit(MenuResult.Loading("Eliminando producto..."))

            val response = menuApiService.deleteMenuItem(id)

            if (response.isSuccessful) {
                emit(MenuResult.Success(Unit))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "No se puede eliminar este producto porque está en carritos o pedidos activos."
                    401 -> "No tienes permisos para eliminar productos."
                    404 -> "El producto no existe."
                    422 -> "El producto está en uso y no puede ser eliminado."
                    else -> "Error al eliminar producto: ${response.message()}"
                }
                emit(MenuResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun toggleItemAvailability(id: String, available: Boolean): Flow<MenuResult<MenuItem>> = flow {
        try {
            emit(MenuResult.Loading("Actualizando disponibilidad..."))

            val response = menuApiService.toggleItemAvailability(id, available)

            if (response.isSuccessful) {
                response.body()?.let { updatedMenuItem ->
                    emit(MenuResult.Success(updatedMenuItem))
                } ?: emit(MenuResult.Error("No se pudo actualizar la disponibilidad"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "No tienes permisos para cambiar la disponibilidad."
                    404 -> "El producto no existe."
                    422 -> "No se puede cambiar la disponibilidad de este producto."
                    else -> "Error al cambiar disponibilidad: ${response.message()}"
                }
                emit(MenuResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }
}