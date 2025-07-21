// data/DataModule.kt - Versión completa con repositorios de admin
package com.example.restaurant_app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.restaurant_app.data.local.TokenManager
import com.example.restaurant_app.data.remote.*
import com.example.restaurant_app.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Extension property para DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    // ═══════════════════════════════════════════════════════════════
    // LOCAL DATA (DataStore & TokenManager)
    // ═══════════════════════════════════════════════════════════════

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideTokenManager(dataStore: DataStore<Preferences>): TokenManager {
        return TokenManager(dataStore)
    }

    // ═══════════════════════════════════════════════════════════════
    // CLIENT REPOSITORIES (para usuarios normales)
    // ═══════════════════════════════════════════════════════════════

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApiService: AuthApiService,
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepository(authApiService, tokenManager)
    }

    @Provides
    @Singleton
    fun provideMenuRepository(
        menuApiService: MenuApiService
    ): MenuRepository {
        return MenuRepository(menuApiService)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryApiService: CategoryApiService
    ): CategoryRepository {
        return CategoryRepository(categoryApiService)
    }

    @Provides
    @Singleton
    fun provideCartRepository(
        cartApiService: CartApiService
    ): CartRepository {
        return CartRepository(cartApiService)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(
        orderApiService: OrderApiService
    ): OrderRepository {
        return OrderRepository(orderApiService)
    }

    // ═══════════════════════════════════════════════════════════════
    // ADMIN REPOSITORIES (para staff/administradores)
    // ═══════════════════════════════════════════════════════════════

    @Provides
    @Singleton
    fun provideAdminOrderRepository(
        orderApiService: OrderApiService // Usando OrderApiService existente temporalmente
    ): AdminOrderRepository {
        return AdminOrderRepository(orderApiService)
    }
}