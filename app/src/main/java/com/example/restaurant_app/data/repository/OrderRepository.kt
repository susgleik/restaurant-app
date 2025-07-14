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
     * Obtener pedidos del usuario actual
     */
    suspend fun getOrders(
        status: String? = null,
        skip: Int = 0,
        limit: Int = 50
    ): Result<OrderList> {
        return try {
            val response = orderApiService.getOrders(status, skip, limit)
            if (response.isSuccessful) {
                response.body()?.let { orderList ->
                    Result.success(orderList)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    403 -> "No autorizado"
                    400 -> "Parámetros inválidos"
                    else -> "Error al cargar pedidos: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtener pedido específico por ID
     */
    suspend fun getOrder(id: String): Result<Order> {
        return try {
            val response = orderApiService.getOrder(id)
            if (response.isSuccessful) {
                response.body()?.let { order ->
                    Result.success(order)
                } ?: Result.failure(Exception("Pedido no encontrado"))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Pedido no encontrado"
                    401 -> "Sesión expirada"
                    403 -> "No tienes acceso a este pedido"
                    400 -> "ID de pedido inválido"
                    else -> "Error al cargar pedido: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Crear nuevo pedido
     */
    suspend fun createOrder(order: OrderCreate): Result<Order> {
        return try {
            val response = orderApiService.createOrder(order)
            if (response.isSuccessful) {
                response.body()?.let { createdOrder ->
                    Result.success(createdOrder)
                } ?: Result.failure(Exception("Error al crear pedido"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos del pedido inválidos o items no disponibles"
                    401 -> "Sesión expirada"
                    403 -> "No autorizado"
                    404 -> "Uno o más items no encontrados"
                    422 -> "Datos mal formateados"
                    else -> "Error al crear pedido: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Crear pedido desde el carrito actual
     */
    suspend fun createOrderFromCart(notes: String? = null): Result<Order> {
        return try {
            val notesMap = mapOf("notes" to notes)
            val response = orderApiService.createOrderFromCart(notesMap)
            if (response.isSuccessful) {
                response.body()?.let { order ->
                    Result.success(order)
                } ?: Result.failure(Exception("Error al crear pedido desde carrito"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Carrito vacío o items no disponibles"
                    401 -> "Sesión expirada"
                    403 -> "No autorizado"
                    404 -> "Carrito no encontrado"
                    else -> "Error al crear pedido desde carrito: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Actualizar estado de un pedido (solo ADMIN)
     */
    suspend fun updateOrderStatus(id: String, status: OrderStatus): Result<Order> {
        return try {
            val response = orderApiService.updateOrderStatus(id, status.name)
            if (response.isSuccessful) {
                response.body()?.let { order ->
                    Result.success(order)
                } ?: Result.failure(Exception("Error al actualizar estado"))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Pedido no encontrado"
                    400 -> "Estado inválido o transición no permitida"
                    401 -> "Sesión expirada"
                    403 -> "Sin permisos de administrador"
                    422 -> "Estado mal formateado"
                    else -> "Error al actualizar estado: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Cancelar pedido (solo si está en estado PENDING)
     */
    suspend fun cancelOrder(id: String): Result<Order> {
        return updateOrderStatus(id, OrderStatus.CANCELLED)
    }

    /**
     * Obtener pedidos activos del usuario (PENDING, IN_PREPARATION, READY)
     */
    suspend fun getActiveOrders(): Result<OrderList> {
        return try {
            // Hacer múltiples llamadas para cada estado activo
            val pendingResult = getOrders(status = "PENDING")
            val preparingResult = getOrders(status = "IN_PREPARATION")
            val readyResult = getOrders(status = "READY")

            val allOrders = mutableListOf<Order>()
            var totalCount = 0

            pendingResult.getOrNull()?.let {
                allOrders.addAll(it.orders)
                totalCount += it.total
            }
            preparingResult.getOrNull()?.let {
                allOrders.addAll(it.orders)
                totalCount += it.total
            }
            readyResult.getOrNull()?.let {
                allOrders.addAll(it.orders)
                totalCount += it.total
            }

            // Ordenar por fecha de creación (más recientes primero)
            val sortedOrders = allOrders.sortedByDescending { it.created_at }

            Result.success(OrderList(orders = sortedOrders, total = totalCount))
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener pedidos activos: ${e.message}"))
        }
    }

    /**
     * Obtener historial de pedidos (DELIVERED, CANCELLED)
     */
    suspend fun getOrderHistory(): Result<OrderList> {
        return try {
            val deliveredResult = getOrders(status = "DELIVERED")
            val cancelledResult = getOrders(status = "CANCELLED")

            val allOrders = mutableListOf<Order>()
            var totalCount = 0

            deliveredResult.getOrNull()?.let {
                allOrders.addAll(it.orders)
                totalCount += it.total
            }
            cancelledResult.getOrNull()?.let {
                allOrders.addAll(it.orders)
                totalCount += it.total
            }

            // Ordenar por fecha de creación (más recientes primero)
            val sortedOrders = allOrders.sortedByDescending { it.created_at }

            Result.success(OrderList(orders = sortedOrders, total = totalCount))
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener historial: ${e.message}"))
        }
    }

    /**
     * Obtener el pedido más reciente del usuario
     */
    suspend fun getLatestOrder(): Result<Order?> {
        return try {
            val result = getOrders(limit = 1)
            result.fold(
                onSuccess = { orderList ->
                    val latestOrder = orderList.orders.firstOrNull()
                    Result.success(latestOrder)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener último pedido: ${e.message}"))
        }
    }

    /**
     * Verificar si el usuario puede cancelar un pedido
     */
    suspend fun canCancelOrder(orderId: String): Result<Boolean> {
        return try {
            val result = getOrder(orderId)
            result.fold(
                onSuccess = { order ->
                    val canCancel = order.status == OrderStatus.PENDING
                    Result.success(canCancel)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al verificar cancelación: ${e.message}"))
        }
    }

    // =================== FUNCIONES PARA ADMIN ===================

    /**
     * Obtener todos los pedidos (solo ADMIN)
     */
    suspend fun getAllOrdersAdmin(
        status: String? = null,
        userId: String? = null,
        skip: Int = 0,
        limit: Int = 50
    ): Result<OrderList> {
        return try {
            val response = orderApiService.getAllOrdersAdmin(status, userId, skip, limit)
            if (response.isSuccessful) {
                response.body()?.let { orderList ->
                    Result.success(orderList)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    403 -> "Sin permisos de administrador"
                    400 -> "Parámetros inválidos"
                    else -> "Error al cargar pedidos: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtener pedidos pendientes para cocina (solo ADMIN)
     */
    suspend fun getPendingOrdersForKitchen(): Result<OrderList> {
        return getAllOrdersAdmin(status = "PENDING")
    }

    /**
     * Obtener pedidos en preparación (solo ADMIN)
     */
    suspend fun getOrdersInPreparation(): Result<OrderList> {
        return getAllOrdersAdmin(status = "IN_PREPARATION")
    }

    /**
     * Obtener pedidos listos para entregar (solo ADMIN)
     */
    suspend fun getReadyOrders(): Result<OrderList> {
        return getAllOrdersAdmin(status = "READY")
    }

    /**
     * Obtener todos los pedidos entregados (solo ADMIN)
     */
    suspend fun getDeliveredOrders(): Result<OrderList> {
        return getAllOrdersAdmin(status = "DELIVERED")
    }

    /**
     * Obtener todos los pedidos cancelados (solo ADMIN)
     */
    suspend fun getCancelledOrders(): Result<OrderList> {
        return getAllOrdersAdmin(status = "CANCELLED")
    }

    /**
     * Marcar pedido como en preparación (solo ADMIN)
     */
    suspend fun markOrderInPreparation(id: String): Result<Order> {
        return updateOrderStatus(id, OrderStatus.IN_PREPARATION)
    }

    /**
     * Marcar pedido como listo (solo ADMIN)
     */
    suspend fun markOrderReady(id: String): Result<Order> {
        return updateOrderStatus(id, OrderStatus.READY)
    }

    /**
     * Marcar pedido como entregado (solo ADMIN)
     */
    suspend fun markOrderDelivered(id: String): Result<Order> {
        return updateOrderStatus(id, OrderStatus.DELIVERED)
    }

    /**
     * Cancelar pedido como admin (solo ADMIN)
     */
    suspend fun cancelOrderAdmin(id: String): Result<Order> {
        return updateOrderStatus(id, OrderStatus.CANCELLED)
    }

    /**
     * Obtener estadísticas de pedidos del día (solo ADMIN)
     */
    suspend fun getTodayOrderStats(): Result<OrderStats> {
        return try {
            val allOrdersResult = getAllOrdersAdmin(limit = 1000) // Obtener más pedidos para estadísticas

            allOrdersResult.fold(
                onSuccess = { orderList ->
                    val today = java.time.LocalDate.now().toString()
                    val todayOrders = orderList.orders.filter { order ->
                        order.created_at.startsWith(today)
                    }

                    val stats = OrderStats(
                        total = todayOrders.size,
                        pending = todayOrders.count { it.status == OrderStatus.PENDING },
                        inPreparation = todayOrders.count { it.status == OrderStatus.IN_PREPARATION },
                        ready = todayOrders.count { it.status == OrderStatus.READY },
                        delivered = todayOrders.count { it.status == OrderStatus.DELIVERED },
                        cancelled = todayOrders.count { it.status == OrderStatus.CANCELLED },
                        totalRevenue = todayOrders
                            .filter { it.status == OrderStatus.DELIVERED }
                            .sumOf { it.total },
                        averageOrderValue = if (todayOrders.isNotEmpty()) {
                            todayOrders.sumOf { it.total } / todayOrders.size
                        } else 0.0
                    )

                    Result.success(stats)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener estadísticas: ${e.message}"))
        }
    }

    /**
     * Obtener pedidos de un usuario específico (solo ADMIN)
     */
    suspend fun getUserOrders(userId: String): Result<OrderList> {
        return getAllOrdersAdmin(userId = userId)
    }

    /**
     * Obtener resumen de todos los pedidos activos (solo ADMIN)
     */
    suspend fun getActiveOrdersSummary(): Result<AdminOrderSummary> {
        return try {
            val pendingResult = getPendingOrdersForKitchen()
            val preparingResult = getOrdersInPreparation()
            val readyResult = getReadyOrders()

            val summary = AdminOrderSummary(
                pendingCount = pendingResult.getOrNull()?.total ?: 0,
                preparingCount = preparingResult.getOrNull()?.total ?: 0,
                readyCount = readyResult.getOrNull()?.total ?: 0,
                totalActiveOrders = (pendingResult.getOrNull()?.total ?: 0) +
                        (preparingResult.getOrNull()?.total ?: 0) +
                        (readyResult.getOrNull()?.total ?: 0)
            )

            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener resumen: ${e.message}"))
        }
    }
}

/**
 * Clase de datos para estadísticas de pedidos
 */
data class OrderStats(
    val total: Int,
    val pending: Int,
    val inPreparation: Int,
    val ready: Int,
    val delivered: Int,
    val cancelled: Int,
    val totalRevenue: Double,
    val averageOrderValue: Double
)

/**
 * Clase de datos para resumen de pedidos activos (Admin)
 */
data class AdminOrderSummary(
    val pendingCount: Int,
    val preparingCount: Int,
    val readyCount: Int,
    val totalActiveOrders: Int
)