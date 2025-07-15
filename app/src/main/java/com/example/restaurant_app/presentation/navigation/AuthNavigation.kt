package com.example.restaurant_app.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.restaurant_app.presentation.screens.auth.LoginScreen
import com.example.restaurant_app.presentation.screens.auth.RegisterScreen

fun NavGraphBuilder.authNavigation(navController: NavHostController) {
    navigation(
        startDestination = "login",
        route = "auth"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
    }
}