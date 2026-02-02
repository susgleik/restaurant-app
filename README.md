# Restaurant Management App 🍽️

Aplicación móvil Android para gestión de restaurantes con interfaz dual: experiencia para clientes y panel administrativo completo. Desarrollada con Jetpack Compose y arquitectura MVVM siguiendo principios de Clean Architecture.

## ✨ Características Principales

### Para Clientes 👥
- 📱 Menú digital interactivo con búsqueda y filtros
- 🛒 Carrito de compras con cálculo automático de totales
- 📋 Sistema de pedidos con seguimiento en tiempo real
- 📊 Historial completo de pedidos
- 👤 Gestión de perfil

### Para Administradores 🔧
- 📊 Dashboard con estadísticas y métricas en tiempo real
- 🗂️ CRUD completo de categorías y productos
- 📦 Control avanzado de pedidos con actualización de estados
- 📈 Análisis de ingresos y rendimiento
- 👨‍🍳 Dashboard de cocina optimizado

## 🛠️ Stack Tecnológico

### Lenguaje
- **Kotlin 1.9.22** - Lenguaje principal
- **Android SDK** - minSdk 24 | targetSdk 35

### UI & Design
- **Jetpack Compose** - UI declarativa moderna
- **Material Design 3** - Sistema de diseño
- **Navigation Compose** - Navegación type-safe
- **Coil** - Carga de imágenes

### Arquitectura
- **MVVM** - Patrón arquitectónico
- **Clean Architecture** - Separación por capas
- **Dagger Hilt** - Inyección de dependencias
- **Repository Pattern** - Abstracción de datos

### Networking
- **Retrofit 2.9.0** - Cliente HTTP
- **OkHttp** - Interceptores y logging
- **Kotlinx Serialization** - Serialización JSON

### Asincronía & Estado
- **Kotlin Coroutines** - Operaciones asíncronas
- **Flow & StateFlow** - Programación reactiva
- **DataStore** - Persistencia local

## 📂 Estructura del Proyecto

```
app/src/main/java/com/example/restaurant_app/
├── data/              # Capa de datos
│   ├── local/         # DataStore (tokens, sesiones)
│   ├── models/        # Modelos de datos (DTOs)
│   ├── remote/        # API Services (Retrofit)
│   └── repository/    # Repositorios
├── network/           # Configuración de red (Hilt, Interceptors)
├── presentation/      # Capa de presentación
│   ├── screens/       # Pantallas (Composables)
│   ├── viewmodels/    # ViewModels
│   ├── components/    # Componentes reutilizables
│   └── navigation/    # Sistema de navegación
└── ui/theme/          # Tema Material Design 3
```

## 🚀 Instalación

### Requisitos
- Android Studio Hedgehog | 2023.1.1+
- JDK 11+
- Android SDK (minSdk 24)

### Pasos

1. **Clonar el repositorio**
```bash
git clone <repository-url>
cd restaurant-app
```

2. **Abrir en Android Studio**
   - File → Open → Seleccionar la carpeta del proyecto

3. **Sincronizar dependencias**
   - Android Studio sincronizará automáticamente Gradle
   - O ejecutar: `./gradlew build`

4. **Ejecutar**
   - Conectar dispositivo/emulador
   - Run (Shift + F10)

## 🔧 Configuración

### Backend API
- **Producción**: `https://restaurant-backend-x0sz.onrender.com/api/v1/`
- **Autenticación**: JWT Bearer Token

Para desarrollo local con Docker:
```kotlin
// app/build.gradle.kts
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")
```

## 📱 Sistema de Roles

| Rol | Permisos |
|-----|----------|
| **CLIENT** | Ver menú, carrito, pedidos propios, perfil |
| **ADMIN_STAFF** | CRUD completo, todos los pedidos, estadísticas, dashboard de cocina |

La navegación se adapta automáticamente según el rol del usuario.

## 📸 Screenshots

[Agregar screenshots de la aplicación]

## 📚 Documentación

Para información técnica detallada sobre la arquitectura, patrones de diseño y flujos de datos, consultar:

📖 **[ARCHITECTURE.md](./ARCHITECTURE.md)** - Documentación técnica completa

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## 🤝 Contribuir

1. Fork el proyecto
2. Crear rama feature (`git checkout -b feature/NuevaFeature`)
3. Commit cambios (`git commit -m 'Add: nueva feature'`)
4. Push a la rama (`git push origin feature/NuevaFeature`)
5. Abrir Pull Request

## 📝 Licencia

[Especificar licencia]

## 📧 Contacto

[Información de contacto]

---

<div align="center">

**Desarrollado con ❤️ usando Kotlin & Jetpack Compose**

[⭐ Star este proyecto](https://github.com/yourusername/restaurant-app) si te resultó útil

</div>
