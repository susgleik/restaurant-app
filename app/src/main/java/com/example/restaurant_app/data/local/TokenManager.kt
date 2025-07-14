package com.example.restaurant_app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
){
    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    private val USER_USERNAME_KEY = stringPreferencesKey("user_username")

    // Guardar Token de acceso
    suspend fun saveToken(accessToken: String){
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
        }
    }

    // Guardar token  de refresh
    suspend fun saveRefreshToken(refreshToken: String){
        context.dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    // Guardar información completa del usuario
    suspend fun saveUserInfo(userId: String, email: String, role: String, username: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_ROLE_KEY] = role
            preferences[USER_USERNAME_KEY] = username
        }
    }

    // Obtener token de acceso
    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }

    // Obtener token de refresh
    fun getRefreshToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }
    }

    // Obtener ID del usuario
    fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    // Obtener email del usuario
    fun getUserEmail(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_EMAIL_KEY]
        }
    }

    // Obtener rol del usuario
    fun getUserRole(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ROLE_KEY]
        }
    }

    // Obtener username del usuario
    fun getUserUsername(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_USERNAME_KEY]
        }
    }

    // Limpiar solo tokens (mantener info del usuario)
    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }

    // Limpiar todo (logout completo)
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Para mantener compatibilidad (alias de clearAll)
    suspend fun clearToken() {
        clearAll()
    }
}