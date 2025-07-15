// presentation/viewmodels/AuthViewModel.kt
package com.example.restaurant_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel(){
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init{
        checkAuthStatus()
    }

    /**
     * Iniciar sesión
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // Validaciones básicas
            if (email.isBlank() || password.isBlank()) {
                _authState.value = AuthState.Error("Por favor completa todos los campos")
                return@launch
            }

            if (!isValidEmail(email)) {
                _authState.value = AuthState.Error("Formato de email inválido")
                return@launch
            }

            val result = authRepository.login(UserLogin(email, password))
            result.fold(
                onSuccess = { tokenResponse ->
                    _currentUser.value = tokenResponse.user
                    _isLoggedIn.value = true
                    _authState.value = AuthState.Success("Inicio de sesión exitoso")
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Error al iniciar sesión")
                }
            )
        }
    }

    /**
     * Registrar nuevo usuario
     */
    fun register(username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // Validaciones
            val validation = validateRegistrationData(username, email, password, confirmPassword)
            if (validation != null) {
                _authState.value = AuthState.Error(validation)
                return@launch
            }

            val result = authRepository.register(
                UserCreate(username, email, password, UserRole.CLIENT)
            )
            result.fold(
                onSuccess = { tokenResponse ->
                    _currentUser.value = tokenResponse.user
                    _isLoggedIn.value = true
                    _authState.value = AuthState.Success("Registro exitoso")
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Error al registrarse")
                }
            )
        }
    }

    /**
     * Cerrar sesión
     */
    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authRepository.logout()
            result.fold(
                onSuccess = {
                    _currentUser.value = null
                    _isLoggedIn.value = false
                    _authState.value = AuthState.Success("Sesión cerrada")
                },
                onFailure = { error ->
                    // Aún si falla el logout en servidor, limpiamos local
                    _currentUser.value = null
                    _isLoggedIn.value = false
                    _authState.value = AuthState.Success("Sesión cerrada")
                }
            )
        }
    }

    /**
     * Verificar estado de autenticación
     */
    private fun checkAuthStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (authRepository.isLoggedIn()) {
                val result = authRepository.getCurrentUser()
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _isLoggedIn.value = true
                        _authState.value = AuthState.Idle
                    },
                    onFailure = {
                        // Si falla obtener usuario, limpiar sesión
                        authRepository.forceLogout()
                        _currentUser.value = null
                        _isLoggedIn.value = false
                        _authState.value = AuthState.Idle
                    }
                )
            } else {
                _isLoggedIn.value = false
                _authState.value = AuthState.Idle
            }
        }
    }

    /**
     * Validar sesión actual
     */
    fun validateSession() {
        viewModelScope.launch {
            val result = authRepository.validateSession()
            result.fold(
                onSuccess = { isValid ->
                    if (!isValid) {
                        _currentUser.value = null
                        _isLoggedIn.value = false
                        _authState.value = AuthState.Error("Sesión expirada")
                    }
                },
                onFailure = { error ->
                    _currentUser.value = null
                    _isLoggedIn.value = false
                    _authState.value = AuthState.Error(error.message ?: "Error de sesión")
                }
            )
        }
    }

    /**
     * Limpiar estado de error
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    /**
     * Limpiar estado de éxito
     */
    fun clearSuccess() {
        if (_authState.value is AuthState.Success) {
            _authState.value = AuthState.Idle
        }
    }

    /**
     * Verificar si el usuario es administrador
     */
    fun isAdmin(): Boolean {
        return _currentUser.value?.role == UserRole.ADMIN_STAFF
    }

    /**
     * Obtener información del usuario actual
     */
    fun getCurrentUserInfo(): User? {
        return _currentUser.value
    }

    // =================== FUNCIONES DE VALIDACIÓN ===================

    private fun validateRegistrationData(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        return when {
            username.isBlank() -> "El nombre de usuario es requerido"
            username.length < 3 -> "El nombre de usuario debe tener al menos 3 caracteres"
            username.length > 50 -> "El nombre de usuario no puede tener más de 50 caracteres"
            email.isBlank() -> "El email es requerido"
            !isValidEmail(email) -> "Formato de email inválido"
            password.isBlank() -> "La contraseña es requerida"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            password != confirmPassword -> "Las contraseñas no coinciden"
            !isValidPassword(password) -> "La contraseña debe contener al menos una letra y un número"
            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
}

/**
 * Estados de autenticación
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
