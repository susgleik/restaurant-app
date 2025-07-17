package com.example.restaurant_app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_USERNAME_KEY = stringPreferencesKey("user_username")
    }

    // Métodos originales
    fun getAccessToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }

    fun getRefreshToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }
    }

    fun getUserId(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    fun getUserRole(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_ROLE_KEY]
        }
    }

    fun getUserEmail(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_EMAIL_KEY]
        }
    }

    fun getUserUsername(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_USERNAME_KEY]
        }
    }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String? = null,
        userId: String? = null,
        userRole: String? = null
    ) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = it }
            userId?.let { preferences[USER_ID_KEY] = it }
            userRole?.let { preferences[USER_ROLE_KEY] = it }
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun updateAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
        }
    }

    // Métodos adicionales que espera AuthRepository
    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }

    suspend fun saveUserInfo(
        userId: String? = null,
        email: String? = null,
        username: String? = null,
        role: String? = null
    ) {
        dataStore.edit { preferences ->
            userId?.let { preferences[USER_ID_KEY] = it }
            email?.let { preferences[USER_EMAIL_KEY] = it }
            username?.let { preferences[USER_USERNAME_KEY] = it }
            role?.let { preferences[USER_ROLE_KEY] = it }
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    fun getToken(): Flow<String?> {
        return getAccessToken()
    }
}