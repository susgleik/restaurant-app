// MainNavigation.kt - Versión temporal simplificada
package com.example.restaurant_app.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.restaurant_app.presentation.viewmodels.AuthViewModel

fun NavGraphBuilder.mainNavigation(navController: NavHostController) {
    navigation(
        startDestination = "main_content",
        route = "main"
    ) {
        composable("main_content") {
            // Pantalla temporal de bienvenida
            MainTemporaryScreen(navController)
        }
    }
}

@Composable
fun MainTemporaryScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Bienvenido!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Has iniciado sesión correctamente",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Aquí irán las pantallas principales:",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            Text("• Menú del restaurante")
            Text("• Carrito de compras")
            Text("• Historial de pedidos")
            Text("• Perfil de usuario")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                authViewModel.logout()
                navController.navigate("auth") {
                    popUpTo("main") { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cerrar Sesión")
        }
    }
}