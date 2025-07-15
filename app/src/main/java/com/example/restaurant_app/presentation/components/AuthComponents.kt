package com.example.restaurant_app.presentation.components

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Indicador de fortaleza de contraseña
 */
@Composable
fun PasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier
) {
    val strength = calculatePasswordStrength(password)

    Column(modifier = modifier) {
        // Barra de fortaleza
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = when {
                                index < strength.level -> strength.color
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            },
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        if (password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strength.label,
                color = strength.color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Lista de validaciones para el registro
 */
@Composable
fun PasswordValidationList(
    password: String,
    modifier: Modifier = Modifier
) {
    val validations = listOf(
        ValidationRule(
            label = "Al menos 6 caracteres",
            isValid = password.length >= 6
        ),
        ValidationRule(
            label = "Contiene una letra",
            isValid = password.any { it.isLetter() }
        ),
        ValidationRule(
            label = "Contiene un número",
            isValid = password.any { it.isDigit() }
        ),
        ValidationRule(
            label = "Contiene un carácter especial",
            isValid = password.any { !it.isLetterOrDigit() }
        )
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        validations.forEach { rule ->
            ValidationItem(rule = rule)
        }
    }
}

@Composable
private fun ValidationItem(rule: ValidationRule) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (rule.isValid) Icons.Default.CheckCircle else Icons.Default.AddCircle,
            contentDescription = null,
            tint = if (rule.isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = rule.label,
            fontSize = 12.sp,
            color = if (rule.isValid) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
        )
    }
}

/**
 * Campo de texto personalizado para autenticación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null)
        },
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = supportingText?.let { { Text(it, fontSize = 12.sp) } },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Botón de carga personalizado
 */
@Composable
fun LoadingButton(
    onClick: () -> Unit,
    text: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Mensaje de error/éxito
 */
@Composable
fun MessageCard(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = message,
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )

            onDismiss?.let {
                IconButton(
                    onClick = it,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = if (isError) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// =================== CLASES DE DATOS Y FUNCIONES DE UTILIDAD ===================

data class ValidationRule(
    val label: String,
    val isValid: Boolean
)

data class PasswordStrength(
    val level: Int, // 0-4
    val label: String,
    val color: Color
)

@Composable
private fun calculatePasswordStrength(password: String): PasswordStrength {
    var score = 0

    if (password.length >= 6) score++
    if (password.length >= 8) score++
    if (password.any { it.isLowerCase() } && password.any { it.isUpperCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when (score) {
        0, 1 -> PasswordStrength(
            level = 1,
            label = "Muy débil",
            color = MaterialTheme.colorScheme.error
        )
        2 -> PasswordStrength(
            level = 2,
            label = "Débil",
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        )
        3 -> PasswordStrength(
            level = 3,
            label = "Regular",
            color = Color(0xFFFF9800) // Orange
        )
        4 -> PasswordStrength(
            level = 4,
            label = "Fuerte",
            color = MaterialTheme.colorScheme.primary
        )
        else -> PasswordStrength(
            level = 4,
            label = "Muy fuerte",
            color = Color(0xFF4CAF50) // Green
        )
    }
}

/**
 * Validador de email en tiempo real
 */
@Composable
fun EmailValidationIndicator(
    email: String,
    modifier: Modifier = Modifier
) {
    if (email.isNotEmpty()) {
        val isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isValid) "Email válido" else "Formato de email inválido",
                color = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
    }
}