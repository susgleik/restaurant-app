package com.example.restaurant_app.presentation.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.restaurant_app.data.models.MenuItemWithCategory
import com.example.restaurant_app.presentation.viewmodels.MenuViewModel
import com.example.restaurant_app.presentation.viewmodels.CartViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDetailScreen(
    menuItemId: String,
    onNavigateBack: () -> Unit,
    onAddToCart: (String, Int) -> Unit, // itemId, quantity
    modifier: Modifier = Modifier,
    menuViewModel: MenuViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val menuUiState by menuViewModel.uiState.collectAsStateWithLifecycle()
    val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
    var quantity by remember { mutableIntStateOf(1) }

    // Verificar si el item ya está en el carrito
    val cartItem = cartViewModel.getCartItemByMenuId(menuItemId)
    val isInCart = cartItem != null

    LaunchedEffect(menuItemId) {
        menuViewModel.loadMenuItemDetails(menuItemId)
    }

    // Manejar éxito al agregar al carrito
    LaunchedEffect(cartUiState.successMessage) {
        cartUiState.successMessage?.let {
            // Limpiar mensaje sin recargar carrito extra
            cartViewModel.clearMessages()
        }
    }

    when {
        menuUiState.isLoading -> {
            LoadingDetailScreen(onNavigateBack = onNavigateBack)
        }

        menuUiState.errorMessage != null -> {
            ErrorDetailScreen(
                message = menuUiState.errorMessage!!,
                onNavigateBack = onNavigateBack,
                onRetry = { menuViewModel.loadMenuItemDetails(menuItemId) }
            )
        }

        menuUiState.selectedMenuItem != null -> {
            MenuItemDetailContent(
                menuItem = menuUiState.selectedMenuItem!!,
                quantity = quantity,
                onQuantityChange = { quantity = it },
                onNavigateBack = onNavigateBack,
                onAddToCart = { onAddToCart(menuItemId, quantity) },
                isInCart = isInCart,
                cartQuantity = cartItem?.quantity ?: 0,
                onUpdateCartQuantity = { newQuantity ->
                    if (cartItem != null) {
                        cartViewModel.updateItemQuantity(cartItem.id, newQuantity)
                    }
                },
                onRemoveFromCart = {
                    if (cartItem != null) {
                        cartViewModel.removeItem(cartItem.id)
                    }
                },
                isUpdating = cartUiState.isUpdating,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItemDetailContent(
    menuItem: MenuItemWithCategory,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onAddToCart: () -> Unit,
    isInCart: Boolean,
    cartQuantity: Int,
    onUpdateCartQuantity: (Int) -> Unit,
    onRemoveFromCart: () -> Unit,
    isUpdating: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Detalle del producto") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        )

        // Contenido scrolleable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Imagen del producto
            if (!menuItem.image_url.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(menuItem.image_url)
                        .crossfade(true)
                        .build(),
                    contentDescription = menuItem.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Información del producto
            Text(
                text = menuItem.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Categoría
            if (!menuItem.category_name.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = menuItem.category_name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Precio
            Text(
                text = formatPrice(menuItem.price),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Estado en carrito si está agregado
            if (isInCart) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Ya tienes $cartQuantity en tu carrito",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Descripción
            if (!menuItem.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = menuItem.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Estado de disponibilidad
            if (!menuItem.available) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Este producto no está disponible en este momento",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Sección inferior con cantidad y botones
        if (menuItem.available) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (isInCart) {
                        // Controles para item ya en carrito
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "En carrito",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FilledIconButton(
                                    onClick = {
                                        if (cartQuantity > 1) {
                                            onUpdateCartQuantity(cartQuantity - 1)
                                        } else {
                                            onRemoveFromCart()
                                        }
                                    },
                                    enabled = !isUpdating
                                ) {
                                    Icon(
                                        imageVector = if (cartQuantity > 1) Icons.Default.Remove else Icons.Default.Remove,
                                        contentDescription = if (cartQuantity > 1) "Disminuir cantidad" else "Eliminar del carrito"
                                    )
                                }

                                Text(
                                    text = cartQuantity.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                FilledIconButton(
                                    onClick = { onUpdateCartQuantity(cartQuantity + 1) },
                                    enabled = !isUpdating
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Aumentar cantidad"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón para agregar más cantidad
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRemoveFromCart,
                                modifier = Modifier.weight(1f),
                                enabled = !isUpdating
                            ) {
                                Text("Eliminar")
                            }

                            Button(
                                onClick = onAddToCart,
                                modifier = Modifier.weight(1f),
                                enabled = !isUpdating
                            ) {
                                if (isUpdating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Agregar más")
                                }
                            }
                        }
                    } else {
                        // Controles para item no en carrito
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cantidad",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FilledIconButton(
                                    onClick = {
                                        if (quantity > 1) onQuantityChange(quantity - 1)
                                    },
                                    enabled = quantity > 1
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Disminuir cantidad"
                                    )
                                }

                                Text(
                                    text = quantity.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                FilledIconButton(
                                    onClick = { onQuantityChange(quantity + 1) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Aumentar cantidad"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón agregar al carrito
                        Button(
                            onClick = onAddToCart,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isUpdating
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isUpdating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null
                                    )
                                }
                                Text("Agregar al carrito")
                                Text("• ${formatPrice(menuItem.price * quantity)}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingDetailScreen(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Cargando...") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorDetailScreen(
    message: String,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Error") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

// Función auxiliar para formatear precios
private fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PA"))
    return formatter.format(price)
}