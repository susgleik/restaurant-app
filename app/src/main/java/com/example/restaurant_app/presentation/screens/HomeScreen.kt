package com.example.restaurant_app.presentation.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.restaurant_app.data.models.MenuItem
//import com.example.restaurant_app.presentation.screens.cart.CartScreen
import com.example.restaurant_app.presentation.screens.menu.MenuItemDetailScreen
import com.example.restaurant_app.presentation.screens.menu.MenuScreen
//import com.example.restaurant_app.presentation.screens.orders.OrdersScreen
import com.example.restaurant_app.presentation.screens.profile.ProfileScreen

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
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
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
                        // TODO: Implementar lógica para agregar al carrito
                        addToCart(menuItem, 1)
                    }
                )
            }

            /*
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
                    }
                )
            }


            composable(Screen.Orders.route) {
                OrdersScreen()
            }

             */
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
                        // TODO: Implementar lógica para agregar al carrito con cantidad
                        addToCartWithQuantity(itemId, quantity)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

// Función temporal para simular agregar al carrito
private fun addToCart(menuItem: MenuItem, quantity: Int) {
    // TODO: Implementar con CartViewModel
    println("Agregando al carrito: ${menuItem.name} x$quantity")
}

// Función temporal para agregar al carrito con cantidad específica
private fun addToCartWithQuantity(itemId: String, quantity: Int) {
    // TODO: Implementar con CartViewModel
    println("Agregando al carrito: Item $itemId x$quantity")
}