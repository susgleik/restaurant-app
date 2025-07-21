// data/repository/AdminOrderRepository.kt - Usando endpoints reales
package com.example.restaurant_app.data.repository

import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.remote.OrderApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminOrderRepository @Inject constructor(
    private val orderApiService: OrderApiService // Usando el servicio real
) {

    /**
     * Obtener todos los pedidos usando el endpoint real /orders/
     */
    suspend fun getAllOrders(
        skip: Int = 0,
        limit: Int = 100,
        status: OrderStatus? = null
    ): Result<OrderList> {
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
     * Obtener pedido por ID usando endpoint real
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
     * Actualizar estado de un pedido usando endpoint real
     */
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Order> {
        return try {
            val statusUpdate = OrderStatusUpdate(status = newStatus)
            val response = orderApiService.updateOrderStatus(orderId, statusUpdate)
            if (response.isSuccessful) {
                response.body()?.let { updatedOrder ->
                    Result.success(updatedOrder)
                } ?: Result.failure(Exception("Error al actualizar pedido"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Estado inválido o transición no permitida"
                    401 -> "Sesión expirada"
                    403 -> "No autorizado - Solo administradores"
                    404 -> "Pedido no encontrado"
                    422 -> "No se puede cambiar el estado de este pedido"
                    else -> "Error al actualizar pedido: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtener pedidos por rango de fechas - usar endpoint existente por ahora
     */
    suspend fun getOrdersByDateRange(
        startDate: String,
        endDate: String,
        status: OrderStatus? = null
    ): Result<OrderList> {
        // Por ahora, usar el endpoint regular y filtrar localmente
        return getAllOrders()
    }

    /**
     * Obtener estadísticas diarias - calcular desde pedidos reales
     */
    suspend fun getDailyOrderStats(date: String? = null): Result<OrderDailyStats> {
        return try {
            val ordersResult = getAllOrders()
            ordersResult.fold(
                onSuccess = { orderList ->
                    val orders = orderList.orders

                    // Calcular estadísticas reales desde los datos
                    val totalOrders = orders.size
                    val deliveredOrders = orders.filter { it.status == OrderStatus.DELIVERED }
                    val totalRevenue = deliveredOrders.sumOf { it.totalAmount }
                    val avgOrderValue = if (totalOrders > 0) totalRevenue / totalOrders else 0.0

                    val statusCounts = orders.groupingBy { it.status.name }.eachCount()

                    // Encontrar items más populares
                    val popularItems = orders.flatMap { order ->
                        order.items.map { item ->
                            PopularMenuItem(
                                menu_item_id = item.menu_item_id,
                                menu_item_name = item.menu_item_name,
                                quantity_sold = item.quantity,
                                revenue = item.subtotal
                            )
                        }
                    }.groupBy { it.menu_item_name }
                        .map { (name, items) ->
                            PopularMenuItem(
                                menu_item_id = items.first().menu_item_id,
                                menu_item_name = name,
                                quantity_sold = items.sumOf { it.quantity_sold },
                                revenue = items.sumOf { it.revenueAmount }.toString()
                            )
                        }.sortedByDescending { it.quantity_sold }
                        .take(5)

                    val stats = OrderDailyStats(
                        date = date ?: java.time.LocalDate.now().toString(),
                        total_orders = totalOrders,
                        total_revenue = String.format("%.2f", totalRevenue),
                        orders_by_status = statusCounts,
                        avg_order_value = String.format("%.2f", avgOrderValue),
                        peak_hour = null, // TODO: calcular desde created_at
                        most_popular_items = popularItems
                    )
                    Result.success(stats)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al calcular estadísticas: ${e.message}"))
        }
    }

    /**
     * Obtener resumen de pedidos activos desde datos reales
     */
    suspend fun getActiveOrdersSummary(): Result<ActiveOrdersSummary> {
        return try {
            val ordersResult = getAllOrders()
            ordersResult.fold(
                onSuccess = { orderList ->
                    val orders = orderList.orders
                    val activeOrders = orders.filter {
                        it.status in listOf(
                            OrderStatus.PENDING,
                            OrderStatus.IN_PREPARATION,
                            OrderStatus.READY
                        )
                    }

                    val pendingCount = activeOrders.count { it.status == OrderStatus.PENDING }
                    val preparingCount = activeOrders.count { it.status == OrderStatus.IN_PREPARATION }
                    val readyCount = activeOrders.count { it.status == OrderStatus.READY }

                    // Buscar pedidos urgentes (pendientes por más de 30 min)
                    val urgentOrders = activeOrders.filter { order ->
                        order.status == OrderStatus.PENDING &&
                                isOrderOlderThan(order.created_at, 30)
                    }.map { it.id }

                    val summary = ActiveOrdersSummary(
                        pending_count = pendingCount,
                        in_preparation_count = preparingCount,
                        ready_count = readyCount,
                        urgent_orders = urgentOrders,
                        avg_preparation_time = null, // TODO: calcular tiempo promedio
                        oldest_pending_order = activeOrders
                            .filter { it.status == OrderStatus.PENDING }
                            .minByOrNull { it.created_at }?.created_at
                    )
                    Result.success(summary)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener resumen: ${e.message}"))
        }
    }

    /**
     * Buscar pedidos por información del cliente usando endpoint real
     */
    suspend fun searchOrdersByCustomer(
        query: String,
        skip: Int = 0,
        limit: Int = 50
    ): Result<OrderList> {
        return try {
            // Obtener todos los pedidos y filtrar localmente
            val ordersResult = getAllOrders(skip = 0, limit = 200) // Obtener más para filtrar
            ordersResult.fold(
                onSuccess = { orderList ->
                    val filteredOrders = orderList.orders.filter { order ->
                        order.user_id.contains(query, ignoreCase = true) ||
                                order.displayCustomerName.contains(query, ignoreCase = true) ||
                                order.displayCustomerEmail.contains(query, ignoreCase = true)
                    }.drop(skip).take(limit)

                    Result.success(OrderList(
                        orders = filteredOrders,
                        total = filteredOrders.size
                    ))
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error en la búsqueda: ${e.message}"))
        }
    }

    /**
     * Obtener métricas de rendimiento desde datos reales
     */
    suspend fun getPerformanceMetrics(
        startDate: String? = null,
        endDate: String? = null
    ): Result<PerformanceMetrics> {
        return try {
            val ordersResult = getAllOrders()
            ordersResult.fold(
                onSuccess = { orderList ->
                    val orders = orderList.orders
                    val totalOrders = orders.size
                    val completedOrders = orders.count { it.status == OrderStatus.DELIVERED }
                    val cancelledOrders = orders.count { it.status == OrderStatus.CANCELLED }
                    val totalRevenue = orders.filter { it.status == OrderStatus.DELIVERED }
                        .sumOf { it.totalAmount }
                    val avgOrderValue = if (totalOrders > 0) totalRevenue / totalOrders else 0.0
                    val completionRate = if (totalOrders > 0) (completedOrders.toDouble() / totalOrders) * 100 else 0.0
                    val cancellationRate = if (totalOrders > 0) (cancelledOrders.toDouble() / totalOrders) * 100 else 0.0

                    // Agrupar por día para revenue_by_day
                    val revenueByDay = orders
                        .filter { it.status == OrderStatus.DELIVERED }
                        .groupBy { it.created_at.substringBefore("T") }
                        .map { (date, dayOrders) ->
                            val dayRevenue = dayOrders.sumOf { it.totalAmount }
                            val dayAvgOrder = if (dayOrders.isNotEmpty()) dayRevenue / dayOrders.size else 0.0
                            DailyRevenue(
                                date = date,
                                order_count = dayOrders.size,
                                revenue = String.format("%.2f", dayRevenue),
                                avg_order_value = String.format("%.2f", dayAvgOrder)
                            )
                        }.sortedByDescending { it.date }

                    val metrics = PerformanceMetrics(
                        total_orders = totalOrders,
                        completed_orders = completedOrders,
                        cancelled_orders = cancelledOrders,
                        total_revenue = String.format("%.2f", totalRevenue),
                        avg_order_value = String.format("%.2f", avgOrderValue),
                        completion_rate = completionRate,
                        cancellation_rate = cancellationRate,
                        avg_preparation_time = "0", // TODO: calcular tiempo real
                        customer_satisfaction = null,
                        busiest_hours = emptyList(), // TODO: calcular desde created_at
                        revenue_by_day = revenueByDay
                    )
                    Result.success(metrics)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al calcular métricas: ${e.message}"))
        }
    }

    /**
     * Procesar múltiples pedidos - PLACEHOLDER hasta implementar endpoint batch
     */
    suspend fun batchUpdateOrderStatus(
        orderIds: List<String>,
        newStatus: OrderStatus
    ): Result<BatchUpdateResult> {
        return try {
            // TODO: Implementar endpoint POST /admin/orders/batch-update
            val mockResult = BatchUpdateResult(
                success_count = 0, // No hacer cambios reales hasta tener endpoint
                error_count = orderIds.size,
                errors = listOf("Endpoint batch no implementado en el backend")
            )
            Result.success(mockResult)
        } catch (e: Exception) {
            Result.failure(Exception("Operación batch no disponible: ${e.message}"))
        }
    }

    /**
     * Obtener pedidos urgentes desde datos reales
     */
    suspend fun getUrgentOrders(minutesThreshold: Int = 30): Result<OrderList> {
        return try {
            val ordersResult = getAllOrders()
            ordersResult.fold(
                onSuccess = { orderList ->
                    val urgentOrders = orderList.orders.filter { order ->
                        order.status == OrderStatus.PENDING &&
                                isOrderOlderThan(order.created_at, minutesThreshold)
                    }
                    Result.success(OrderList(
                        orders = urgentOrders,
                        total = urgentOrders.size
                    ))
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al buscar pedidos urgentes: ${e.message}"))
        }
    }

    /**
     * Exportar pedidos a CSV usando datos reales
     */
    suspend fun exportOrdersToCSV(
        startDate: String? = null,
        endDate: String? = null,
        status: OrderStatus? = null
    ): Result<String> {
        return try {
            val ordersResult = getAllOrders()
            ordersResult.fold(
                onSuccess = { orderList ->
                    val csvData = buildString {
                        appendLine("Order ID,User ID,Status,Total,Items,Notes,Created At,Updated At")
                        orderList.orders.forEach { order ->
                            val itemsText = order.items.joinToString("; ") {
                                "${it.quantity}x ${it.menu_item_name}"
                            }
                            appendLine("${order.id},${order.user_id},${order.status},${order.total},\"$itemsText\",\"${order.notes ?: ""}\",${order.created_at},${order.updated_at}")
                        }
                    }
                    Result.success(csvData)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al exportar CSV: ${e.message}"))
        }
    }

    /**
     * Obtener notificaciones desde pedidos reales
     */
    suspend fun getOrderNotifications(): Result<List<OrderNotification>> {
        return try {
            val ordersResult = getAllOrders()
            ordersResult.fold(
                onSuccess = { orderList ->
                    val notifications = mutableListOf<OrderNotification>()

                    // Crear notificaciones para pedidos urgentes
                    orderList.orders.filter {
                        it.status == OrderStatus.PENDING && isOrderOlderThan(it.created_at, 30)
                    }.forEach { order ->
                        notifications.add(
                            OrderNotification(
                                id = "urgent_${order.id}",
                                type = NotificationType.ORDER_URGENT,
                                order_id = order.id,
                                message = "Pedido #${order.id.takeLast(8)} pendiente por más de 30 minutos",
                                timestamp = java.time.Instant.now().toString(),
                                is_read = false,
                                priority = NotificationPriority.URGENT
                            )
                        )
                    }

                    // Crear notificaciones para nuevos pedidos (últimos 10 min)
                    orderList.orders.filter {
                        it.status == OrderStatus.PENDING && isOrderNewerThan(it.created_at, 10)
                    }.forEach { order ->
                        notifications.add(
                            OrderNotification(
                                id = "new_${order.id}",
                                type = NotificationType.NEW_ORDER,
                                order_id = order.id,
                                message = "Nuevo pedido recibido #${order.id.takeLast(8)}",
                                timestamp = order.created_at,
                                is_read = false,
                                priority = NotificationPriority.HIGH
                            )
                        )
                    }

                    Result.success(notifications.sortedByDescending { it.timestamp })
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener notificaciones: ${e.message}"))
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // FUNCIONES AUXILIARES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Verificar si un pedido es más antiguo que X minutos
     */
    private fun isOrderOlderThan(createdAt: String, minutes: Int): Boolean {
        return try {
            val orderTime = java.time.LocalDateTime.parse(
                createdAt.replace("T", "T").substringBefore(".")
            )
            val now = java.time.LocalDateTime.now()
            val diffMinutes = java.time.Duration.between(orderTime, now).toMinutes()
            diffMinutes > minutes
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verificar si un pedido es más nuevo que X minutos
     */
    private fun isOrderNewerThan(createdAt: String, minutes: Int): Boolean {
        return try {
            val orderTime = java.time.LocalDateTime.parse(
                createdAt.replace("T", "T").substringBefore(".")
            )
            val now = java.time.LocalDateTime.now()
            val diffMinutes = java.time.Duration.between(orderTime, now).toMinutes()
            diffMinutes <= minutes
        } catch (e: Exception) {
            false
        }
    }
}