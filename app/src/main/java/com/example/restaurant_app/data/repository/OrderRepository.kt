// data/repository/OrderRepository.kt
package com.example.restaurant_app.data.repository

import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.remote.OrderApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderApiService: OrderApiService
) {

    /**
     * Obtener lista de pedidos del usuario
     */
    suspend fun getOrders(skip: Int = 0, limit: Int = 50): Result<OrderList> {
        return try {
            val response = orderApiService.getOrders(skip, limit)
            if (response.isSuccessful) {
                response.body()?.let { orderList ->
                    Result.success(orderList)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    403 -> "No autorizado"
                    else -> "Error al cargar pedidos: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtener pedido por ID
     */
    suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val response = orderApiService.getOrderById(orderId)
            if (response.isSuccessful) {
                response.body()?.let { order ->
                    Result.success(order)
                } ?: Result.failure(Exception("Pedido no encontrado"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    403 -> "No autorizado"
                    404 -> "Pedido no encontrado"
                    else -> "Error al cargar pedido: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Crear nuevo pedido manualmente
     */
    suspend fun createOrder(items: List<OrderItemCreate>, notes: String? = null): Result<Order> {
        return try {
            val orderCreate = OrderCreate(items, notes)
            val response = orderApiService.createOrder(orderCreate)
            if (response.isSuccessful) {
                response.body()?.let { order ->
                    Result.success(order)
                } ?: Result.failure(Exception("Error al crear pedido"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos del pedido inválidos"
                    401 -> "Sesión expirada"
                    403 -> "No autorizado"
                    422 -> "Items no válidos o no disponibles"
                    else -> "Error al crear pedido: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Crear pedido desde el carrito
     */
    suspend fun createOrderFromCart(notes: String? = null): Result<Order> {
        return try {
            val response = orderApiService.createOrderFromCart(notes)
            if (response.isSuccessful) {
                response.body()?.let { order ->
                    Result.success(order)
                } ?: Result.failure(Exception("Error al crear pedido desde carrito"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Carrito vacío o items no válidos"
                    401 -> "Sesión expirada"
                    403 -> "No autorizado"
                    422 -> "Items del carrito no disponibles"
                    else -> "Error al crear pedido: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Cancelar pedido
     */
    suspend fun cancelOrder(orderId: String): Result<Unit> {
        return try {
            val response = orderApiService.cancelOrder(orderId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "No se puede cancelar este pedido"
                    401 -> "Sesión expirada"
                    403 -> "No autorizado"
                    404 -> "Pedido no encontrado"
                    else -> "Error al cancelar pedido: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtener pedidos activos (PENDING, IN_PREPARATION, READY)
     */
    suspend fun getActiveOrders(): Result<List<Order>> {
        return getOrders().fold(
            onSuccess = { orderList ->
                val activeOrders = orderList.orders.filter { order ->
                    order.status in listOf(
                        OrderStatus.PENDING,
                        OrderStatus.IN_PREPARATION,
                        OrderStatus.READY
                    )
                }.sortedByDescending { it.created_at }
                Result.success(activeOrders)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Obtener historial de pedidos (DELIVERED, CANCELLED)
     */
    suspend fun getOrderHistory(): Result<List<Order>> {
        return getOrders().fold(
            onSuccess = { orderList ->
                val historyOrders = orderList.orders.filter { order ->
                    order.status in listOf(
                        OrderStatus.DELIVERED,
                        OrderStatus.CANCELLED
                    )
                }.sortedByDescending { it.created_at }
                Result.success(historyOrders)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Verificar si hay pedidos pendientes
     */
    suspend fun hasPendingOrders(): Result<Boolean> {
        return getActiveOrders().fold(
            onSuccess = { activeOrders ->
                val hasPending = activeOrders.any { it.status == OrderStatus.PENDING }
                Result.success(hasPending)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Obtener conteo de pedidos por estado
     */
    suspend fun getOrderStatusCounts(): Result<Map<OrderStatus, Int>> {
        return getOrders().fold(
            onSuccess = { orderList ->
                val counts = orderList.orders.groupingBy { it.status }.eachCount()
                Result.success(counts)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}