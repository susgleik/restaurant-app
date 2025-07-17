package com.example.restaurant_app.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.restaurant_app.presentation.viewmodels.AuthViewModel

@Composable
fun RestaurantNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authUiState by authViewModel.uiState.collectAsState()

    // Determinar la ruta inicial basada en el estado de autenticación
    val startDestination = if (authUiState.isLoggedIn) "main" else "auth"

    // Manejar navegación automática después de login exitoso
    LaunchedEffect(authUiState.isLoginSuccessful) {
        if (authUiState.isLoginSuccessful) {
            navController.navigate("main") {
                popUpTo("auth") {
                    inclusive = true
                }
            }
            authViewModel.clearLoginSuccess()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Navegación de autenticación
        authNavigation(
            navController = navController,
            authViewModel = authViewModel
        )

        // Navegación principal (después del login)
        mainNavigation(
            navController = navController,
            onLogout = {
                navController.navigate("auth") {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            }
        )
    }
}