// presentation/screens/orders/OrdersScreen.kt
package com.example.restaurant_app.presentation.screens.orders

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.restaurant_app.data.models.Order
import com.example.restaurant_app.data.models.OrderStatus
import com.example.restaurant_app.presentation.viewmodels.OrderViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onOrderClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCancelDialog by remember { mutableStateOf<Order?>(null) }

    // Tabs para separar pedidos activos e historial
    val tabs = listOf("Activos", "Historial")

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            // Mostrar mensaje de éxito
            viewModel.clearMessages()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Mis Pedidos",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { viewModel.refreshOrders() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar"
                    )
                }
            }
        )

        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(title)
                            if (index == 0 && uiState.activeOrders.isNotEmpty()) {
                                Badge {
                                    Text(uiState.activeOrders.size.toString())
                                }
                            }
                        }
                    }
                )
            }
        }

        // Contenido según la tab seleccionada
        when {
            uiState.isLoading -> {
                LoadingOrdersContent()
            }

            uiState.errorMessage != null -> {
                ErrorOrdersContent(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.loadOrders() }
                )
            }

            selectedTab == 0 -> {
                // Pedidos activos
                if (uiState.activeOrders.isEmpty()) {
                    EmptyOrdersContent(
                        title = "No tienes pedidos activos",
                        subtitle = "Tus pedidos en proceso aparecerán aquí"
                    )
                } else {
                    OrdersList(
                        orders = uiState.activeOrders,
                        onOrderClick = onOrderClick,
                        onCancelOrder = { order -> showCancelDialog = order },
                        showCancelButton = true
                    )
                }
            }

            else -> {
                // Historial de pedidos
                if (uiState.orderHistory.isEmpty()) {
                    EmptyOrdersContent(
                        title = "No tienes historial de pedidos",
                        subtitle = "Tus pedidos completados aparecerán aquí"
                    )
                } else {
                    OrdersList(
                        orders = uiState.orderHistory,
                        onOrderClick = onOrderClick,
                        onCancelOrder = null,
                        showCancelButton = false
                    )
                }
            }
        }
    }

    // Dialog de confirmación para cancelar
    showCancelDialog?.let { order ->
        CancelOrderDialog(
            order = order,
            onConfirm = {
                viewModel.cancelOrder(order.id)
                showCancelDialog = null
            },
            onDismiss = { showCancelDialog = null },
            isLoading = uiState.isCancelling
        )
    }
}

@Composable
private fun OrdersList(
    orders: List<Order>,
    onOrderClick: (String) -> Unit,
    onCancelOrder: ((Order) -> Unit)?,
    showCancelButton: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = orders,
            key = { it.id }
        ) { order ->
            OrderCard(
                order = order,
                onClick = { onOrderClick(order.id) },
                onCancel = if (showCancelButton && order.canBeCancelled) {
                    { onCancelOrder?.invoke(order) }
                } else null
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    onCancel: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con número de pedido y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido #${order.id.takeLast(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OrderStatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fecha
            Text(
                text = formatDate(order.created_at),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Items del pedido (resumen)
            Text(
                text = "${order.items.size} producto(s) • ${order.items.sumOf { it.quantity }} item(s)",
                style = MaterialTheme.typography.bodyMedium
            )

            // Mostrar algunos items
            order.items.take(2).forEach { item ->
                Text(
                    text = "• ${item.quantity}x ${item.menu_item_name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (order.items.size > 2) {
                Text(
                    text = "• y ${order.items.size - 2} más...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ${formatPrice(order.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onCancel != null) {
                        FilledTonalButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancelar")
                        }
                    }

                    FilledTonalButton(onClick = onClick) {
                        Text("Ver detalles")
                    }
                }
            }

            // Notas si existen
            order.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Notas: $notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: OrderStatus) {
    val (color, icon, text) = when (status) {
        OrderStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.Schedule,
            "Pendiente"
        )
        OrderStatus.IN_PREPARATION -> Triple(
            MaterialTheme.colorScheme.primary,
            Icons.Default.Restaurant,
            "En preparación"
        )
        OrderStatus.READY -> Triple(
            Color(0xFF4CAF50),
            Icons.Default.CheckCircle,
            "Listo"
        )
        OrderStatus.DELIVERED -> Triple(
            Color(0xFF2196F3),
            Icons.Default.DeliveryDining,
            "Entregado"
        )
        OrderStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.error,
            Icons.Default.Cancel,
            "Cancelado"
        )
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CancelOrderDialog(
    order: Order,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Cancelar Pedido") },
        text = {
            Column {
                Text("¿Estás seguro de que quieres cancelar este pedido?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pedido #${order.id.takeLast(8)}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total: ${formatPrice(order.totalAmount)}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Cancelar pedido")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("No cancelar")
            }
        }
    )
}

@Composable
private fun EmptyOrdersContent(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingOrdersContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando pedidos...")
        }
    }
}

@Composable
private fun ErrorOrdersContent(
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
            text = "Error al cargar pedidos",
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

// Funciones auxiliares
private fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PA"))
    return formatter.format(price)
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}