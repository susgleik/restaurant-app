// RestaurantNavigation.kt - Versión simplificada solo para auth
package com.example.restaurant_app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun RestaurantNavigation(
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "main" else "auth",
        modifier = modifier
    ) {
        // Navegación de autenticación
        authNavigation(navController)

        // Pantalla temporal para cuando esté logueado
        mainNavigation(navController)
    }
}