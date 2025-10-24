plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Serialization
    kotlin("plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.example.mirai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mirai"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // ========================================
    // CORE ANDROID
    // ========================================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ========================================
    // COMPOSE
    // ========================================
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.text)
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")

    // ========================================
    // NAVIGATION
    // ========================================
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ========================================
    // LIFECYCLE
    // ========================================
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // ========================================
    // SERIALIZATION
    // ========================================
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // ========================================
    // COIL - Para cargar imágenes ✅
    // Necesario para mostrar fotos en las entradas
    // ========================================
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ========================================
    // TESTING
    // ========================================
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // ========================================
    // DEBUG
    // ========================================
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}