package com.example.restaurant_app.data.repository

import com.example.restaurant_app.data.local.TokenManager
import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.remote.AuthApiService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) {

    /**
     * Registrar un nuevo usuario
     */
    suspend fun register(userCreate: UserCreate): Result<TokenResponse> {
        return try {
            val response = authApiService.register(userCreate)
            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    // Guardar token y información del usuario
                    tokenManager.saveToken(tokenResponse.access_token)
                    tokenManager.saveUserInfo(
                        userId = tokenResponse.user.id,
                        email = tokenResponse.user.email,
                        role = tokenResponse.user.role.name,
                        username = tokenResponse.user.username
                    )
                    Result.success(tokenResponse)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos de registro inválidos"
                    409 -> "El email ya está registrado"
                    422 -> "Datos inválidos: verifique los campos"
                    else -> "Error de registro: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Iniciar sesión
     */
    suspend fun login(userLogin: UserLogin): Result<TokenResponse> {
        return try {
            val response = authApiService.login(userLogin)
            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    // Guardar token y información del usuario
                    tokenManager.saveToken(tokenResponse.access_token)
                    tokenManager.saveUserInfo(
                        userId = tokenResponse.user.id,
                        email = tokenResponse.user.email,
                        role = tokenResponse.user.role.name,
                        username = tokenResponse.user.username
                    )
                    Result.success(tokenResponse)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Email o contraseña incorrectos"
                    400 -> "Datos de login inválidos"
                    422 -> "Formato de email inválido"
                    else -> "Error de login: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtener usuario actual
     */
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = authApiService.getCurrentUser()
            if (response.isSuccessful) {
                response.body()?.let { user ->
                    // Actualizar información local del usuario
                    tokenManager.saveUserInfo(
                        userId = user.id,
                        email = user.email,
                        role = user.role.name,
                        username = user.username
                    )
                    Result.success(user)
                } ?: Result.failure(Exception("No se pudo obtener información del usuario"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    403 -> "Token inválido"
                    else -> "Error al obtener usuario: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Cerrar sesión
     */
    suspend fun logout(): Result<Unit> {
        return try {
            // Intentar logout en el servidor
            val response = authApiService.logout()

            // Limpiar datos locales independientemente del resultado del servidor
            tokenManager.clearAll()

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                // Aunque el servidor falle, ya limpiamos local, así que es éxito
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // Limpiar datos locales aún en caso de error de red
            tokenManager.clearAll()
            Result.success(Unit) // Consideramos éxito porque limpiamos local
        }
    }

    /**
     * Refrescar token (si tu API lo soporta)
     */
    suspend fun refreshToken(): Result<TokenResponse> {
        return try {
            val currentToken = tokenManager.getToken().first()
            if (currentToken.isNullOrEmpty()) {
                return Result.failure(Exception("No hay token disponible"))
            }

            val response = authApiService.refreshToken(RefreshTokenRequest(currentToken))
            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    tokenManager.saveToken(tokenResponse.access_token)
                    // Actualizar información del usuario si viene en la respuesta
                    tokenManager.saveUserInfo(
                        userId = tokenResponse.user.id,
                        email = tokenResponse.user.email,
                        role = tokenResponse.user.role.name,
                        username = tokenResponse.user.username
                    )
                    Result.success(tokenResponse)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Token expirado"
                    403 -> "Token inválido"
                    else -> "Error al refrescar token: ${response.message()}"
                }
                // Si el refresh falla, limpiar tokens
                tokenManager.clearAll()
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // Si hay error de conexión, también limpiar
            tokenManager.clearAll()
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Verificar si hay sesión activa
     */
    suspend fun isLoggedIn(): Boolean {
        return try {
            val token = tokenManager.getToken().first()
            !token.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtener rol del usuario logueado
     */
    suspend fun getUserRole(): String? {
        return try {
            tokenManager.getUserRole().first()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtener ID del usuario logueado
     */
    suspend fun getUserId(): String? {
        return try {
            tokenManager.getUserId().first()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtener email del usuario logueado
     */
    suspend fun getUserEmail(): String? {
        return try {
            tokenManager.getUserEmail().first()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtener username del usuario logueado
     */
    suspend fun getUserUsername(): String? {
        return try {
            tokenManager.getUserUsername().first()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Verificar si el usuario es admin
     */
    suspend fun isAdmin(): Boolean {
        return try {
            val role = getUserRole()
            role == "ADMIN_STAFF"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verificar si el usuario es cliente
     */
    suspend fun isClient(): Boolean {
        return try {
            val role = getUserRole()
            role == "CLIENT"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Validar sesión actual (verificar si el token sigue siendo válido)
     */
    suspend fun validateSession(): Result<Boolean> {
        return try {
            if (!isLoggedIn()) {
                Result.success(false)
            } else {
                // Intentar obtener usuario actual para validar token
                val result = getCurrentUser()
                result.fold(
                    onSuccess = { Result.success(true) },
                    onFailure = {
                        // Si falla, limpiar sesión
                        tokenManager.clearAll()
                        Result.success(false)
                    }
                )
            }
        } catch (e: Exception) {
            tokenManager.clearAll()
            Result.failure(Exception("Error al validar sesión: ${e.message}"))
        }
    }

    /**
     * Forzar logout local (sin llamar al servidor)
     */
    suspend fun forceLogout(): Result<Unit> {
        return try {
            tokenManager.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al limpiar datos locales: ${e.message}"))
        }
    }
}