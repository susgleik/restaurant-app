plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.restaurant_app"
    compileSdk = 36  // CAMBIAR DE 36 A 35

    defaultConfig {
        applicationId = "com.example.restaurant_app"
        minSdk = 24
        targetSdk = 35  // CAMBIAR DE 36 A 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //Configuracion para URLs base
        buildConfigField("String", "BASE_URL", "\"https://restaurant-backend-x0sz.onrender.com/\"")
        buildConfigField("String", "API_VERSION", "\"api/v1/\"")
    }

    buildTypes {
        debug {
            isDebuggable = true
            buildConfigField("String", "BASE_URL", "\"https://restaurant-backend-x0sz.onrender.com/\"") // Para emulador
            buildConfigField("String", "BASE_URL", "\"https://restaurant-backend-x0sz.onrender.com/\"") // Para dispositivo físico
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"https://your-production-domain.com/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // AGREGADO: Material Icons Extended - Contiene TODOS los íconos
    implementation(libs.androidx.material.icons.extended)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Lifecycle y ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Networking - Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Serialización JSON
    implementation(libs.kotlinx.serialization.json)

    // Dependency Injection - Hilt (usando KSP en lugar de kapt)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    // ELIMINAR esta línea que da error:
    // implementation(libs.firebase.crashlytics.buildtools)
    ksp(libs.hilt.compiler)

    // DataStore para almacenar tokens
    implementation(libs.androidx.datastore.preferences)

    // Coil para cargar imágenes
    implementation(libs.coil.compose)

    // Acompañante para permisos y otros
    implementation(libs.accompanist.permissions)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}