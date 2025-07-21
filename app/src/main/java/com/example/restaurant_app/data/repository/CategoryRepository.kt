// data/repository/CategoryRepository.kt
package com.example.restaurant_app.data.repository

import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.remote.CategoryApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.example.restaurant_app.data.repository.MenuResult

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryApiService: CategoryApiService
) {

    fun getCategories(
        activeOnly: Boolean? = null,
        skip: Int = 0,
        limit: Int = 50
    ): Flow<MenuResult<CategoryList>> = flow {
        emit(MenuResult.Loading("Cargando categorías..."))
        try {
            val response = categoryApiService.getCategories(activeOnly, skip, limit)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(MenuResult.Success(it))
                } ?: emit(MenuResult.Error("Respuesta vacía del servidor"))
            } else {
                val msg = when (response.code()) {
                    404 -> "No se encontraron categorías"
                    500 -> "Error interno del servidor"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                emit(MenuResult.Error(msg))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error(e.message ?: "Error desconocido"))
        }
    }

    suspend fun getCategory(id: String): Result<Category> = withContext(Dispatchers.IO) {
        try {
            val response = categoryApiService.getCategory(id)

            if (response.isSuccessful) {
                response.body()?.let { category ->
                    Result.success(category)
                } ?: Result.failure(Exception("Categoría no encontrada"))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Categoría no encontrada"
                    400 -> "ID de categoría inválido"
                    500 -> "Error interno del servidor"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createCategory(categoryCreate: CategoryCreate): Flow<MenuResult<Category>> = flow {
        emit(MenuResult.Loading("creando categorias..."))
        try {
            val response = categoryApiService.createCategory(categoryCreate)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(MenuResult.Success(it))
                } ?: emit(MenuResult.Error("Error al crear la categoría"))
            } else {
                val msg = when (response.code()) {
                    400 -> "Datos inválidos"
                    409 -> "Ya existe una categoría con ese nombre"
                    401 -> "No tienes permisos"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                emit(MenuResult.Error(msg))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error(e.message ?: "Error desconocido"))
        }
    }

    fun updateCategory(id: String, categoryUpdate: CategoryUpdate): Flow<MenuResult<Category>> = flow {
        emit(MenuResult.Loading("actualizando categorias..."))
        try {
            val response = categoryApiService.updateCategory(id, categoryUpdate)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(MenuResult.Success(it))
                } ?: emit(MenuResult.Error("Error al actualizar la categoría"))
            } else {
                val msg = when (response.code()) {
                    400 -> "Datos inválidos"
                    404 -> "No encontrada"
                    409 -> "Nombre duplicado"
                    401 -> "Sin permisos"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                emit(MenuResult.Error(msg))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error(e.message ?: "Error desconocido"))
        }
    }

    fun deleteCategory(id: String): Flow<MenuResult<Unit>> = flow {
        emit(MenuResult.Loading("eliminando categorias..."))
        try {
            val response = categoryApiService.deleteCategory(id)
            if (response.isSuccessful) {
                emit(MenuResult.Success(Unit))
            } else {
                val msg = when (response.code()) {
                    400 -> "No se puede eliminar porque tiene productos"
                    404 -> "No encontrada"
                    401 -> "Sin permisos"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                emit(MenuResult.Error(msg))
            }
        } catch (e: Exception) {
            emit(MenuResult.Error(e.message ?: "Error desconocido"))
        }
    }
}