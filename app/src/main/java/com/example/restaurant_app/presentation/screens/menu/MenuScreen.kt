package com.example.restaurant_app.presentation.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.restaurant_app.data.models.Category
import com.example.restaurant_app.data.models.MenuItem
import com.example.restaurant_app.presentation.components.*
import com.example.restaurant_app.presentation.viewmodels.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onMenuItemClick: (MenuItem) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshMenu()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Barra de búsqueda
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Chips de categorías
        if (uiState.categories.isNotEmpty()) {
            CategoryChips(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::selectCategory,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Contenido principal
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }

                uiState.errorMessage != null -> {
                    ErrorMessage(
                        message = uiState.errorMessage!!,
                        onRetry = {
                            viewModel.clearError()
                            viewModel.refreshMenu()
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.menuItems.isEmpty() -> {
                    EmptyMenuState(
                        message = if (searchQuery.isNotBlank()) {
                            "No se encontraron productos para \"$searchQuery\""
                        } else {
                            "No hay productos disponibles"
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    MenuContent(
                        menuItems = uiState.menuItems,
                        onMenuItemClick = onMenuItemClick,
                        onAddToCart = onAddToCart
                    )
                }
            }

            // Botón de refresh flotante
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
    }
}

@Composable
private fun LoadingContent() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(6) { // Mostrar 6 cards de loading
            LoadingCard()
        }
    }
}

@Composable
private fun MenuContent(
    menuItems: List<MenuItem>,
    onMenuItemClick: (MenuItem) -> Unit,
    onAddToCart: (MenuItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(menuItems) { menuItem ->
            MenuItemCard(
                menuItem = menuItem,
                onItemClick = onMenuItemClick,
                onAddToCart = onAddToCart
            )
        }
    }
}