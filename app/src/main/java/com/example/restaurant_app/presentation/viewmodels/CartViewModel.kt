// presentation/viewmodels/CartViewModel.kt
package com.example.restaurant_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurant_app.data.models.CartItem
import com.example.restaurant_app.data.models.CartList
import com.example.restaurant_app.data.repository.CartRepository
import com.example.restaurant_app.data.repository.CartSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val isLoading: Boolean = false,
    val cart: CartList? = null,
    val errorMessage: String? = null,
    val isUpdating: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _cartSummary = MutableStateFlow<CartSummary?>(null)
    val cartSummary: StateFlow<CartSummary?> = _cartSummary.asStateFlow()

    init {
        loadCart()
    }

    /**
     * Cargar carrito
     */
    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            cartRepository.getCart().fold(
                onSuccess = { cartList ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cart = cartList
                    )
                    updateCartSummary()
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
     * Agregar item al carrito
     */
    fun addToCart(menuItemId: String, quantity: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)

            cartRepository.addToCart(menuItemId, quantity).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        successMessage = "Producto agregado al carrito"
                    )
                    loadCart() // Recargar carrito
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Actualizar cantidad de item
     */
    fun updateItemQuantity(cartItemId: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(cartItemId)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)

            cartRepository.updateCartItem(cartItemId, quantity).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadCart() // Recargar carrito
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Incrementar cantidad de item por menu item ID
     */
    fun incrementItemQuantity(menuItemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)

            cartRepository.incrementItemQuantity(menuItemId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadCart() // Recargar carrito
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Decrementar cantidad de item por menu item ID
     */
    fun decrementItemQuantity(menuItemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)

            cartRepository.decrementItemQuantity(menuItemId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadCart() // Recargar carrito
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Eliminar item del carrito
     */
    fun removeItem(cartItemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)

            cartRepository.removeFromCart(cartItemId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        successMessage = "Producto eliminado del carrito"
                    )
                    loadCart() // Recargar carrito
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Limpiar todo el carrito
     */
    fun clearCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null)

            cartRepository.clearCart().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        successMessage = "Carrito vaciado"
                    )
                    loadCart() // Recargar carrito
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Obtener item del carrito por menu item ID
     */
    fun getCartItemByMenuId(menuItemId: String): CartItem? {
        return _uiState.value.cart?.items?.find { it.menu_item_id == menuItemId }
    }

    /**
     * Verificar si un item está en el carrito
     */
    fun isItemInCart(menuItemId: String): Boolean {
        return getCartItemByMenuId(menuItemId) != null
    }

    /**
     * Obtener cantidad de un item específico
     */
    fun getItemQuantity(menuItemId: String): Int {
        return getCartItemByMenuId(menuItemId)?.quantity ?: 0
    }

    /**
     * Actualizar resumen del carrito
     */
    private fun updateCartSummary() {
        viewModelScope.launch {
            cartRepository.getCartSummary().fold(
                onSuccess = { summary ->
                    _cartSummary.value = summary
                },
                onFailure = {
                    _cartSummary.value = null
                }
            )
        }
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
     * Verificar si el carrito está vacío
     */
    fun isCartEmpty(): Boolean {
        return _uiState.value.cart?.is_empty ?: true
    }

    /**
     * Obtener total del carrito
     */
    fun getCartTotal(): Double {
        return _uiState.value.cart?.totalAmount ?: 0.0
    }

    /**
     * Obtener número total de items
     */
    fun getTotalItems(): Int {
        return _uiState.value.cart?.total_quantity ?: 0
    }
}