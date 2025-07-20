package com.example.restaurant_app.presentation.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.restaurant_app.presentation.screens.cart.CartScreen
import com.example.restaurant_app.presentation.screens.menu.MenuItemDetailScreen
import com.example.restaurant_app.presentation.screens.menu.MenuScreen
import com.example.restaurant_app.presentation.screens.orders.OrdersScreen
import com.example.restaurant_app.presentation.screens.orders.OrderStatusChip
import com.example.restaurant_app.presentation.screens.profile.ProfileScreen
import com.example.restaurant_app.presentation.viewmodels.CartViewModel
import java.text.SimpleDateFormat
import java.util.*

// Definición de las rutas de navegación
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Menu : Screen("menu", "Menú", Icons.Default.Restaurant)
    object Cart : Screen("cart", "Carrito", Icons.Default.ShoppingCart)
    object Orders : Screen("orders", "Pedidos", Icons.Default.Receipt)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val cartSummary by cartViewModel.cartSummary.collectAsStateWithLifecycle()

    // Lista de pantallas para la navegación inferior
    val bottomNavScreens = listOf(
        Screen.Menu,
        Screen.Cart,
        Screen.Orders,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    // Mostrar badge en el carrito si hay items
                                    if (screen == Screen.Cart && cartSummary != null && !cartSummary!!.isEmpty) {
                                        Badge {
                                            Text(cartSummary!!.itemCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            }
                        },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination to avoid building up a large stack
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Menu.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Menu.route) {
                MenuScreen(
                    onMenuItemClick = { menuItem ->
                        navController.navigate("menu_item_detail/${menuItem.id}")
                    },
                    onAddToCart = { menuItem ->
                        cartViewModel.addToCart(menuItem.id, 1)
                    }
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    onNavigateToMenu = {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToOrders = {
                        navController.navigate(Screen.Orders.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Orders.route) {
                OrdersScreen(
                    onOrderClick = { orderId ->
                        navController.navigate("order_detail/$orderId")
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = onLogout
                )
            }

            // Pantalla de detalle del item del menú
            composable("menu_item_detail/{itemId}") { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                MenuItemDetailScreen(
                    menuItemId = itemId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onAddToCart = { itemId, quantity ->
                        cartViewModel.addToCart(itemId, quantity)
                        navController.popBackStack()
                    }
                )
            }

            // Pantalla de detalle del pedido
            composable("order_detail/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderDetailScreen(
                    orderId = orderId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: com.example.restaurant_app.presentation.viewmodels.OrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.loadOrderDetails(orderId)
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Detalle del Pedido") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadOrderDetails(orderId) }) {
                        Text("Reintentar")
                    }
                }
            }

            uiState.selectedOrder != null -> {
                OrderDetailContent(
                    order = uiState.selectedOrder!!,
                    onCancelOrder = { viewModel.cancelOrder(orderId) },
                    isCancelling = uiState.isCancelling
                )
            }
        }
    }
}

@Composable
private fun OrderDetailContent(
    order: com.example.restaurant_app.data.models.Order,
    onCancelOrder: () -> Unit,
    isCancelling: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header del pedido
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pedido #${order.id.takeLast(8)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OrderStatusChip(status = order.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Fecha: ${formatDate(order.created_at)}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (order.notes?.isNotBlank() == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Notas: ${order.notes}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Items del pedido
        items(order.items) { item ->
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.menu_item_name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cantidad: ${item.quantity}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Precio unitario: ${item.unit_price}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        item.special_instructions?.let { instructions ->
                            if (instructions.isNotBlank()) {
                                Text(
                                    text = "Instrucciones: $instructions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        text = item.subtotal,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            // Total del pedido
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total del pedido",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.total,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Botón de cancelar si es posible
        if (order.canBeCancelled) {
            item {
                Button(
                    onClick = onCancelOrder,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCancelling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Cancelar Pedido")
                }
            }
        }
    }
}

// Función auxiliar para formatear fechas
private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: java.util.Date())
    } catch (e: Exception) {
        dateString
    }
}