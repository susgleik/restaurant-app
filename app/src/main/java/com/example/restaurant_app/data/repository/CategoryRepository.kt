// data/repository/CategoryRepository.kt - Versión final corregida
package com.example.restaurant_app.data.repository

import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.remote.MenuApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val menuApiService: MenuApiService // Usar MenuApiService ya que tiene los endpoints de categories
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
                val errorMessage = when (response.code()) {
                    401 -> "No tienes permisos para ver las categorías."
                    500 -> "Error del servidor. Intenta más tarde."
                    else -> "Error al cargar categorías: ${response.message()}"
                }
                emit(MenuResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun getCategory(id: String): Flow<MenuResult<Category>> = flow {
        try {
            emit(MenuResult.Loading("Cargando categoría..."))

            val response = menuApiService.getCategory(id)

            if (response.isSuccessful) {
                response.body()?.let { category ->
                    emit(MenuResult.Success(category))
                } ?: emit(MenuResult.Error("Categoría no encontrada"))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "La categoría no existe."
                    401 -> "No tienes permisos para ver esta categoría."
                    else -> "Error al cargar categoría: ${response.message()}"
                }
                emit(MenuResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun createCategory(category: CategoryCreate): Flow<MenuResult<Category>> = flow {
        try {
            emit(MenuResult.Loading("Creando categoría..."))

            val response = menuApiService.createCategory(category)

            if (response.isSuccessful) {
                response.body()?.let { newCategory ->
                    emit(MenuResult.Success(newCategory))
                } ?: emit(MenuResult.Error("No se pudo crear la categoría"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos inválidos. Verifica la información ingresada."
                    401 -> "No tienes permisos para crear categorías."
                    409 -> "Ya existe una categoría con ese nombre."
                    422 -> "Los datos enviados no son válidos."
                    else -> "Error al crear categoría: ${response.message()}"
                }
                emit(MenuResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun updateCategory(id: String, category: CategoryUpdate): Flow<MenuResult<Category>> = flow {
        try {
            emit(MenuResult.Loading("Actualizando categoría..."))

            val response = menuApiService.updateCategory(id, category)

            if (response.isSuccessful) {
                response.body()?.let { updatedCategory ->
                    emit(MenuResult.Success(updatedCategory))
                } ?: emit(MenuResult.Error("No se pudo actualizar la categoría"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos inválidos. Verifica la información ingresada."
                    401 -> "No tienes permisos para modificar categorías."
                    404 -> "La categoría no existe."
                    409 -> "Ya existe una categoría con ese nombre."
                    422 -> "Los datos enviados no son válidos."
                    else -> "Error al actualizar categoría: ${response.message()}"
                }
                emit(MenuResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun deleteCategory(id: String): Flow<MenuResult<Unit>> = flow {
        try {
            emit(MenuResult.Loading("Eliminando categoría..."))

            val response = menuApiService.deleteCategory(id)

            if (response.isSuccessful) {
                emit(MenuResult.Success(Unit))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "No se puede eliminar esta categoría porque tiene productos asociados."
                    401 -> "No tienes permisos para eliminar categorías."
                    404 -> "La categoría no existe."
                    422 -> "La categoría está en uso y no puede ser eliminada."
                    else -> "Error al eliminar categoría: ${response.message()}"
                }
                emit(MenuResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error("Error de conexión: ${e.message}"))
        }
    }
}