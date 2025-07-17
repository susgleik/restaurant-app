package com.example.restaurant_app.data.models

/**
 * Clase sellada para manejar diferentes estados de las operaciones del menú
 *
 * @param T El tipo de datos que se retorna en caso de éxito
 */
sealed class MenuResult<out T> {
    /**
     * Estado de éxito con los datos obtenidos
     */
    data class Success<T>(val data: T) : MenuResult<T>()

    /**
     * Estado de error con mensaje descriptivo
     */
    data class Error<T>(val message: String) : MenuResult<T>()

    /**
     * Estado de carga con mensaje opcional
     */
    data class Loading<T>(val message: String = "Cargando...") : MenuResult<T>()
}

/**
 * Extensión para verificar si el resultado es exitoso
 */
fun <T> MenuResult<T>.isSuccess(): Boolean = this is MenuResult.Success

/**
 * Extensión para verificar si el resultado es un error
 */
fun <T> MenuResult<T>.isError(): Boolean = this is MenuResult.Error

/**
 * Extensión para verificar si está cargando
 */
fun <T> MenuResult<T>.isLoading(): Boolean = this is MenuResult.Loading

/**
 * Extensión para obtener los datos si es exitoso, null en caso contrario
 */
fun <T> MenuResult<T>.getDataOrNull(): T? = when (this) {
    is MenuResult.Success -> data
    else -> null
}

/**
 * Extensión para obtener el mensaje de error si es un error, null en caso contrario
 */
fun <T> MenuResult<T>.getErrorOrNull(): String? = when (this) {
    is MenuResult.Error -> message
    else -> null
}