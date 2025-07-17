package com.example.restaurant_app.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.restaurant_app.presentation.screens.auth.LoginScreen
import com.example.restaurant_app.presentation.screens.auth.RegisterScreen
import com.example.restaurant_app.presentation.viewmodels.AuthViewModel

fun NavGraphBuilder.authNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    navigation(
        startDestination = "login",
        route = "auth"
    ) {
        composable("login") {
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
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}