// presentation/viewmodels/AdminCategoryViewModel.kt - Compatible con MenuResult
package com.example.restaurant_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurant_app.data.models.*
import com.example.restaurant_app.data.repository.CategoryRepository
import com.example.restaurant_app.data.repository.MenuResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showDialog: Boolean = false,
    val dialogType: DialogType = DialogType.CREATE
)

enum class DialogType {
    CREATE, EDIT
}

@HiltViewModel
class AdminCategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories(activeOnly = null).collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is MenuResult.Success -> {
                        val categoryList = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                categories = categoryList.categories,
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

    fun createCategory(name: String, description: String, active: Boolean = true) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El nombre es requerido") }
            return
        }

        viewModelScope.launch {
            val categoryCreate = CategoryCreate(
                name = name.trim(),
                description = description.trim(),
                active = active
            )

            categoryRepository.createCategory(categoryCreate).collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.update { it.copy(isCreating = true, errorMessage = null) }
                    }
                    is MenuResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isCreating = false,
                                successMessage = "Categoría creada exitosamente",
                                showDialog = false
                            )
                        }
                        loadCategories() // Recargar lista
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

    fun updateCategory(id: String, name: String?, description: String?, active: Boolean?) {
        viewModelScope.launch {
            val categoryUpdate = CategoryUpdate(
                name = name?.trim()?.takeIf { it.isNotBlank() },
                description = description?.trim(),
                active = active
            )

            categoryRepository.updateCategory(id, categoryUpdate).collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.update { it.copy(isUpdating = true, errorMessage = null) }
                    }
                    is MenuResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isUpdating = false,
                                successMessage = "Categoría actualizada exitosamente",
                                showDialog = false
                            )
                        }
                        loadCategories() // Recargar lista
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

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(id).collect { result ->
                when (result) {
                    is MenuResult.Loading -> {
                        _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
                    }
                    is MenuResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isDeleting = false,
                                successMessage = "Categoría eliminada exitosamente"
                            )
                        }
                        loadCategories() // Recargar lista
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

    fun showCreateDialog() {
        _uiState.update {
            it.copy(
                showDialog = true,
                dialogType = DialogType.CREATE,
                selectedCategory = null,
                errorMessage = null
            )
        }
    }

    fun showEditDialog(category: Category) {
        _uiState.update {
            it.copy(
                showDialog = true,
                dialogType = DialogType.EDIT,
                selectedCategory = category,
                errorMessage = null
            )
        }
    }

    fun hideDialog() {
        _uiState.update {
            it.copy(
                showDialog = false,
                selectedCategory = null,
                errorMessage = null
            )
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun toggleCategoryStatus(category: Category) {
        updateCategory(
            id = category.id,
            name = null,
            description = null,
            active = !category.active
        )
    }
}