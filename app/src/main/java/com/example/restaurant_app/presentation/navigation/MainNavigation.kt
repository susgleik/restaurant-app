// presentation/navigation/MainNavigation.kt
package com.example.restaurant_app.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.restaurant_app.presentation.screens.HomeScreen

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.mainNavigation(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    navigation(
        startDestination = "home",
        route = "main"
    ) {
        composable("home") {
            HomeScreen(
                onLogout = onLogout
            )
        }
    }
}