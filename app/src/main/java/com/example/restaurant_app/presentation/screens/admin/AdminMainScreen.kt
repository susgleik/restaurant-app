// presentation/screens/admin/AdminMainScreen.kt - Versión básica para empezar
package com.example.restaurant_app.presentation.screens.admin

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.restaurant_app.presentation.screens.profile.ProfileScreen
import com.example.restaurant_app.presentation.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Definir las pestañas para admin
    val adminTabs = listOf(
        AdminTab.MENU,
        AdminTab.ORDERS,
        AdminTab.PROFILE
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { adminTabs.size }
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = when (adminTabs[pagerState.currentPage]) {
                        AdminTab.MENU -> "Gestión del Menú"
                        AdminTab.ORDERS -> "Gestión de Pedidos"
                        AdminTab.PROFILE -> "Perfil del Staff"
                    }
                )
            },
            actions = {
                // Mostrar info del staff
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = authUiState.username ?: "Staff",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "ADMIN STAFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            }
        )

        // Tabs
        TabRow(
            selectedTabIndex = pagerState.currentPage
        ) {
            adminTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(tab.title) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title
                        )
                    }
                )
            }
        }

        // Contenido de las pestañas
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (adminTabs[page]) {
                AdminTab.MENU -> {
                    // TODO: Implementar gestión del menú
                    AdminMenuPlaceholder()
                }
                AdminTab.ORDERS -> {
                    // TODO: Implementar gestión de pedidos
                    AdminOrdersPlaceholder()
                }
                AdminTab.PROFILE -> {
                    ProfileScreen(
                        onLogout = onLogout,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// Placeholder temporal para el menú
@Composable
private fun AdminMenuPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.RestaurantMenu,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Gestión del Menú",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aquí podrás gestionar productos y categorías del menú",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "🎉 ¡Funcionalidad de admin activada!",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// Placeholder temporal para pedidos
@Composable
private fun AdminOrdersPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Gestión de Pedidos",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aquí podrás gestionar todos los pedidos del restaurante",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Enum para las pestañas principales del admin
enum class AdminTab(
    val title: String,
    val icon: ImageVector
) {
    MENU("Menú", Icons.Default.RestaurantMenu),
    ORDERS("Pedidos", Icons.Default.Receipt),
    PROFILE("Perfil", Icons.Default.Person)
}