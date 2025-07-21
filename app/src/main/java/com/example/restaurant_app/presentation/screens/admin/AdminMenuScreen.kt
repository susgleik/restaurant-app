// presentation/screens/admin/AdminMenuScreen.kt
package com.example.restaurant_app.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.restaurant_app.data.models.Category
import com.example.restaurant_app.data.models.MenuItem
import com.example.restaurant_app.presentation.components.CategoryChips
import com.example.restaurant_app.presentation.components.MenuItemDialog
import com.example.restaurant_app.presentation.components.SearchBar
import com.example.restaurant_app.presentation.viewmodels.AdminMenuViewModel
import com.example.restaurant_app.presentation.viewmodels.MenuDialogType
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminMenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // Manejar mensajes
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gestión del Menú",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { viewModel.showCreateDialog() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nuevo Producto")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barra de búsqueda
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    modifier = Modifier.fillMaxWidth()
                )

                // Chips de categorías
                if (uiState.categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryChips(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = viewModel::selectCategory,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Contenido principal
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage != null -> {
                    ErrorContent(
                        message = uiState.errorMessage!!,
                        onRetry = { viewModel.refreshMenu() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.menuItems.isEmpty() -> {
                    EmptyMenuContent(
                        message = if (searchQuery.isNotBlank()) {
                            "No se encontraron productos para \"$searchQuery\""
                        } else {
                            "No hay productos en el menú"
                        },
                        onAddClick = { viewModel.showCreateDialog() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    AdminMenuContent(
                        menuItems = uiState.menuItems,
                        onEditClick = { viewModel.showEditDialog(it) },
                        onToggleAvailability = { viewModel.toggleItemAvailability(it) },
                        onDeleteClick = { viewModel.deleteMenuItem(it.id) },
                        isDeleting = uiState.isDeleting,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // FAB para refresh
            FloatingActionButton(
                onClick = { viewModel.refreshMenu() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar menú"
                )
            }
        }

        // Dialog para crear/editar producto
        if (uiState.showDialog) {
            MenuItemDialog(
                dialogType = uiState.dialogType,
                categories = uiState.categories,
                menuItem = uiState.selectedMenuItem,
                isLoading = if (uiState.dialogType == MenuDialogType.CREATE) uiState.isCreating else uiState.isUpdating,
                onDismiss = { viewModel.hideDialog() },
                onConfirm = { categoryId, name, description, price, imageUrl, available ->
                    when (uiState.dialogType) {
                        MenuDialogType.CREATE -> {
                            viewModel.createMenuItem(categoryId, name, description, price, imageUrl, available)
                        }
                        MenuDialogType.EDIT -> {
                            uiState.selectedMenuItem?.let { item ->
                                viewModel.updateMenuItem(
                                    id = item.id,
                                    categoryId = categoryId,
                                    name = name,
                                    description = description,
                                    price = price,
                                    imageUrl = imageUrl,
                                    available = available
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun AdminMenuContent(
    menuItems: List<MenuItem>,
    onEditClick: (MenuItem) -> Unit,
    onToggleAvailability: (MenuItem) -> Unit,
    onDeleteClick: (MenuItem) -> Unit,
    isDeleting: Boolean,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1), // Una columna para mostrar más información
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(menuItems) { menuItem ->
            AdminMenuItemCard(
                menuItem = menuItem,
                onEditClick = { onEditClick(menuItem) },
                onToggleAvailability = { onToggleAvailability(menuItem) },
                onDeleteClick = { onDeleteClick(menuItem) },
                isDeleting = isDeleting
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminMenuItemCard(
    menuItem: MenuItem,
    onEditClick: () -> Unit,
    onToggleAvailability: () -> Unit,
    onDeleteClick: () -> Unit,
    isDeleting: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (menuItem.available)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.ImageNotSupported,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = menuItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Surface(
                        color = if (menuItem.available)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (menuItem.available) "Disponible" else "No disponible",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (menuItem.available)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatPrice(menuItem.price),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (!menuItem.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = menuItem.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Botones de acción
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onToggleAvailability) {
                    Icon(
                        imageVector = if (menuItem.available) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (menuItem.available) "Marcar como no disponible" else "Marcar como disponible",
                        tint = if (menuItem.available) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )

        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun EmptyMenuContent(
    message: String,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.RestaurantMenu,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(onClick = onAddClick) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar Primer Producto")
        }
    }
}

// Función auxiliar para formatear precios
private fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PA"))
    return formatter.format(price)
}