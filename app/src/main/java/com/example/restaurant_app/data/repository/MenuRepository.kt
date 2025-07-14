// data/repository/MenuRepository.kt
package com.example.restaurant_app.data.repository

import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.remote.MenuApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor(
    private val menuApiService: MenuApiService
) {

    // =================== CATEGORÍAS ===================

    /**
     * Obtener todas las categorías
     */
    suspend fun getCategories(
        activeOnly: Boolean? = null,
        skip: Int = 0,
        limit: Int = 50
    ): Result<CategoryList> {
        return try {
            val response = menuApiService.getCategories(activeOnly, skip, limit)
            if (response.isSuccessful) {
                response.body()?.let { categoryList ->
                    Result.success(categoryList)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "No se encontraron categorías"
                    400 -> "Parámetros inválidos"
                    else -> "Error al cargar categorías: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtener solo categorías activas
     */
    suspend fun getActiveCategories(): Result<CategoryList> {
        return getCategories(activeOnly = true)
    }

    /**
     * Obtener categoría específica por ID
     */
    suspend fun getCategory(id: String): Result<Category> {
        return try {
            val response = menuApiService.getCategory(id)
            if (response.isSuccessful) {
                response.body()?.let { category ->
                    Result.success(category)
                } ?: Result.failure(Exception("Categoría no encontrada"))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Categoría no encontrada"
                    400 -> "ID de categoría inválido"
                    else -> "Error al cargar categoría: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Crear nueva categoría (solo ADMIN)
     */
    suspend fun createCategory(category: CategoryCreate): Result<Category> {
        return try {
            val response = menuApiService.createCategory(category)
            if (response.isSuccessful) {
                response.body()?.let { createdCategory ->
                    Result.success(createdCategory)
                } ?: Result.failure(Exception("Error al crear categoría"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos de categoría inválidos"
                    401 -> "Sesión expirada"
                    403 -> "Sin permisos de administrador"
                    409 -> "Ya existe una categoría con ese nombre"
                    422 -> "Datos mal formateados"
                    else -> "Error al crear categoría: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Actualizar categoría (solo ADMIN)
     */
    suspend fun updateCategory(id: String, category: CategoryUpdate): Result<Category> {
        return try {
            val response = menuApiService.updateCategory(id, category)
            if (response.isSuccessful) {
                response.body()?.let { updatedCategory ->
                    Result.success(updatedCategory)
                } ?: Result.failure(Exception("Error al actualizar categoría"))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Categoría no encontrada"
                    400 -> "Datos inválidos"
                    401 -> "Sesión expirada"
                    403 -> "Sin permisos de administrador"
                    409 -> "Ya existe una categoría con ese nombre"
                    422 -> "Datos mal formateados"
                    else -> "Error al actualizar categoría: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Eliminar categoría (solo ADMIN)
     */
    suspend fun deleteCategory(id: String): Result<Unit> {
        return try {
            val response = menuApiService.deleteCategory(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Categoría no encontrada"
                    400 -> "No se puede eliminar: tiene items asociados"
                    401 -> "Sesión expirada"
                    403 -> "Sin permisos de administrador"
                    else -> "Error al eliminar categoría: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Activar/Desactivar categoría (solo ADMIN)
     */
    suspend fun toggleCategoryStatus(id: String, active: Boolean): Result<Category> {
        return updateCategory(id, CategoryUpdate(active = active))
    }

    // =================== ITEMS DEL MENÚ ===================

    /**
     * Obtener items del menú con filtros
     */
    suspend fun getMenuItems(
        categoryId: String? = null,
        available: Boolean? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        search: String? = null,
        skip: Int = 0,
        limit: Int = 50
    ): Result<MenuItemList> {
        return try {
            val response = menuApiService.getMenuItems(
                categoryId, available, minPrice, maxPrice, search, skip, limit
            )
            if (response.isSuccessful) {
                response.body()?.let { menuItemList ->
                    Result.success(menuItemList)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Parámetros de búsqueda inválidos"
                    404 -> "No se encontraron items"
                    else -> "Error al cargar items del menú: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtener solo items disponibles
     */
    suspend fun getAvailableMenuItems(
        categoryId: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        search: String? = null
    ): Result<MenuItemList> {
        return getMenuItems(
            categoryId = categoryId,
            available = true,
            minPrice = minPrice,
            maxPrice = maxPrice,
            search = search
        )
    }

    /**
     * Buscar items del menú
     */
    suspend fun searchMenuItems(query: String): Result<MenuItemList> {
        return getMenuItems(
            search = query,
            available = true
        )
    }

    /**
     * Obtener item específico por ID
     */
    suspend fun getMenuItem(id: String): Result<MenuItemWithCategory> {
        return try {
            val response = menuApiService.getMenuItem(id)
            if (response.isSuccessful) {
                response.body()?.let { menuItem ->
                    Result.success(menuItem)
                } ?: Result.failure(Exception("Item no encontrado"))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Item del menú no encontrado"
                    400 -> "ID de item inválido"
                    else -> "Error al cargar item: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtener items por categoría
     */
    suspend fun getMenuItemsByCategory(
        categoryId: String,
        availableOnly: Boolean = true,
        skip: Int = 0,
        limit: Int = 50
    ): Result<MenuItemList> {
        return try {
            val response = menuApiService.getMenuItemsByCategory(
                categoryId, availableOnly, skip, limit
            )
            if (response.isSuccessful) {
                response.body()?.let { menuItemList ->
                    Result.success(menuItemList)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Categoría no encontrada"
                    400 -> "ID de categoría inválido"
                    else -> "Error al cargar items de la categoría: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Crear nuevo item del menú (solo ADMIN)
     */
    suspend fun createMenuItem(menuItem: MenuItemCreate): Result<MenuItem> {
        return try {
            val response = menuApiService.createMenuItem(menuItem)
            if (response.isSuccessful) {
                response.body()?.let { createdMenuItem ->
                    Result.success(createdMenuItem)
                } ?: Result.failure(Exception("Error al crear item"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos del item inválidos"
                    401 -> "Sesión expirada"
                    403 -> "Sin permisos de administrador"
                    404 -> "Categoría no encontrada"
                    409 -> "Ya existe un item con ese nombre en la categoría"
                    422 -> "Datos mal formateados"
                    else -> "Error al crear item: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Actualizar item del menú (solo ADMIN)
     */
    suspend fun updateMenuItem(id: String, menuItem: MenuItemUpdate): Result<MenuItem> {
        return try {
            val response = menuApiService.updateMenuItem(id, menuItem)
            if (response.isSuccessful) {
                response.body()?.let { updatedMenuItem ->
                    Result.success(updatedMenuItem)
                } ?: Result.failure(Exception("Error al actualizar item"))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Item no encontrado"
                    400 -> "Datos inválidos"
                    401 -> "Sesión expirada"
                    403 -> "Sin permisos de administrador"
                    409 -> "Ya existe un item con ese nombre en la categoría"
                    422 -> "Datos mal formateados"
                    else -> "Error al actualizar item: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Eliminar item del menú (solo ADMIN)
     */
    suspend fun deleteMenuItem(id: String): Result<Unit> {
        return try {
            val response = menuApiService.deleteMenuItem(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Item no encontrado"
                    400 -> "No se puede eliminar: está en carritos o pedidos activos"
                    401 -> "Sesión expirada"
                    403 -> "Sin permisos de administrador"
                    else -> "Error al eliminar item: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Cambiar disponibilidad de un item (solo ADMIN)
     */
    suspend fun toggleItemAvailability(id: String, available: Boolean): Result<MenuItem> {
        return try {
            val response = menuApiService.toggleItemAvailability(id, available)
            if (response.isSuccessful) {
                response.body()?.let { menuItem ->
                    Result.success(menuItem)
                } ?: Result.failure(Exception("Error al cambiar disponibilidad"))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Item no encontrado"
                    401 -> "Sesión expirada"
                    403 -> "Sin permisos de administrador"
                    else -> "Error al cambiar disponibilidad: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Activar item del menú (solo ADMIN)
     */
    suspend fun activateMenuItem(id: String): Result<MenuItem> {
        return toggleItemAvailability(id, true)
    }

    /**
     * Desactivar item del menú (solo ADMIN)
     */
    suspend fun deactivateMenuItem(id: String): Result<MenuItem> {
        return toggleItemAvailability(id, false)
    }

    /**
     * Actualizar precio de un item (solo ADMIN)
     */
    suspend fun updateMenuItemPrice(id: String, newPrice: Double): Result<MenuItem> {
        return updateMenuItem(id, MenuItemUpdate(price = newPrice))
    }

    // =================== FUNCIONES DE UTILIDAD ===================

    /**
     * Obtener menú completo con categorías e items
     */
    suspend fun getFullMenu(): Result<FullMenu> {
        return try {
            val categoriesResult = getActiveCategories()

            categoriesResult.fold(
                onSuccess = { categoryList ->
                    val menuWithItems = mutableListOf<CategoryWithItems>()

                    // Para cada categoría, obtener sus items
                    for (category in categoryList.categories) {
                        val itemsResult = getMenuItemsByCategory(category.id, availableOnly = true)
                        itemsResult.fold(
                            onSuccess = { itemList ->
                                menuWithItems.add(
                                    CategoryWithItems(
                                        category = category,
                                        items = itemList.items
                                    )
                                )
                            },
                            onFailure = {
                                // Si falla obtener items de una categoría, agregar categoría vacía
                                menuWithItems.add(
                                    CategoryWithItems(
                                        category = category,
                                        items = emptyList()
                                    )
                                )
                            }
                        )
                    }

                    Result.success(FullMenu(categoriesWithItems = menuWithItems))
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al cargar menú completo: ${e.message}"))
        }
    }

    /**
     * Obtener items populares (simulado por ahora)
     */
    suspend fun getPopularItems(limit: Int = 10): Result<MenuItemList> {
        return getAvailableMenuItems().fold(
            onSuccess = { menuItemList ->
                // Por ahora retornamos los primeros items,
                // en el futuro se podría implementar lógica de popularidad real
                val popularItems = menuItemList.items.take(limit)
                Result.success(MenuItemList(items = popularItems, total = popularItems.size))
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Obtener items por rango de precio
     */
    suspend fun getItemsByPriceRange(minPrice: Double, maxPrice: Double): Result<MenuItemList> {
        return getMenuItems(
            minPrice = minPrice,
            maxPrice = maxPrice,
            available = true
        )
    }

    /**
     * Obtener items más baratos
     */
    suspend fun getCheapestItems(limit: Int = 10): Result<MenuItemList> {
        return getAvailableMenuItems().fold(
            onSuccess = { menuItemList ->
                val sortedItems = menuItemList.items.sortedBy { it.price }.take(limit)
                Result.success(MenuItemList(items = sortedItems, total = sortedItems.size))
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Obtener items más caros
     */
    suspend fun getMostExpensiveItems(limit: Int = 10): Result<MenuItemList> {
        return getAvailableMenuItems().fold(
            onSuccess = { menuItemList ->
                val sortedItems = menuItemList.items.sortedByDescending { it.price }.take(limit)
                Result.success(MenuItemList(items = sortedItems, total = sortedItems.size))
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Verificar si un item existe y está disponible
     */
    suspend fun isItemAvailable(itemId: String): Result<Boolean> {
        return getMenuItem(itemId).fold(
            onSuccess = { menuItem ->
                Result.success(menuItem.available)
            },
            onFailure = { error ->
                if (error.message?.contains("no encontrado") == true) {
                    Result.success(false)
                } else {
                    Result.failure(error)
                }
            }
        )
    }

    /**
     * Obtener estadísticas del menú (solo ADMIN)
     */
    suspend fun getMenuStats(): Result<MenuStats> {
        return try {
            val categoriesResult = getCategories()
            val allItemsResult = getMenuItems(limit = 1000)

            if (categoriesResult.isSuccess && allItemsResult.isSuccess) {
                val categories = categoriesResult.getOrNull()!!.categories
                val allItems = allItemsResult.getOrNull()!!.items

                val stats = MenuStats(
                    totalCategories = categories.size,
                    activeCategories = categories.count { it.active },
                    totalItems = allItems.size,
                    availableItems = allItems.count { it.available },
                    unavailableItems = allItems.count { !it.available },
                    averagePrice = if (allItems.isNotEmpty()) {
                        allItems.map { it.price }.average()
                    } else 0.0,
                    minPrice = allItems.minOfOrNull { it.price } ?: 0.0,
                    maxPrice = allItems.maxOfOrNull { it.price } ?: 0.0,
                    itemsPerCategory = categories.associate { category ->
                        category.name to allItems.count { it.category_id == category.id }
                    }
                )

                Result.success(stats)
            } else {
                Result.failure(Exception("Error al obtener datos para estadísticas"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al calcular estadísticas: ${e.message}"))
        }
    }

    /**
     * Refrescar caché del menú (para implementación futura)
     */
    suspend fun refreshMenuCache(): Result<Unit> {
        return try {
            // Por ahora solo retorna éxito
            // En el futuro se podría implementar caché local
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al refrescar caché: ${e.message}"))
        }
    }
}

// =================== CLASES DE DATOS ADICIONALES ===================

/**
 * Clase para representar el menú completo con categorías e items
 */
data class FullMenu(
    val categoriesWithItems: List<CategoryWithItems>
)

/**
 * Clase para representar una categoría con sus items
 */
data class CategoryWithItems(
    val category: Category,
    val items: List<MenuItem>
)

/**
 * Clase para estadísticas del menú
 */
data class MenuStats(
    val totalCategories: Int,
    val activeCategories: Int,
    val totalItems: Int,
    val availableItems: Int,
    val unavailableItems: Int,
    val averagePrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val itemsPerCategory: Map<String, Int>
)