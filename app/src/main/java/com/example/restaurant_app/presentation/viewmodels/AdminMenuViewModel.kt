// presentation/viewmodels/AdminMenuViewModel.kt - Versión final corregida
package com.example.restaurant_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.repository.MenuRepository
import com.example.restaurant_app.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminMenuUiState(
    val menuItems: List<MenuItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedMenuItem: MenuItem? = null,
    val selectedCategory: Category? = null,
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showDialog: Boolean = false,
    val dialogType: MenuDialogType = MenuDialogType.CREATE,
    val searchQuery: String = ""
)

enum class MenuDialogType {
    CREATE, EDIT
}

@HiltViewModel
class AdminMenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminMenuUiState())
    val uiState: StateFlow<AdminMenuUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        loadCategories()
        loadMenuItems()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().collect { result ->
                when (result) {
                    is MenuResult.Success -> {
                        val categoryList = result.data
                        _uiState.update {
                            it.copy(categories = categoryList.categories)
                        }
                    }
                    is MenuResult.Error -> {
                        _uiState.update {
                            it.copy(errorMessage = "Error al cargar categorías: ${result.message}")
                        }
                    }
                    else -> {} // Loading state handled in menu loading
                }
            }
        }
    }

    fun loadMenuItems(categoryId: String? = null) {
        viewModelScope.launch {
            menuRepository.getMenuItems(
                categoryId = categoryId,
                search = if (_searchQuery.value.isNotBlank()) _searchQuery.value else null
            ).collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is MenuResult.Success -> {
                        val menuItemList = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                menuItems = menuItemList.items,
                                errorMessage = null
                            )
                        }
                    }
                    is MenuResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun createMenuItem(
        categoryId: String,
        name: String,
        description: String?,
        price: Double,
        imageUrl: String?,
        available: Boolean = true
    ) {
        if (name.isBlank() || price <= 0) {
            _uiState.update {
                it.copy(errorMessage = "Nombre y precio son obligatorios y válidos")
            }
            return
        }

        viewModelScope.launch {
            val menuItemCreate = MenuItemCreate(
                category_id = categoryId,
                name = name.trim(),
                description = description?.trim()?.takeIf { it.isNotBlank() },
                price = price,
                image_url = imageUrl?.trim()?.takeIf { it.isNotBlank() },
                available = available
            )

            menuRepository.createMenuItem(menuItemCreate).collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.update { it.copy(isCreating = true, errorMessage = null) }
                    }
                    is MenuResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isCreating = false,
                                successMessage = "Producto creado exitosamente",
                                showDialog = false
                            )
                        }
                        loadMenuItems() // Recargar lista
                    }
                    is MenuResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isCreating = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateMenuItem(
        id: String,
        categoryId: String?,
        name: String?,
        description: String?,
        price: Double?,
        imageUrl: String?,
        available: Boolean?
    ) {
        viewModelScope.launch {
            val menuItemUpdate = MenuItemUpdate(
                category_id = categoryId,
                name = name?.trim()?.takeIf { it.isNotBlank() },
                description = description?.trim()?.takeIf { it.isNotBlank() },
                price = price?.takeIf { it > 0 },
                image_url = imageUrl?.trim()?.takeIf { it.isNotBlank() },
                available = available
            )

            menuRepository.updateMenuItem(id, menuItemUpdate).collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.update { it.copy(isUpdating = true, errorMessage = null) }
                    }
                    is MenuResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isUpdating = false,
                                successMessage = "Producto actualizado exitosamente",
                                showDialog = false
                            )
                        }
                        loadMenuItems() // Recargar lista
                    }
                    is MenuResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isUpdating = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun deleteMenuItem(id: String) {
        viewModelScope.launch {
            menuRepository.deleteMenuItem(id).collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
                    }
                    is MenuResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isDeleting = false,
                                successMessage = "Producto eliminado exitosamente"
                            )
                        }
                        loadMenuItems() // Recargar lista
                    }
                    is MenuResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isDeleting = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun toggleItemAvailability(item: MenuItem) {
        viewModelScope.launch {
            menuRepository.toggleItemAvailability(item.id, !item.available).collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.update { it.copy(isUpdating = true, errorMessage = null) }
                    }
                    is MenuResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isUpdating = false,
                                successMessage = if (item.available) {
                                    "Producto marcado como no disponible"
                                } else {
                                    "Producto marcado como disponible"
                                }
                            )
                        }
                        loadMenuItems() // Recargar lista
                    }
                    is MenuResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isUpdating = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun showCreateDialog() {
        _uiState.update {
            it.copy(
                showDialog = true,
                dialogType = MenuDialogType.CREATE,
                selectedMenuItem = null,
                errorMessage = null
            )
        }
    }

    fun showEditDialog(menuItem: MenuItem) {
        _uiState.update {
            it.copy(
                showDialog = true,
                dialogType = MenuDialogType.EDIT,
                selectedMenuItem = menuItem,
                errorMessage = null
            )
        }
    }

    fun hideDialog() {
        _uiState.update {
            it.copy(
                showDialog = false,
                selectedMenuItem = null,
                errorMessage = null
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
        loadMenuItems(_uiState.value.selectedCategory?.id)
    }

    fun selectCategory(category: Category?) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadMenuItems(category?.id)
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun refreshMenu() {
        loadData()
    }
}