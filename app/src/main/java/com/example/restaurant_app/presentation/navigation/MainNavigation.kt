// presentation/navigation/MainNavigation.kt - Actualizada con detección de roles
package com.example.restaurant_app.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.restaurant_app.presentation.screens.HomeScreen
import com.example.restaurant_app.presentation.screens.admin.AdminMainScreen
import com.example.restaurant_app.presentation.viewmodels.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.mainNavigation(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    navigation(
        startDestination = "main_entry", // ✅ CAMBIO: nuevo destino inicial
        route = "main"
    ) {
        composable("main_entry") { // ✅ NUEVO: Punto de entrada que decide qué mostrar
            // Obtener el AuthViewModel para verificar el rol
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            // ✅ LÓGICA DE DECISIÓN: Mostrar pantalla según el rol del usuario
            when (authUiState.userRole) {
                "ADMIN_STAFF" -> {
                    // 🔧 Vista de administración para staff
                    AdminMainScreen(
                        onLogout = {
                            authViewModel.logout()
                            onLogout()
                        }
                    )
                }
                "CLIENT" -> {
                    // 👤 Vista normal para clientes
                    HomeScreen(
                        onLogout = {
                            authViewModel.logout()
                            onLogout()
                        }
                    )
                }
                null -> {
                    // ⏳ Si el rol aún no se ha cargado, mostrar vista de cliente por defecto
                    // Esto puede pasar durante la carga inicial
                    HomeScreen(
                        onLogout = {
                            authViewModel.logout()
                            onLogout()
                        }
                    )
                }
                else -> {
                    // 🔄 Fallback para cualquier otro rol no esperado
                    HomeScreen(
                        onLogout = {
                            authViewModel.logout()
                            onLogout()
                        }
                    )
                }
            }
        }

        // ✅ MANTENER: Ruta original por compatibilidad (opcional)
        composable("home") {
            HomeScreen(
                onLogout = onLogout
            )
        }
    }
}