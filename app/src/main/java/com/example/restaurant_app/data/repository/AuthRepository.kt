package com.example.restaurant_app.data.repository

import com.example.restaurant_app.data.local.TokenManager
import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.remote.AuthApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error<T>(val message: String) : AuthResult<T>()
    data class Loading<T>(val message: String = "Cargando...") : AuthResult<T>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) {

    fun login(email: String, password: String): Flow<AuthResult<LoginResponse>> = flow {
        try {
            emit(AuthResult.Loading("Iniciando sesión..."))

            val loginRequest = LoginRequest(email, password)
            val response = authApiService.login(loginRequest)

            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // Guardar token y información del usuario
                    tokenManager.saveTokens(
                        accessToken = loginResponse.access_token,
                        refreshToken = null, // Si tu API no proporciona refresh token
                        userId = loginResponse.user.id,
                        userRole = loginResponse.user.role
                    )

                    // Guardar información adicional del usuario
                    tokenManager.saveUserInfo(
                        userId = loginResponse.user.id,
                        email = loginResponse.user.email,
                        username = loginResponse.user.username,
                        role = loginResponse.user.role
                    )

                    emit(AuthResult.Success(loginResponse))
                } ?: emit(AuthResult.Error("Respuesta vacía del servidor"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Credenciales incorrectas"
                    404 -> "Usuario no encontrado"
                    else -> "Error al iniciar sesión: ${response.message()}"
                }
                emit(AuthResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun register(username: String, email: String, password: String): Flow<AuthResult<User>> = flow {
        try {
            emit(AuthResult.Loading("Registrando usuario..."))

            val registerRequest = RegisterRequest(username, email, password)
            val response = authApiService.register(registerRequest)

            if (response.isSuccessful) {
                response.body()?.let { user ->
                    emit(AuthResult.Success(user))
                } ?: emit(AuthResult.Error("Respuesta vacía del servidor"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos inválidos"
                    409 -> "El email ya está registrado"
                    else -> "Error al registrar: ${response.message()}"
                }
                emit(AuthResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error("Error de conexión: ${e.message}"))
        }
    }

    fun getCurrentUser(): Flow<AuthResult<User>> = flow {
        try {
            emit(AuthResult.Loading("Obteniendo información del usuario..."))

            val response = authApiService.getCurrentUser()

            if (response.isSuccessful) {
                response.body()?.let { user ->
                    emit(AuthResult.Success(user))
                } ?: emit(AuthResult.Error("Respuesta vacía del servidor"))
            } else {
                when (response.code()) {
                    401, 403 -> {
                        // Token inválido o expirado, limpiar datos
                        tokenManager.clearTokens()
                        emit(AuthResult.Error("Sesión expirada"))
                    }
                    else -> emit(AuthResult.Error("Error al obtener usuario: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            emit(AuthResult.Error("Error de conexión: ${e.message}"))
        }
    }

    suspend fun logout() {
        try {
            tokenManager.clearTokens()
        } catch (e: Exception) {
            // Log error but don't throw
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return try {
            val token = tokenManager.getAccessToken().first()
            !token.isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }

    fun getAccessToken(): Flow<String?> = tokenManager.getAccessToken()

    fun getUserId(): Flow<String?> = tokenManager.getUserId()

    fun getUserRole(): Flow<String?> = tokenManager.getUserRole()
}