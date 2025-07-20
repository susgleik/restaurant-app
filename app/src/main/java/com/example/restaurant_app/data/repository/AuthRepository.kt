package com.example.restaurant_app.data.repository

import android.annotation.SuppressLint
import com.example.restaurant_app.data.local.TokenManager
import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.remote.AuthApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

sealed class AuthResult<T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error<T>(val message: String) : AuthResult<T>()
    data class Loading<T>(val message: String = "Cargando...") : AuthResult<T>()
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ValidationErrorDetail(
    val type: String,
    val loc: List<String>,
    val msg: String,
    val input: String? = null
)
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ValidationErrorResponse(
    val detail: List<ValidationErrorDetail>
)

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
                    422 -> {
                        // Intentar parsear errores de validación
                        try {
                            val errorBody = response.errorBody()?.string()
                            if (!errorBody.isNullOrBlank()) {
                                val json = Json { ignoreUnknownKeys = true }
                                val validationError = json.decodeFromString<ValidationErrorResponse>(errorBody)
                                val messages = validationError.detail.map { it.msg }
                                "Error de validación: ${messages.joinToString(", ")}"
                            } else {
                                "Datos inválidos"
                            }
                        } catch (e: Exception) {
                            "Datos inválidos"
                        }
                    }
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
                    422 -> {
                        // Intentar parsear errores de validación específicos
                        try {
                            val errorBody = response.errorBody()?.string()
                            if (!errorBody.isNullOrBlank()) {
                                val json = Json { ignoreUnknownKeys = true }
                                val validationError = json.decodeFromString<ValidationErrorResponse>(errorBody)

                                val messages = validationError.detail.map { detail ->
                                    when {
                                        detail.loc.contains("username") -> {
                                            if (detail.msg.contains("solo puede contener")) {
                                                "El nombre de usuario solo puede contener letras, números, guiones y guiones bajos"
                                            } else {
                                                "Error en el nombre de usuario: ${detail.msg}"
                                            }
                                        }
                                        detail.loc.contains("email") -> {
                                            if (detail.msg.contains("email address")) {
                                                "El formato del email no es válido"
                                            } else {
                                                "Error en el email: ${detail.msg}"
                                            }
                                        }
                                        detail.loc.contains("password") -> {
                                            "Error en la contraseña: ${detail.msg}"
                                        }
                                        else -> detail.msg
                                    }
                                }
                                messages.joinToString("\n")
                            } else {
                                "Error de validación en los datos"
                            }
                        } catch (e: Exception) {
                            "Error de validación en los datos"
                        }
                    }
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