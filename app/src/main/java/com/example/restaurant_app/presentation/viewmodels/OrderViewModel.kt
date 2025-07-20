// presentation/viewmodels/OrderViewModel.kt
package com.example.restaurant_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurant_app.data.models.Order
import com.example.restaurant_app.data.models.OrderItemCreate
import com.example.restaurant_app.data.models.OrderStatus
import com.example.restaurant_app.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val activeOrders: List<Order> = emptyList(),
    val orderHistory: List<Order> = emptyList(),
    val selectedOrder: Order? = null,
    val errorMessage: String? = null,
    val isCreatingOrder: Boolean = false,
    val successMessage: String? = null,
    val isCancelling: Boolean = false
)

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    /**
     * Cargar todos los pedidos
     */
    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            orderRepository.getOrders().fold(
                onSuccess = { orderList ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orders = orderList.orders
                    )
                    separateOrdersByStatus()
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
     * Cargar pedidos activos
     */
    fun loadActiveOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            orderRepository.getActiveOrders().fold(
                onSuccess = { activeOrders ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        activeOrders = activeOrders
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
     * Cargar historial de pedidos
     */
    fun loadOrderHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            orderRepository.getOrderHistory().fold(
                onSuccess = { historyOrders ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orderHistory = historyOrders
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
     * Obtener pedido por ID
     */
    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            orderRepository.getOrderById(orderId).fold(
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
     * Crear pedido desde carrito
     */
    fun createOrderFromCart(notes: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingOrder = true, errorMessage = null)

            orderRepository.createOrderFromCart(notes).fold(
                onSuccess = { order ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrder = false,
                        successMessage = "Pedido creado exitosamente",
                        selectedOrder = order
                    )
                    loadOrders() // Recargar lista de pedidos
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrder = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Crear pedido manual
     */
    fun createOrder(items: List<OrderItemCreate>, notes: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingOrder = true, errorMessage = null)

            orderRepository.createOrder(items, notes).fold(
                onSuccess = { order ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrder = false,
                        successMessage = "Pedido creado exitosamente",
                        selectedOrder = order
                    )
                    loadOrders() // Recargar lista de pedidos
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingOrder = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Cancelar pedido
     */
    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCancelling = true, errorMessage = null)

            orderRepository.cancelOrder(orderId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isCancelling = false,
                        successMessage = "Pedido cancelado exitosamente"
                    )
                    loadOrders() // Recargar lista de pedidos
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isCancelling = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Separar pedidos por estado
     */
    private fun separateOrdersByStatus() {
        val currentOrders = _uiState.value.orders

        val active = currentOrders.filter { order ->
            order.status in listOf(
                OrderStatus.PENDING,
                OrderStatus.IN_PREPARATION,
                OrderStatus.READY
            )
        }.sortedByDescending { it.created_at }

        val history = currentOrders.filter { order ->
            order.status in listOf(
                OrderStatus.DELIVERED,
                OrderStatus.CANCELLED
            )
        }.sortedByDescending { it.created_at }

        _uiState.value = _uiState.value.copy(
            activeOrders = active,
            orderHistory = history
        )
    }

    /**
     * Verificar si hay pedidos pendientes
     */
    fun hasPendingOrders(): Boolean {
        return _uiState.value.activeOrders.any { it.status == OrderStatus.PENDING }
    }

    /**
     * Obtener pedidos por estado
     */
    fun getOrdersByStatus(status: OrderStatus): List<Order> {
        return _uiState.value.orders.filter { it.status == status }
    }

    /**
     * Obtener conteo de pedidos activos
     */
    fun getActiveOrdersCount(): Int {
        return _uiState.value.activeOrders.size
    }

    /**
     * Limpiar mensajes
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    /**
     * Limpiar pedido seleccionado
     */
    fun clearSelectedOrder() {
        _uiState.value = _uiState.value.copy(selectedOrder = null)
    }

    /**
     * Refresh orders
     */
    fun refreshOrders() {
        loadOrders()
    }
}