// presentation/screens/admin/AdminMainScreen.kt - Versión corregida para edge-to-edge
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

    // Definir las pestañas para admin con categorías y productos separados
    val adminTabs = listOf(
        AdminTab.CATEGORIES,
        AdminTab.MENU,
        AdminTab.ORDERS,
        AdminTab.PROFILE
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { adminTabs.size }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = when (adminTabs[pagerState.currentPage]) {
                        AdminTab.CATEGORIES -> "Gestión de Categorías"
                        AdminTab.MENU -> "Gestión de Productos"
                        AdminTab.ORDERS -> "Gestión de Pedidos"
                        AdminTab.PROFILE -> "Perfil del Staff"
                    }
                )
            },
            actions = {
                // Mostrar info del staff
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = authUiState.username ?: "Staff",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ADMIN STAFF",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
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
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
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
                AdminTab.CATEGORIES -> {
                    AdminCategoriesScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AdminTab.MENU -> {
                    AdminMenuScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AdminTab.ORDERS -> {
                    AdminOrdersScreen(
                        modifier = Modifier.fillMaxSize()
                    )
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

// Enum actualizado para las pestañas principales del admin
enum class AdminTab(
    val title: String,
    val icon: ImageVector
) {
    CATEGORIES("Categorías", Icons.Default.Category),
    MENU("Productos", Icons.Default.RestaurantMenu),
    ORDERS("Pedidos", Icons.Default.Receipt),
    PROFILE("Perfil", Icons.Default.Person)
}