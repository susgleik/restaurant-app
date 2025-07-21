// presentation/viewmodels/AuthViewModel.kt - Actualizado con roles
package com.example.restaurant_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurant_app.data.models.User
import com.example.restaurant_app.data.repository.AuthRepository
import com.example.restaurant_app.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
    val isRegisterSuccessful: Boolean = false,
    // ✅ NUEVAS PROPIEDADES para facilitar acceso al rol
    val userRole: String? = null,
    val username: String? = null,
    val userEmail: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkLoginStatus()
    }

    /**
     * Verificar si el usuario ya está logueado
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            _uiState.value = _uiState.value.copy(isLoggedIn = isLoggedIn)

            if (isLoggedIn) {
                getCurrentUser()
            }
        }
    }

    /**
     * Iniciar sesión
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            authRepository.login(email, password).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            user = result.data.user,
                            // ✅ NUEVAS LÍNEAS - Extraer datos del usuario para fácil acceso
                            userRole = result.data.user.role,
                            username = result.data.user.username,
                            userEmail = result.data.user.email,
                            errorMessage = null,
                            isLoginSuccessful = true
                        )
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Registrar usuario
     */
    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            authRepository.register(username, email, password).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null,
                            isRegisterSuccessful = true
                        )
                        // Log para debugging
                        println("AuthViewModel: Registration successful, setting isRegisterSuccessful = true")
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Obtener usuario actual
     */
    fun getCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            user = result.data,
                            // ✅ NUEVAS LÍNEAS - Actualizar datos del usuario
                            userRole = result.data.role,
                            username = result.data.username,
                            userEmail = result.data.email,
                            isLoggedIn = true
                        )
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            isLoggedIn = false,
                            user = null,
                            // ✅ NUEVAS LÍNEAS - Limpiar datos del usuario
                            userRole = null,
                            username = null,
                            userEmail = null
                        )
                    }
                }
            }
        }
    }

    /**
     * Cerrar sesión
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState() // Reset state
        }
    }

    /**
     * Limpiar mensaje de error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Limpiar éxito de login
     */
    fun clearLoginSuccess() {
        _uiState.value = _uiState.value.copy(isLoginSuccessful = false)
    }

    /**
     * Limpiar éxito de registro
     */
    fun clearRegisterSuccess() {
        println("AuthViewModel: Clearing register success")
        _uiState.value = _uiState.value.copy(isRegisterSuccessful = false)
    }

    /**
     * Verificar si el usuario es admin
     */
    fun isAdmin(): Boolean {
        return _uiState.value.userRole == "ADMIN_STAFF"
    }

    /**
     * Verificar si el usuario es cliente
     */
    fun isClient(): Boolean {
        return _uiState.value.userRole == "CLIENT"
    }

    // ✅ NUEVO MÉTODO - Verificar si es admin staff de forma más clara
    fun isAdminStaff(): Boolean {
        return _uiState.value.userRole == "ADMIN_STAFF"
    }
}