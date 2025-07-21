// presentation/viewmodels/AdminOrderViewModel.kt
package com.example.restaurant_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurant_app.data.models.Order
import com.example.restaurant_app.data.models.OrderStatus
import com.example.restaurant_app.data.repository.AdminOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminOrderUiState(
    val isLoading: Boolean = false,
    val allOrders: List<Order> = emptyList(),
    val activeOrders: List<Order> = emptyList(),
    val orderHistory: List<Order> = emptyList(),
    val selectedOrder: Order? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isUpdatingStatus: Boolean = false,

    // Estadísticas
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val avgOrderValue: Double = 0.0,
    val statusCounts: Map<OrderStatus, Int> = emptyMap(),

    // Filtros
    val selectedStatusFilter: OrderStatus? = null,
    val dateFilter: String? = null
)

@HiltViewModel
class AdminOrderViewModel @Inject constructor(
    private val adminOrderRepository: AdminOrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminOrderUiState())
    val uiState: StateFlow<AdminOrderUiState> = _uiState.asStateFlow()

    init {
        loadAllOrders()
    }

    /**
     * Cargar todos los pedidos para administradores
     */
    fun loadAllOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            adminOrderRepository.getAllOrders().fold(
                onSuccess = { orderList ->
                    val orders = orderList.orders
                    val active = orders.filter { order ->
                        order.status in listOf(
                            OrderStatus.PENDING,
                            OrderStatus.IN_PREPARATION,
                            OrderStatus.READY
                        )
                    }.sortedWith(
                        compareBy<Order> { order ->
                            // Ordenar por prioridad: PENDING > IN_PREPARATION > READY
                            when (order.status) {
                                OrderStatus.PENDING -> 0
                                OrderStatus.IN_PREPARATION -> 1
                                OrderStatus.READY -> 2
                                else -> 3
                            }
                        }.thenBy { it.created_at } // Más antiguos primero
                    )

                    val history = orders.filter { order ->
                        order.status in listOf(
                            OrderStatus.DELIVERED,
                            OrderStatus.CANCELLED
                        )
                    }.sortedByDescending { it.updated_at } // Más recientes primero

                    // Calcular estadísticas
                    val totalRevenue = orders.filter { it.status == OrderStatus.DELIVERED }
                        .sumOf { it.totalAmount }
                    val avgOrderValue = if (orders.isNotEmpty()) totalRevenue / orders.size else 0.0
                    val statusCounts = orders.groupingBy { it.status }.eachCount()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allOrders = orders,
                        activeOrders = active,
                        orderHistory = history,
                        totalOrders = orders.size,
                        totalRevenue = totalRevenue,
                        avgOrderValue = avgOrderValue,
                        statusCounts = statusCounts
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Actualizar estado de un pedido
     */
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingStatus = true, errorMessage = null)

            adminOrderRepository.updateOrderStatus(orderId, newStatus).fold(
                onSuccess = { updatedOrder ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingStatus = false,
                        successMessage = "Estado del pedido actualizado correctamente"
                    )
                    // Recargar pedidos para reflejar los cambios
                    loadAllOrders()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingStatus = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Obtener detalles de un pedido específico
     */
    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            adminOrderRepository.getOrderById(orderId).fold(
                onSuccess = { order ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedOrder = order
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Filtrar pedidos por estado
     */
    fun filterOrdersByStatus(status: OrderStatus?) {
        _uiState.value = _uiState.value.copy(selectedStatusFilter = status)

        val filteredActive = if (status != null) {
            _uiState.value.allOrders.filter { it.status == status && isActiveStatus(it.status) }
        } else {
            _uiState.value.allOrders.filter { isActiveStatus(it.status) }
        }

        val filteredHistory = if (status != null) {
            _uiState.value.allOrders.filter { it.status == status && !isActiveStatus(it.status) }
        } else {
            _uiState.value.allOrders.filter { !isActiveStatus(it.status) }
        }

        _uiState.value = _uiState.value.copy(
            activeOrders = filteredActive.sortedWith(
                compareBy<Order> { order ->
                    when (order.status) {
                        OrderStatus.PENDING -> 0
                        OrderStatus.IN_PREPARATION -> 1
                        OrderStatus.READY -> 2
                        else -> 3
                    }
                }.thenBy { it.created_at }
            ),
            orderHistory = filteredHistory.sortedByDescending { it.updated_at }
        )
    }

    /**
     * Obtener pedidos por rango de fechas
     */
    fun filterOrdersByDateRange(startDate: String, endDate: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            adminOrderRepository.getOrdersByDateRange(startDate, endDate).fold(
                onSuccess = { orderList ->
                    val orders = orderList.orders
                    val active = orders.filter { isActiveStatus(it.status) }
                        .sortedWith(
                            compareBy<Order> { order ->
                                when (order.status) {
                                    OrderStatus.PENDING -> 0
                                    OrderStatus.IN_PREPARATION -> 1
                                    OrderStatus.READY -> 2
                                    else -> 3
                                }
                            }.thenBy { it.created_at }
                        )

                    val history = orders.filter { !isActiveStatus(it.status) }
                        .sortedByDescending { it.updated_at }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allOrders = orders,
                        activeOrders = active,
                        orderHistory = history,
                        dateFilter = "$startDate - $endDate"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Obtener estadísticas de pedidos por día
     */
    fun loadDailyStats() {
        viewModelScope.launch {
            adminOrderRepository.getDailyOrderStats().fold(
                onSuccess = { stats ->
                    // Aquí puedes manejar las estadísticas diarias
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Estadísticas cargadas correctamente"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Procesar múltiples pedidos (para operaciones batch)
     */
    fun processBatchOrders(orderIds: List<String>, newStatus: OrderStatus) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingStatus = true, errorMessage = null)

            var successCount = 0
            var errorCount = 0

            orderIds.forEach { orderId ->
                adminOrderRepository.updateOrderStatus(orderId, newStatus).fold(
                    onSuccess = { successCount++ },
                    onFailure = { errorCount++ }
                )
            }

            _uiState.value = _uiState.value.copy(
                isUpdatingStatus = false,
                successMessage = if (errorCount == 0) {
                    "Todos los pedidos ($successCount) fueron actualizados correctamente"
                } else {
                    "$successCount pedidos actualizados, $errorCount con errores"
                }
            )

            // Recargar datos
            loadAllOrders()
        }
    }

    /**
     * Buscar pedidos por cliente
     */
    fun searchOrdersByCustomer(customerName: String) {
        if (customerName.isBlank()) {
            // Restaurar vista completa
            loadAllOrders()
            return
        }

        val filteredOrders = _uiState.value.allOrders.filter { order ->
            // Usar los campos derivados que manejan valores nulos
            order.displayCustomerName.contains(customerName, ignoreCase = true) ||
                    order.displayCustomerEmail.contains(customerName, ignoreCase = true) ||
                    order.user_id.contains(customerName, ignoreCase = true)
        }

        val active = filteredOrders.filter { isActiveStatus(it.status) }
        val history = filteredOrders.filter { !isActiveStatus(it.status) }

        _uiState.value = _uiState.value.copy(
            activeOrders = active,
            orderHistory = history
        )
    }

    /**
     * Obtener pedidos urgentes (pendientes por más de X minutos)
     */
    fun getUrgentOrders(minutesThreshold: Int = 30): List<Order> {
        val currentTime = System.currentTimeMillis()
        return _uiState.value.activeOrders.filter { order ->
            order.status == OrderStatus.PENDING &&
                    isOrderOlderThan(order.created_at, minutesThreshold, currentTime)
        }
    }

    /**
     * Marcar pedido como prioritario
     */
    fun markOrderAsPriority(orderId: String) {
        // Esta funcionalidad requeriría modificar el backend
        // Por ahora, podemos ordenar los pedidos localmente
        val currentActive = _uiState.value.activeOrders.toMutableList()
        val priorityOrder = currentActive.find { it.id == orderId }

        priorityOrder?.let { order ->
            currentActive.remove(order)
            currentActive.add(0, order) // Mover al principio

            _uiState.value = _uiState.value.copy(
                activeOrders = currentActive,
                successMessage = "Pedido marcado como prioritario"
            )
        }
    }

    /**
     * Obtener resumen del día actual
     */
    fun getTodaySummary(): Map<String, Any> {
        val todayOrders = _uiState.value.allOrders.filter {
            isOrderFromToday(it.created_at)
        }

        return mapOf(
            "totalOrders" to todayOrders.size,
            "revenue" to todayOrders.filter { it.status == OrderStatus.DELIVERED }
                .sumOf { it.totalAmount },
            "pending" to todayOrders.count { it.status == OrderStatus.PENDING },
            "inPreparation" to todayOrders.count { it.status == OrderStatus.IN_PREPARATION },
            "ready" to todayOrders.count { it.status == OrderStatus.READY },
            "delivered" to todayOrders.count { it.status == OrderStatus.DELIVERED },
            "cancelled" to todayOrders.count { it.status == OrderStatus.CANCELLED }
        )
    }

    /**
     * Limpiar mensajes de estado
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    /**
     * Limpiar filtros aplicados
     */
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedStatusFilter = null,
            dateFilter = null
        )
        loadAllOrders()
    }

    /**
     * Limpiar pedido seleccionado
     */
    fun clearSelectedOrder() {
        _uiState.value = _uiState.value.copy(selectedOrder = null)
    }

    /**
     * Refresh datos
     */
    fun refreshData() {
        loadAllOrders()
    }

    // Funciones auxiliares privadas
    private fun isActiveStatus(status: OrderStatus): Boolean {
        return status in listOf(
            OrderStatus.PENDING,
            OrderStatus.IN_PREPARATION,
            OrderStatus.READY
        )
    }

    private fun isOrderOlderThan(createdAt: String, minutes: Int, currentTime: Long): Boolean {
        return try {
            // Implementar lógica de comparación de tiempo
            // Esta es una implementación simplificada
            true // Por ahora retorna true para test
        } catch (e: Exception) {
            false
        }
    }

    private fun isOrderFromToday(createdAt: String): Boolean {
        return try {
            // Implementar lógica para verificar si el pedido es de hoy
            // Esta es una implementación simplificada
            true // Por ahora retorna true para test
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtener métricas de rendimiento
     */
    fun getPerformanceMetrics(): Map<String, Double> {
        val deliveredOrders = _uiState.value.allOrders.filter {
            it.status == OrderStatus.DELIVERED
        }

        return mapOf(
            "averageOrderValue" to _uiState.value.avgOrderValue,
            "deliveryRate" to if (_uiState.value.totalOrders > 0) {
                (deliveredOrders.size.toDouble() / _uiState.value.totalOrders) * 100
            } else 0.0,
            "cancellationRate" to if (_uiState.value.totalOrders > 0) {
                (_uiState.value.statusCounts[OrderStatus.CANCELLED]?.toDouble() ?: 0.0) / _uiState.value.totalOrders * 100
            } else 0.0,
            "totalRevenue" to _uiState.value.totalRevenue
        )
    }
}