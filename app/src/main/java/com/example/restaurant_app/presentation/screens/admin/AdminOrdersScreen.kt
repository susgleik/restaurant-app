// presentation/screens/admin/AdminOrdersScreen.kt
package com.example.restaurant_app.presentation.screens.admin

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.restaurant_app.data.models.Order
import com.example.restaurant_app.data.models.OrderStatus
import com.example.restaurant_app.presentation.viewmodels.AdminOrderViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    modifier: Modifier = Modifier,
    adminOrderViewModel: AdminOrderViewModel = hiltViewModel()
) {
    val uiState by adminOrderViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Estados de las pestañas
    val orderTabs = listOf(
        OrderTab.ACTIVE,
        OrderTab.HISTORY,
        OrderTab.STATS
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { orderTabs.size }
    )

    // Efecto para cargar datos
    LaunchedEffect(Unit) {
        adminOrderViewModel.loadAllOrders()
    }

    // Manejo de mensajes
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            adminOrderViewModel.clearMessages()
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Estadísticas rápidas
        OrderStatsCard(
            activeCount = uiState.activeOrders.size,
            pendingCount = uiState.activeOrders.count { it.status == OrderStatus.PENDING },
            preparingCount = uiState.activeOrders.count { it.status == OrderStatus.IN_PREPARATION },
            readyCount = uiState.activeOrders.count { it.status == OrderStatus.READY },
            modifier = Modifier.padding(16.dp)
        )

        // Tabs para diferentes vistas
        TabRow(
            selectedTabIndex = pagerState.currentPage
        ) {
            orderTabs.forEachIndexed { index, tab ->
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
            when (orderTabs[page]) {
                OrderTab.ACTIVE -> {
                    ActiveOrdersTab(
                        orders = uiState.activeOrders,
                        isLoading = uiState.isLoading,
                        onUpdateOrderStatus = { orderId, status ->
                            adminOrderViewModel.updateOrderStatus(orderId, status)
                        },
                        onRefresh = {
                            adminOrderViewModel.loadAllOrders()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                OrderTab.HISTORY -> {
                    OrderHistoryTab(
                        orders = uiState.orderHistory,
                        isLoading = uiState.isLoading,
                        onRefresh = {
                            adminOrderViewModel.loadAllOrders()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                OrderTab.STATS -> {
                    OrderStatsTab(
                        orders = uiState.allOrders,
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Mostrar mensajes de error
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Aquí podrías mostrar un Snackbar
        }
    }
}

@Composable
private fun OrderStatsCard(
    activeCount: Int,
    pendingCount: Int,
    preparingCount: Int,
    readyCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Estado de Pedidos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip(
                    label = "Pendientes",
                    count = pendingCount,
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.Schedule
                )
                StatChip(
                    label = "Preparando",
                    count = preparingCount,
                    color = MaterialTheme.colorScheme.tertiary,
                    icon = Icons.Default.Restaurant
                )
                StatChip(
                    label = "Listos",
                    count = readyCount,
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.CheckCircle
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    count: Int,
    color: Color,
    icon: ImageVector
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ActiveOrdersTab(
    orders: List<Order>,
    isLoading: Boolean,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Botón de refresh
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pedidos Activos (${orders.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar"
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay pedidos activos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    OrderCard(
                        order = order,
                        isAdmin = true,
                        onUpdateStatus = { status ->
                            onUpdateOrderStatus(order.id, status)
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun OrderHistoryTab(
    orders: List<Order>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Historial (${orders.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar"
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay historial de pedidos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    OrderCard(
                        order = order,
                        isAdmin = true,
                        onUpdateStatus = null // No se puede cambiar estado en historial
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun OrderStatsTab(
    orders: List<Order>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OrderStatsContent(orders = orders)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun OrderStatsContent(orders: List<Order>) {
    val totalOrders = orders.size
    val totalRevenue = orders.sumOf { it.totalAmount }
    val avgOrderValue = if (totalOrders > 0) totalRevenue / totalOrders else 0.0

    val statusCounts = orders.groupingBy { it.status }.eachCount()

    // Estadísticas generales
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Estadísticas Generales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total Pedidos",
                    value = totalOrders.toString(),
                    icon = Icons.Default.Receipt
                )
                StatItem(
                    label = "Ingresos Totales",
                    value = "$${String.format("%.2f", totalRevenue)}",
                    icon = Icons.Default.AttachMoney
                )
                StatItem(
                    label = "Promedio por Pedido",
                    value = "$${String.format("%.2f", avgOrderValue)}",
                    icon = Icons.Default.Calculate
                )
            }
        }
    }

    // Distribución por estado
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Distribución por Estado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            OrderStatus.values().forEach { status ->
                val count = statusCounts[status] ?: 0
                val percentage = if (totalOrders > 0) (count * 100.0 / totalOrders) else 0.0

                StatusDistributionItem(
                    status = status,
                    count = count,
                    percentage = percentage
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusDistributionItem(
    status: OrderStatus,
    count: Int,
    percentage: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(getStatusColor(status))
        )
        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = getStatusDisplayName(status),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "$count (${String.format("%.1f", percentage)}%)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun OrderCard(
    order: Order,
    isAdmin: Boolean,
    onUpdateStatus: ((OrderStatus) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del pedido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido #${order.id.takeLast(8)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = getStatusColor(order.status).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.formattedStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = getStatusColor(order.status),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información del cliente (solo para admin)
            if (isAdmin) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = order.username ?: "Cliente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Items del pedido
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity}x ${item.menu_item_name}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$${String.format("%.2f", item.subtotalAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Instrucciones especiales
                item.special_instructions?.let { instructions ->
                    if (instructions.isNotBlank()) {
                        Text(
                            text = "• $instructions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Total y fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total: $${String.format("%.2f", order.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDateTime(order.created_at),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Acciones del admin
                if (isAdmin && onUpdateStatus != null) {
                    OrderActionButtons(
                        currentStatus = order.status,
                        onUpdateStatus = onUpdateStatus
                    )
                }
            }

            // Notas del pedido
            order.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Nota: $notes",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderActionButtons(
    currentStatus: OrderStatus,
    onUpdateStatus: (OrderStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (currentStatus) {
            OrderStatus.PENDING -> {
                OutlinedButton(
                    onClick = { onUpdateStatus(OrderStatus.IN_PREPARATION) },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Preparar",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                OutlinedButton(
                    onClick = { onUpdateStatus(OrderStatus.CANCELLED) },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            OrderStatus.IN_PREPARATION -> {
                FilledTonalButton(
                    onClick = { onUpdateStatus(OrderStatus.READY) },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Listo",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            OrderStatus.READY -> {
                Button(
                    onClick = { onUpdateStatus(OrderStatus.DELIVERED) },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Entregar",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            else -> {
                // Estados finales - no hay acciones
            }
        }
    }
}

@Composable
private fun getStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.PENDING -> MaterialTheme.colorScheme.error
        OrderStatus.IN_PREPARATION -> MaterialTheme.colorScheme.tertiary
        OrderStatus.READY -> MaterialTheme.colorScheme.primary
        OrderStatus.DELIVERED -> Color(0xFF4CAF50)
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.outline
    }
}

private fun getStatusDisplayName(status: OrderStatus): String {
    return when (status) {
        OrderStatus.PENDING -> "Pendiente"
        OrderStatus.IN_PREPARATION -> "En preparación"
        OrderStatus.READY -> "Listo"
        OrderStatus.DELIVERED -> "Entregado"
        OrderStatus.CANCELLED -> "Cancelado"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDateTime(dateTimeString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateTimeString.replace("Z", ""))
        dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (e: Exception) {
        dateTimeString
    }
}

// Enum para las pestañas de pedidos
enum class OrderTab(
    val title: String,
    val icon: ImageVector
) {
    ACTIVE("Activos", Icons.Default.Restaurant),
    HISTORY("Historial", Icons.Default.History),
    STATS("Estadísticas", Icons.Default.BarChart)
}