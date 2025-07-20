package com.example.restaurant_app.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.restaurant_app.presentation.screens.auth.LoginScreen
import com.example.restaurant_app.presentation.screens.auth.RegisterScreen
import com.example.restaurant_app.presentation.viewmodels.AuthViewModel
import androidx.compose.runtime.getValue

fun NavGraphBuilder.authNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    navigation(
        startDestination = "login",
        route = "auth"
    ) {
        composable("login") {
            val authUiState by authViewModel.uiState.collectAsState()

            // Manejar navegacion despues de login exitoso
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

            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    // La navegación se maneja automáticamente en RestaurantNavigation
                    // mediante LaunchedEffect que observa authUiState.isLoginSuccessful
                }
            )
        }

        composable("register") {
            val authUiState by authViewModel.uiState.collectAsState()

            // Manejar navegación después de registro exitoso
            LaunchedEffect(authUiState.isRegisterSuccessful) {
                if (authUiState.isRegisterSuccessful) {
                    // Ir a login después del registro exitoso
                    navController.navigate("login") {
                        popUpTo("register") {
                            inclusive = true
                        }
                    }
                    authViewModel.clearRegisterSuccess()
                }
            }


            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") {
                            inclusive = true
                        }
                    }
                },
                onRegisterSuccess = {
                    // La navegación se maneja en LaunchedEffect arriba
                }
            )
        }
    }
}