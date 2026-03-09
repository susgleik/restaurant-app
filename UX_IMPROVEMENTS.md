# UI/UX Mejoras - Restaurant App (Android)

Análisis basado en el código actual. Priorizado por impacto en experiencia de usuario.

---

## PRIORIDAD ALTA — Impacto directo en usabilidad

### ✅ 1. Imágenes de productos sin fallback adecuado — COMPLETADO
**Problema:** Las cards de menú usan `AsyncImage` de Coil pero sin placeholder ni error state significativo. Si una imagen falla, la card queda con espacio en blanco o icono genérico roto.

**Archivos afectados:**
- `presentation/screens/menu/MenuScreen.kt`
- `presentation/screens/menu/MenuItemDetailScreen.kt`
- `presentation/components/MenuComponents.kt`
- `presentation/screens/admin/AdminMenuScreen.kt` — formulario solo acepta URL, sin upload

**Mejoras:**
- ✅ Creado composable reutilizable `ProductImage` en `MenuComponents.kt` con estados: loading (spinner + surfaceVariant), error (`ImageNotSupported` icon) y fallback para URL null/blank
- ✅ `MenuItemCard`: siempre muestra área de imagen de 120dp (antes se ocultaba si no había URL)
- ✅ `MenuItemDetailScreen`: siempre muestra área de imagen de 250dp con fallback
- ✅ `AdminMenuItemCard`: reemplazado bloque if/else manual por `ProductImage` unificado
- ⬜ En `AdminMenuScreen`, agregar opción de subir imagen desde galería (usa el endpoint existente de upload)

---

### ✅ 2. Gestión del teclado y foco en formularios — COMPLETADO
**Problema:** En `LoginScreen`, `RegisterScreen` y diálogos de admin no hay manejo de `ImeAction`. Presionar "Enter/Next" en el teclado no mueve el foco al siguiente campo.

**Archivos afectados:**
- `presentation/screens/auth/LoginScreen.kt`
- `presentation/screens/auth/RegisterScreen.kt`
- `presentation/screens/admin/AdminMenuScreen.kt` — diálogo de crear/editar producto
- `presentation/screens/admin/AdminCategoriesScreen.kt` — diálogo de crear/editar categoría

**Mejoras:**
- ✅ `LoginScreen`: Email→`ImeAction.Next`→password; Password→`ImeAction.Done`→cierra teclado y dispara login
- ✅ `RegisterScreen`: Cadena completa Username→Email→Password→ConfirmPassword→`Done`→dispara registro si válido
- ✅ `CategoryDialog`: Nombre→`ImeAction.Next`→descripción (foco por `FocusRequester`)
- ✅ `MenuItemDialog`: Nombre→Next→descripción; Precio→Next→imageUrl; imageUrl→`Done`→cierra teclado

---

### ✅ 3. Estados vacíos y de error poco informativos — COMPLETADO
**Problema:** Varios screens muestran mensajes genéricos ("Error al cargar", "No hay items") sin contexto ni acciones de recuperación claras.

**Archivos afectados:**
- `presentation/screens/orders/OrdersScreen.kt` — empty state sin CTA a crear pedido
- `presentation/screens/cart/CartScreen.kt` — error state con botón retry pero sin detalles
- `presentation/screens/menu/MenuScreen.kt` — empty state cuando el filtro no da resultados vs cuando no hay datos

**Mejoras:**
- ✅ `EmptyMenuState`: añadido `onAction` opcional; `MenuScreen` distingue: búsqueda vacía / categoría sin productos / sin datos, y pasa "Ver todos los productos" como CTA cuando hay filtro activo
- ✅ `OrdersScreen`: añadido `onNavigateToMenu`; ambos empty states (Activos e Historial) muestran botón "Ir al Menú"
- ✅ `ErrorOrdersContent`: icono `WifiOff`, hint de conexión, mensaje técnico en texto pequeño/muted, botón Refresh
- ✅ `ErrorCartContent`: misma estructura mejorada — hint de conexión, mensaje técnico muted, botón con icono Refresh

---

### ✅ 4. Confirmación de acciones destructivas inconsistente — COMPLETADO
**Problema:** Hay confirmación para "vaciar carrito" y "cerrar sesión", pero **no** para eliminar categoría, eliminar producto del menú, ni cancelar pedido desde admin.

**Archivos afectados:**
- `presentation/screens/admin/AdminCategoriesScreen.kt` — delete sin confirm
- `presentation/screens/admin/AdminMenuScreen.kt` — delete sin confirm
- `presentation/screens/admin/AdminOrdersScreen.kt` — cancelar pedido sin confirm

**Mejoras:**
- ✅ `AdminCategoriesScreen`: `AlertDialog` de confirmación mostrando nombre de la categoría en rojo antes de eliminar
- ✅ `AdminMenuScreen`: `AlertDialog` de confirmación mostrando nombre del producto en rojo antes de eliminar
- ✅ `AdminOrdersScreen`: `AlertDialog` de confirmación en `OrderActionButtons` antes de cambiar status a CANCELLED

---

### ✅ 5. Feedback insuficiente en operaciones de carrito — COMPLETADO
**Problema:** Al agregar un ítem al carrito desde `MenuScreen` o `MenuItemDetailScreen`, no hay confirmación visual inmediata (no hay snackbar, toast, ni animación). El usuario debe ir al carrito para confirmar que funcionó.

**Archivos afectados:**
- `presentation/screens/menu/MenuScreen.kt`
- `presentation/screens/menu/MenuItemDetailScreen.kt`
- `presentation/components/MenuComponents.kt` — botón "Agregar"

**Mejoras:**
- ✅ `HomeScreen`: añadido `SnackbarHostState` + `SnackbarHost` en el `Scaffold`; `LaunchedEffect` observa `cartUiState.successMessage` (ya lo establece `CartViewModel.addToCart`) y muestra snackbar con acción "Ver carrito" que navega directamente a `Screen.Cart`
- ⬜ Animación micro en el botón "Agregar" o badge del BottomNav (complejidad alta, valor incremental)

---

### ✅ 6. Sin manejo de token expirado durante sesión activa — COMPLETADO
**Problema:** Si el JWT expira mientras el usuario está navegando (30 min), las llamadas a la API devuelven 401 pero la app muestra errores genéricos en lugar de redirigir al login.

**Archivos afectados:**
- `network/AuthInterceptor.kt` — no maneja 401 responses
- Todos los repositorios — manejan errores HTTP pero no distinguen 401

**Mejoras:**
- ✅ Creado `network/SessionManager.kt`: singleton con `MutableSharedFlow<Unit>` y `notifySessionExpired()` thread-safe via `tryEmit`
- ✅ `AuthInterceptor`: si `response.code == 401` → `tokenManager.clearTokens()` + `sessionManager.notifySessionExpired()`
- ✅ `NetworkModule`: `provideAuthInterceptor` actualizado para recibir `SessionManager`
- ✅ `AuthViewModel`: inyecta `SessionManager`, expone `sessionExpiredEvents: SharedFlow<Unit>`
- ✅ `RestaurantNavigation`: `LaunchedEffect` recolecta el flow → muestra `AlertDialog` → al confirmar, `logout()` + navega a `auth`

---

## PRIORIDAD MEDIA — Mejoras de flujo y funcionalidades faltantes

### ✅ 7. Búsqueda en MenuScreen sin debounce ni limpiar — COMPLETADO
**Problema:** El campo de búsqueda en `MenuScreen` filtra en cada keystroke sin debounce, lo que puede causar flickering con muchos items. Además, no hay botón X para limpiar el texto.

**Archivos afectados:**
- `presentation/screens/menu/MenuScreen.kt`
- `presentation/components/MenuComponents.kt` — `SearchBar`

**Mejoras:**
- ✅ `MenuViewModel`: `updateSearchQuery` ya no dispara búsquedas directamente; colector en `init` con `debounce(300L).distinctUntilChanged()` controla las llamadas al repositorio
- ✅ `SearchBar`: `trailingIcon` muestra `Icons.Default.Clear` cuando `query.isNotBlank()` — al tocar llama `onQueryChange("")`
- ✅ `MenuScreen`: muestra contador "N producto(s) encontrado(s)" cuando hay filtro activo y resultados

---

### 8. Pantalla de perfil sin edición
**Problema:** El botón de editar perfil está comentado en `ProfileScreen.kt` esperando el endpoint PUT. La pantalla está incompleta y el usuario no puede cambiar ni su nombre ni contraseña desde la app.

**Archivos afectados:**
- `presentation/screens/profile/ProfileScreen.kt` — edit button comentado
- `presentation/viewmodels/ProfileViewModel.kt`

**Mejoras:**
- Mientras el endpoint PUT no existe: activar al menos "Cambiar contraseña" (ya existe `POST /api/v1/auth/change-password`)
- Formulario de cambio de contraseña: contraseña actual + nueva + confirmar
- Mostrar sección de "Seguridad" separada del perfil general

---

### 9. Seguimiento de pedidos sin progreso visual
**Problema:** `OrdersScreen` muestra el estado del pedido como badge de texto coloreado, pero no hay representación visual del progreso (`PENDING → IN_PREPARATION → READY → DELIVERED`).

**Archivos afectados:**
- `presentation/screens/orders/OrdersScreen.kt`
- Vista de detalle de pedido en `HomeScreen.kt`

**Mejoras:**
- Agregar `StepProgressIndicator` horizontal con los 4 estados
- Resaltar el paso actual y oscurecer los anteriores/futuros
- En el detalle del pedido mostrar timestamps de cada cambio de estado (si el backend los provee)
- Pull-to-refresh en la lista de pedidos activos

---

### 10. Admin: sin búsqueda en lista de pedidos
**Problema:** `AdminOrdersScreen` lista todos los pedidos pero no tiene búsqueda ni filtros por fecha. Con muchos pedidos es difícil encontrar uno específico.

**Archivos afectados:**
- `presentation/screens/admin/AdminOrdersScreen.kt`
- `presentation/viewmodels/AdminOrderViewModel.kt`

**Mejoras:**
- Agregar campo de búsqueda por ID de pedido o nombre de cliente
- Filtro por rango de fechas (hoy, esta semana, este mes)
- Ordenar por: más reciente, mayor monto
- En la tab "Estadísticas": agregar gráfico de barras por día/semana (usando Canvas API de Compose)

---

### 11. Cart: imágenes placeholder en vez de imagen real
**Problema:** `CartScreen` muestra un ícono de caja genérico para todos los items en lugar de la imagen del producto, que sí está disponible en el modelo `CartItem`.

**Archivos afectados:**
- `presentation/screens/cart/CartScreen.kt`

**Mejoras:**
- Usar `AsyncImage` con la URL de imagen del `menu_item` (requiere que el backend incluya `image_url` en el CartItem o se obtenga del menú)
- Mientras carga: mostrar skeleton/shimmer de 60×60dp
- Si no hay imagen: mostrar el inicial del nombre del producto en un círculo de color

---

### 12. Sin opción de reordenar desde historial
**Problema:** En el historial de pedidos no hay forma rápida de repetir un pedido anterior. El usuario debe volver al menú y buscar cada ítem manualmente.

**Archivos afectados:**
- `presentation/screens/orders/OrdersScreen.kt`
- `presentation/viewmodels/OrderViewModel.kt`

**Mejoras:**
- Botón "Repetir pedido" en cada card del historial
- Lógica: agregar todos los ítems al carrito + navegar a CartScreen
- Si algún ítem ya no está disponible: mostrar advertencia con lista de ítems no disponibles

---

### 13. Cantidad de items sin límite validado
**Problema:** En `MenuItemDetailScreen` y `CartScreen`, los botones `+` de cantidad no tienen límite superior. Un usuario puede agregar 9999 unidades sin ningún feedback.

**Archivos afectados:**
- `presentation/screens/menu/MenuItemDetailScreen.kt`
- `presentation/screens/cart/CartScreen.kt`

**Mejoras:**
- Definir límite razonable (ej: 99 por ítem)
- Al llegar al límite: deshabilitar botón `+` y mostrar tooltip o snackbar
- En el backend: agregar validación de cantidad máxima en el endpoint de cart

---

## PRIORIDAD BAJA — Polish y accesibilidad

### 14. Sin animaciones de transición entre pantallas
**Problema:** Las transiciones entre screens son instantáneas (fade por defecto de Navigation Compose), lo que hace la app sentirse menos pulida.

**Archivos afectados:**
- `presentation/navigation/AuthNavigation.kt`

**Mejoras:**
- Agregar `enterTransition` y `exitTransition` en el `NavHost`
- Sugerencia: `slideInHorizontally` + `fadeIn` para navegación hacia adelante
- `slideOutHorizontally` + `fadeOut` para navegación hacia atrás
- Transición más suave en BottomNav: `fadeIn`/`fadeOut`

---

### 15. Accesibilidad (a11y) básica faltante
**Problema:** Varios elementos interactivos carecen de `contentDescription`, lo que hace la app inutilizable con TalkBack.

**Archivos afectados:**
- `presentation/components/MenuComponents.kt` — botones de cantidad sin descripción
- `presentation/screens/cart/CartScreen.kt` — botones +/- sin contentDescription
- `presentation/screens/admin/AdminOrdersScreen.kt` — botones de acción de pedido

**Mejoras:**
- Agregar `contentDescription` a todos los `IconButton`
- Asegurar contraste de color mínimo WCAG AA (4.5:1) en texto sobre fondos de color
- Hacer los chips de categoría accesibles con estado seleccionado anunciado
- Tamaño mínimo de touch target: 48×48dp (verificar botones pequeños)

---

### 16. Formateo de fechas y precios inconsistente
**Problema:** Las fechas se formatean de forma diferente entre screens (`OrdersScreen` vs `OrderDetailScreen`). Los precios a veces muestran 2 decimales, a veces no.

**Archivos afectados:**
- `presentation/screens/orders/OrdersScreen.kt`
- `HomeScreen.kt` — detalle de pedido

**Mejoras:**
- Crear objeto utilitario `FormatUtils.kt` con funciones:
  - `formatDate(dateString: String): String` — formato consistente "dd MMM yyyy, HH:mm"
  - `formatPrice(amount: Double): String` — siempre 2 decimales con símbolo "$"
- Reemplazar todos los formateos inline por estas funciones

---

### 17. Admin: formulario de producto sin preview de imagen
**Problema:** En `AdminMenuScreen`, el campo "URL de imagen" es solo texto. No hay forma de verificar que la URL sea correcta sin guardar el producto primero.

**Archivos afectados:**
- `presentation/screens/admin/AdminMenuScreen.kt`

**Mejoras:**
- Al perder foco del campo URL: cargar preview de imagen con `AsyncImage`
- Si la URL es inválida o la imagen no carga: mostrar indicador de error en el campo
- Agrega opción de limpiar la URL con botón X

---

### 18. Dark mode sin probar correctamente
**Problema:** La app usa Material 3 Dynamic Color pero algunos colores hardcodeados (badges de estado en orders, cards de estadísticas) no se adaptan al dark theme.

**Archivos afectados:**
- `presentation/screens/orders/OrdersScreen.kt` — colores hardcodeados en status badges
- `presentation/screens/admin/AdminOrdersScreen.kt` — colores de estadísticas
- `ui/theme/Color.kt`

**Mejoras:**
- Mover los colores de estado de pedido a `Color.kt` como constantes semánticas
- Usar `MaterialTheme.colorScheme` en lugar de colores hardcodeados
- Crear colores de superficie adaptables para las cards de estadísticas

---

### 19. Pull-to-refresh faltante en pantallas de datos
**Problema:** Solo `AdminOrdersScreen` tiene botón de refresh manual. El resto de pantallas no tienen pull-to-refresh ni actualización automática.

**Archivos afectados:**
- `presentation/screens/menu/MenuScreen.kt`
- `presentation/screens/orders/OrdersScreen.kt`
- `presentation/screens/cart/CartScreen.kt`

**Mejoras:**
- Agregar `PullRefreshIndicator` (Material 3: `pullRefresh` modifier) en todas las listas
- Conectar al método `loadData()` / `refresh()` del ViewModel correspondiente
- Mostrar timestamp "Actualizado hace X minutos" en el header

---

### 20. Sin soporte offline ni caché local
**Problema:** Toda la data se obtiene en tiempo real. Si no hay conexión, la app muestra error y no tiene nada que mostrar.

**Archivos afectados:**
- Todos los repositorios en `data/repository/`
- `network/NetworkModule.kt`

**Mejoras (a largo plazo):**
- Agregar caché con `OkHttp Cache` para respuestas GET del menú
- Mostrar banner "Sin conexión - mostrando datos guardados" cuando está offline
- Guardar el último estado del carrito en DataStore como respaldo
- Indicador de conexión en el TopAppBar cuando no hay internet

---

## Resumen por Pantalla

| Pantalla | Issues críticos | Issues medios | Issues menores |
|----------|----------------|---------------|----------------|
| LoginScreen | Foco/teclado (#2) | Sin "Olvidé contraseña" | — |
| RegisterScreen | Foco/teclado (#2) | — | — |
| MenuScreen | Imagen fallback (#1), Feedback carrito (#5) | Búsqueda sin debounce (#7) | Animaciones (#14) |
| MenuItemDetail | Imagen fallback (#1), Límite cantidad (#13) | — | A11y (#15) |
| CartScreen | Imágenes placeholder (#11) | Reordenar (#12), Límite qty (#13) | Dark mode (#18) |
| OrdersScreen | Sesión expirada (#6) | Progress visual (#9), Reordenar (#12) | Pull-to-refresh (#19), Fechas (#16) |
| ProfileScreen | Sin edición (#8) | — | — |
| AdminCategoriesScreen | Sin confirm delete (#4), Foco (#2) | — | — |
| AdminMenuScreen | Sin confirm delete (#4), Imagen preview (#17) | Bulk actions | — |
| AdminOrdersScreen | Sin confirm cancel (#4) | Búsqueda/filtros (#10) | Estadísticas con gráficas (#10) |

---

## Quick Wins (menor esfuerzo, mayor impacto)

Orden sugerido para implementar primero:

1. **Snackbar al agregar al carrito** (#5) — ~1h, muy visible para el usuario
2. **Confirmación antes de eliminar** (#4) — ~2h, previene pérdida de datos
3. **Botón limpiar búsqueda** (#7) — ~30min, muy esperado por usuarios
4. **ImeAction en formularios** (#2) — ~2h, mejora flujo en todos los formularios
5. **Imagen fallback en cards** (#1) — ~1h, elimina espacios en blanco rotos
6. **contentDescription en iconos** (#15) — ~2h, a11y básica obligatoria
7. **FormatUtils centralizado** (#16) — ~1h, consistencia visual
8. **Pull-to-refresh** (#19) — ~3h, patrón estándar en apps móviles
