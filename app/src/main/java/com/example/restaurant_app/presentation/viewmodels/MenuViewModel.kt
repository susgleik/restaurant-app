package com.example.restaurant_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.repository.MenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val categories: List<Category> = emptyList(),
    val menuItems: List<MenuItem> = emptyList(),
    val selectedCategory: Category? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedMenuItem: MenuItemWithCategory? = null
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadCategories()
        loadMenuItems()
    }

    fun loadCategories() {
        viewModelScope.launch {
            menuRepository.getCategories(activeOnly = true).collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                    is MenuResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            categories = result.data.categories,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is MenuResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadMenuItems(categoryId: String? = null) {
        viewModelScope.launch {
            val repository = if (categoryId != null) {
                menuRepository.getMenuItemsByCategory(categoryId, availableOnly = true)
            } else {
                menuRepository.getMenuItems(available = true)
            }

            repository.collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                    is MenuResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            menuItems = result.data.items,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is MenuResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun selectCategory(category: Category?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadMenuItems(category?.id)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)

        if (query.isBlank()) {
            loadMenuItems(_uiState.value.selectedCategory?.id)
        } else {
            searchMenuItems(query)
        }
    }

    private fun searchMenuItems(query: String) {
        viewModelScope.launch {
            menuRepository.searchMenuItems(
                query = query,
                categoryId = _uiState.value.selectedCategory?.id
            ).collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                    is MenuResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            menuItems = result.data.items,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is MenuResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadMenuItemDetails(itemId: String) {
        viewModelScope.launch {
            menuRepository.getMenuItem(itemId).collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is MenuResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            selectedMenuItem = result.data,
                            isLoading = false
                        )
                    }
                    is MenuResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearSelectedMenuItem() {
        _uiState.value = _uiState.value.copy(selectedMenuItem = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun refreshMenu() {
        loadCategories()
        loadMenuItems(_uiState.value.selectedCategory?.id)
    }
}