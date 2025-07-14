// data/repository/CartRepository.kt
package com.example.restaurant_app.data.repository

import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.remote.CartApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val cartApiService: CartApiService
) {

    /**
     * Obtener carrito del usuario actual
     */
    suspend fun getCart(): Result<CartList> {
        return try {
            val response = cartApiService.getCart()
            if (response.isSuccessful) {
                response.body()?.let { cartList ->
                    Result.success(cartList)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    404 -> "Carrito no encontrado"
                    403 -> "No autorizado"
                    else -> "Error al cargar carrito: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Agregar item al carrito
     */
    suspend fun addToCart(item: CartItemCreate): Result<CartItem> {
        return try {
            val response = cartApiService.addToCart(item)
            if (response.isSuccessful) {
                response.body()?.let { cartItem ->
                    Result.success(cartItem)
                } ?: Result.failure(Exception("Error al agregar al carrito"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos inválidos o item no disponible"
                    401 -> "Sesión expirada"
                    403 -> "No autorizado"
                    404 -> "Item del menú no encontrado"
                    422 -> "Cantidad inválida"
                    else -> "Error al agregar al carrito: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Actualizar cantidad de item en carrito
     */
    suspend fun updateCartItem(id: String, item: CartItemUpdate): Result<CartItem> {
        return try {
            val response = cartApiService.updateCartItem(id, item)
            if (response.isSuccessful) {
                response.body()?.let { cartItem ->
                    Result.success(cartItem)
                } ?: Result.failure(Exception("Error al actualizar item"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Cantidad inválida"
                    401 -> "Sesión expirada"
                    403 -> "No autorizado"
                    404 -> "Item no encontrado en carrito"
                    422 -> "Datos inválidos"
                    else -> "Error al actualizar item: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Eliminar item del carrito
     */
    suspend fun removeFromCart(id: String): Result<Unit> {
        return try {
            val response = cartApiService.removeFromCart(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    403 -> "No autorizado"
                    404 -> "Item no encontrado en carrito"
                    else -> "Error al eliminar item: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Limpiar todo el carrito
     */
    suspend fun clearCart(): Result<Unit> {
        return try {
            val response = cartApiService.clearCart()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    403 -> "No autorizado"
                    else -> "Error al limpiar carrito: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Verificar si hay items en el carrito
     */
    suspend fun hasItemsInCart(): Result<Boolean> {
        return getCart().fold(
            onSuccess = { cartList ->
                Result.success(cartList.items.isNotEmpty())
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Obtener cantidad total de items en carrito
     */
    suspend fun getCartItemCount(): Result<Int> {
        return getCart().fold(
            onSuccess = { cartList ->
                val totalItems = cartList.items.sumOf { it.quantity }
                Result.success(totalItems)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Obtener monto total del carrito
     */
    suspend fun getCartTotal(): Result<Double> {
        return getCart().fold(
            onSuccess = { cartList ->
                Result.success(cartList.total_amount)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Verificar si un item específico está en el carrito
     */
    suspend fun isItemInCart(menuItemId: String): Result<Boolean> {
        return getCart().fold(
            onSuccess = { cartList ->
                val isInCart = cartList.items.any { it.menu_item_id == menuItemId }
                Result.success(isInCart)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Obtener la cantidad de un item específico en el carrito
     */
    suspend fun getItemQuantityInCart(menuItemId: String): Result<Int> {
        return getCart().fold(
            onSuccess = { cartList ->
                val cartItem = cartList.items.find { it.menu_item_id == menuItemId }
                Result.success(cartItem?.quantity ?: 0)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Incrementar cantidad de un item (si existe) o agregarlo (si no existe)
     */
    suspend fun incrementItemQuantity(menuItemId: String): Result<CartItem> {
        return try {
            // Primero verificar si el item ya está en el carrito
            val cartResult = getCart()
            cartResult.fold(
                onSuccess = { cartList ->
                    val existingItem = cartList.items.find { it.menu_item_id == menuItemId }

                    if (existingItem != null) {
                        // Si existe, incrementar cantidad
                        val newQuantity = existingItem.quantity + 1
                        updateCartItem(existingItem.id, CartItemUpdate(newQuantity))
                    } else {
                        // Si no existe, agregar nuevo
                        addToCart(CartItemCreate(menuItemId, 1))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al incrementar cantidad: ${e.message}"))
        }
    }

    /**
     * Decrementar cantidad de un item (eliminar si llega a 0)
     */
    suspend fun decrementItemQuantity(menuItemId: String): Result<Unit> {
        return try {
            val cartResult = getCart()
            cartResult.fold(
                onSuccess = { cartList ->
                    val existingItem = cartList.items.find { it.menu_item_id == menuItemId }

                    if (existingItem != null) {
                        if (existingItem.quantity > 1) {
                            // Si la cantidad es mayor a 1, decrementar
                            val newQuantity = existingItem.quantity - 1
                            updateCartItem(existingItem.id, CartItemUpdate(newQuantity))
                                .fold(
                                    onSuccess = { Result.success(Unit) },
                                    onFailure = { error -> Result.failure(error) }
                                )
                        } else {
                            // Si la cantidad es 1, eliminar el item
                            removeFromCart(existingItem.id)
                        }
                    } else {
                        Result.failure(Exception("Item no encontrado en el carrito"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al decrementar cantidad: ${e.message}"))
        }
    }

    /**
     * Establecer cantidad específica de un item
     */
    suspend fun setItemQuantity(menuItemId: String, quantity: Int): Result<CartItem> {
        return try {
            if (quantity <= 0) {
                // Si la cantidad es 0 o menor, eliminar el item
                val cartResult = getCart()
                cartResult.fold(
                    onSuccess = { cartList ->
                        val existingItem = cartList.items.find { it.menu_item_id == menuItemId }
                        if (existingItem != null) {
                            removeFromCart(existingItem.id).fold(
                                onSuccess = {
                                    Result.failure(Exception("Item eliminado del carrito"))
                                },
                                onFailure = { error -> Result.failure(error) }
                            )
                        } else {
                            Result.failure(Exception("Item no encontrado en el carrito"))
                        }
                    },
                    onFailure = { error -> Result.failure(error) }
                )
            } else {
                // Si la cantidad es válida, verificar si existe el item
                val cartResult = getCart()
                cartResult.fold(
                    onSuccess = { cartList ->
                        val existingItem = cartList.items.find { it.menu_item_id == menuItemId }

                        if (existingItem != null) {
                            // Actualizar cantidad existente
                            updateCartItem(existingItem.id, CartItemUpdate(quantity))
                        } else {
                            // Agregar nuevo item con la cantidad especificada
                            addToCart(CartItemCreate(menuItemId, quantity))
                        }
                    },
                    onFailure = { error -> Result.failure(error) }
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al establecer cantidad: ${e.message}"))
        }
    }

    /**
     * Obtener resumen del carrito (para mostrar en UI)
     */
    suspend fun getCartSummary(): Result<CartSummary> {
        return getCart().fold(
            onSuccess = { cartList ->
                val summary = CartSummary(
                    itemCount = cartList.items.sumOf { it.quantity },
                    totalAmount = cartList.total_amount,
                    itemsCount = cartList.items.size,
                    isEmpty = cartList.items.isEmpty()
                )
                Result.success(summary)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}

/**
 * Clase de datos para resumen del carrito
 */
data class CartSummary(
    val itemCount: Int,        // Cantidad total de items (suma de cantidades)
    val totalAmount: Double,   // Monto total
    val itemsCount: Int,       // Número de tipos de items diferentes
    val isEmpty: Boolean       // Si el carrito está vacío
)