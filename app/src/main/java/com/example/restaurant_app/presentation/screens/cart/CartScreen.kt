// presentation/screens/cart/CartScreen.kt
package com.example.restaurant_app.presentation.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.restaurant_app.data.models.CartItem
import com.example.restaurant_app.presentation.viewmodels.CartViewModel
import com.example.restaurant_app.presentation.viewmodels.OrderViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateToMenu: () -> Unit,
    onNavigateToOrders: () -> Unit,
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel = hiltViewModel(),
    orderViewModel: OrderViewModel = hiltViewModel()
) {
    val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val orderUiState by orderViewModel.uiState.collectAsStateWithLifecycle()
    var showOrderDialog by remember { mutableStateOf(false) }
    var orderNotes by remember { mutableStateOf("") }

    // Manejar efectos secundarios
    LaunchedEffect(cartUiState.successMessage) {
        cartUiState.successMessage?.let {
            // Mostrar snackbar o toast si es necesario
            cartViewModel.clearMessages()
        }
    }

    LaunchedEffect(orderUiState.successMessage) {
        orderUiState.successMessage?.let {
            showOrderDialog = false
            orderNotes = ""
            onNavigateToOrders()
            orderViewModel.clearMessages()
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Mi Carrito",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                if (!cartViewModel.isCartEmpty()) {
                    IconButton(
                        onClick = { cartViewModel.clearCart() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Vaciar carrito"
                        )
                    }
                }
            }
        )

        when {
            cartUiState.isLoading -> {
                LoadingCartContent()
            }

            cartUiState.errorMessage != null -> {
                ErrorCartContent(
                    message = cartUiState.errorMessage!!,
                    onRetry = { cartViewModel.loadCart() }
                )
            }

            cartViewModel.isCartEmpty() -> {
                EmptyCartContent(onNavigateToMenu = onNavigateToMenu)
            }

            else -> {
                CartContent(
                    cart = cartUiState.cart!!,
                    isUpdating = cartUiState.isUpdating,
                    onUpdateQuantity = { cartItemId, quantity ->
                        cartViewModel.updateItemQuantity(cartItemId, quantity)
                    },
                    onRemoveItem = { cartItemId ->
                        cartViewModel.removeItem(cartItemId)
                    },
                    onCreateOrder = { showOrderDialog = true },
                    onNavigateToMenu = onNavigateToMenu,
                    isCreatingOrder = orderUiState.isCreatingOrder
                )
            }
        }
    }

    // Dialog para confirmar pedido
    if (showOrderDialog) {
        OrderConfirmationDialog(
            cartTotal = cartViewModel.getCartTotal(),
            notes = orderNotes,
            onNotesChange = { orderNotes = it },
            onConfirm = {
                orderViewModel.createOrderFromCart(orderNotes.takeIf { it.isNotBlank() })
            },
            onDismiss = { showOrderDialog = false },
            isLoading = orderUiState.isCreatingOrder
        )
    }

    // Mostrar errores
    cartUiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Mostrar error en snackbar
        }
    }

    orderUiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Mostrar error en snackbar
        }
    }
}

@Composable
private fun CartContent(
    cart: com.example.restaurant_app.data.models.CartList,
    isUpdating: Boolean,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onCreateOrder: () -> Unit,
    onNavigateToMenu: () -> Unit,
    isCreatingOrder: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Lista de items
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(
                items = cart.items,
                key = { it.id }
            ) { cartItem ->
                CartItemCard(
                    cartItem = cartItem,
                    onUpdateQuantity = { quantity ->
                        onUpdateQuantity(cartItem.id, quantity)
                    },
                    onRemove = { onRemoveItem(cartItem.id) },
                    isUpdating = isUpdating
                )
            }
        }

        // Resumen y botón de pedido
        CartSummarySection(
            cart = cart,
            onCreateOrder = onCreateOrder,
            onContinueShopping = onNavigateToMenu,
            isCreatingOrder = isCreatingOrder
        )
    }
}

@Composable
private fun CartItemCard(
    cartItem: CartItem,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit,
    isUpdating: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder para imagen (puedes agregar imagen si tienes la URL)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cartItem.menu_item_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatPrice(cartItem.menu_item_price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Subtotal: ${formatPrice(cartItem.subtotal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Controles de cantidad
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            if (cartItem.quantity > 1) {
                                onUpdateQuantity(cartItem.quantity - 1)
                            } else {
                                onRemove()
                            }
                        },
                        enabled = !isUpdating,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (cartItem.quantity > 1) Icons.Default.Remove else Icons.Default.Delete,
                            contentDescription = if (cartItem.quantity > 1) "Disminuir" else "Eliminar",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = cartItem.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = 24.dp)
                    )

                    FilledTonalIconButton(
                        onClick = { onUpdateQuantity(cartItem.quantity + 1) },
                        enabled = !isUpdating,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartSummarySection(
    cart: com.example.restaurant_app.data.models.CartList,
    onCreateOrder: () -> Unit,
    onContinueShopping: () -> Unit,
    isCreatingOrder: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Resumen de precios
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:")
                        Text(cart.subtotal)
                    }

                    if (cart.estimated_tax.toDoubleOrNull() != 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Impuestos:")
                            Text(cart.estimated_tax)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            cart.estimated_total,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onContinueShopping,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Seguir comprando")
                }

                Button(
                    onClick = onCreateOrder,
                    modifier = Modifier.weight(1f),
                    enabled = !isCreatingOrder
                ) {
                    if (isCreatingOrder) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Hacer pedido")
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderConfirmationDialog(
    cartTotal: Double,
    notes: String,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Confirmar Pedido") },
        text = {
            Column {
                Text(
                    text = "Total: ${formatPrice(cartTotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notas (opcional)") },
                    placeholder = { Text("Instrucciones especiales...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirmar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun EmptyCartContent(
    onNavigateToMenu: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tu carrito está vacío",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Agrega algunos productos deliciosos",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateToMenu,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver Menú")
        }
    }
}

@Composable
private fun LoadingCartContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando carrito...")
        }
    }
}

@Composable
private fun ErrorCartContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Error al cargar carrito",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

// Función auxiliar para formatear precios
private fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PA"))
    return formatter.format(price)
}