// presentation/components/MenuItemDialog.kt
package com.example.restaurant_app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.restaurant_app.data.models.Category
import com.example.restaurant_app.data.models.MenuItem
import com.example.restaurant_app.presentation.viewmodels.MenuDialogType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDialog(
    dialogType: MenuDialogType,
    categories: List<Category>,
    menuItem: MenuItem?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (categoryId: String, name: String, description: String?, price: Double, imageUrl: String?, available: Boolean) -> Unit
) {
    var selectedCategoryId by remember(menuItem) {
        mutableStateOf(menuItem?.category_id ?: categories.firstOrNull()?.id ?: "")
    }
    var name by remember(menuItem) { mutableStateOf(menuItem?.name ?: "") }
    var description by remember(menuItem) { mutableStateOf(menuItem?.description ?: "") }
    var priceText by remember(menuItem) { mutableStateOf(menuItem?.price?.toString() ?: "") }
    var imageUrl by remember(menuItem) { mutableStateOf(menuItem?.image_url ?: "") }
    var available by remember(menuItem) { mutableStateOf(menuItem?.available ?: true) }

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    val selectedCategory = categories.find { it.id == selectedCategoryId }
    val price = priceText.toDoubleOrNull()
    val isValid = name.isNotBlank() &&
            selectedCategoryId.isNotBlank() &&
            price != null &&
            price > 0

    // Validaciones en tiempo real
    LaunchedEffect(name) {
        nameError = name.isBlank()
    }

    LaunchedEffect(priceText) {
        priceError = priceText.isNotBlank() && (priceText.toDoubleOrNull() == null || priceText.toDouble() <= 0)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    text = when (dialogType) {
                        MenuDialogType.CREATE -> "Nuevo Producto"
                        MenuDialogType.EDIT -> "Editar Producto"
                    },
                    style = MaterialTheme.typography.headlineSmall
                )

                // Selector de categoría
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Seleccionar categoría",
                        onValueChange = { },
                        label = { Text("Categoría") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = selectedCategoryId.isBlank(),
                        supportingText = if (selectedCategoryId.isBlank()) {
                            { Text("Selecciona una categoría") }
                        } else null
                    )

                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (category.description.isNotBlank()) {
                                            Text(
                                                text = category.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedCategoryId = category.id
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                // Nombre del producto
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del producto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("El nombre es requerido") }
                    } else null
                )

                // Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    minLines = 2
                )

                // Precio
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Precio") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("$") },
                    isError = priceError,
                    supportingText = if (priceError) {
                        { Text("Ingresa un precio válido mayor a 0") }
                    } else null
                )

                // URL de imagen
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de imagen (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("https://ejemplo.com/imagen.jpg") }
                )

                // Switch de disponibilidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = available,
                        onCheckedChange = { available = it }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (available) "Producto disponible" else "Producto no disponible",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (isValid && price != null) {
                                onConfirm(
                                    selectedCategoryId,
                                    name.trim(),
                                    description.trim().takeIf { it.isNotBlank() },
                                    price,
                                    imageUrl.trim().takeIf { it.isNotBlank() },
                                    available
                                )
                            }
                        },
                        enabled = isValid && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            when (dialogType) {
                                MenuDialogType.CREATE -> "Crear Producto"
                                MenuDialogType.EDIT -> "Guardar Cambios"
                            }
                        )
                    }
                }
            }
        }
    }
}