package com.example.restaurant_app.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.restaurant_app.presentation.viewmodels.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RestaurantNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authUiState by authViewModel.uiState.collectAsState()
    var showSessionExpiredDialog by remember { mutableStateOf(false) }

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

    // Detectar sesión expirada (401 desde el interceptor)
    LaunchedEffect(Unit) {
        authViewModel.sessionExpiredEvents.collect {
            showSessionExpiredDialog = true
        }
    }

    // Diálogo de sesión expirada
    if (showSessionExpiredDialog) {
        AlertDialog(
            onDismissRequest = { /* No se puede cerrar sin confirmar */ },
            title = { Text("Sesión expirada") },
            text = { Text("Tu sesión ha expirado. Por favor, inicia sesión nuevamente.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSessionExpiredDialog = false
                        authViewModel.logout()
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Iniciar sesión")
                }
            }
        )
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