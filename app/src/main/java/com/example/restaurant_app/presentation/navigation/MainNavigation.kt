package com.example.restaurant_app.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.restaurant_app.presentation.screens.HomeScreen
import com.example.restaurant_app.presentation.viewmodels.AuthViewModel

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
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()

            // Verificar autenticación cuando se entra a main
            LaunchedEffect(Unit) {
                authViewModel.getCurrentUser()
            }

            // Si no está autenticado, regresar a auth
            LaunchedEffect(authUiState.isLoggedIn) {
                if (!authUiState.isLoggedIn && authUiState.errorMessage != null) {
                    onLogout()
                }
            }

            HomeScreen(
                onLogout = {
                    authViewModel.logout()
                    onLogout()
                }
            )
        }
    }
}