import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.1.0"
}

android {
    // Load local.properties for sensitive keys (API keys, etc.)
    val localPropertiesFile = rootProject.file("local.properties")
    val localProperties = Properties().apply {
        if (localPropertiesFile.exists()) {
            load(FileInputStream(localPropertiesFile))
        }
    }

    namespace = "com.bluegenie.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bluegenie.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 44
        versionName = "44.0.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // AI API Keys (will be set from local.properties for security)
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\""
        )

        // Stability AI API Key for music generation (Stable Audio)
        buildConfigField(
            "String",
            "STABILITY_API_KEY",
            "\"${project.findProperty("STABILITY_API_KEY") ?: "your-stability-api-key-here"}\""
        )

        // Replicate API Key for music generation (MusicGen)
        buildConfigField(
            "String",
            "REPLICATE_API_KEY",
            "\"${localProperties.getProperty("REPLICATE_API_KEY", "your-replicate-api-key-here")}\""
        )

        // Suno API Key for premium music generation
        buildConfigField(
            "String",
            "SUNO_API_KEY",
            "\"${localProperties.getProperty("SUNO_API_KEY", "")}\""
        )

        // Groq API Key for LLM responses
        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"${localProperties.getProperty("GROQ_API_KEY", "")}\""
        )

        // Google OAuth Client IDs
        val googleClientId = localProperties.getProperty("GOOGLE_CLIENT_ID", "")
        val googleWebClientId = localProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: googleClientId

        // Android Client ID - for Google Play Services
        buildConfigField(
            "String",
            "GOOGLE_CLIENT_ID",
            "\"$googleClientId\""
        )
        
        // Web Client ID - for Supabase ID token authentication
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"$googleWebClientId\""
        )

        // Supabase Configuration
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${localProperties.getProperty("SUPABASE_URL", "")}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${localProperties.getProperty("SUPABASE_ANON_KEY", "")}\""
        )

        // Stripe Configuration
        buildConfigField(
            "String",
            "STRIPE_PUBLISHABLE_KEY",
            "\"${localProperties.getProperty("STRIPE_PUBLISHABLE_KEY", "")}\""
        )
        
        // Web app URL for Stripe checkout
        buildConfigField(
            "String",
            "WEB_APP_URL",
            "\"${localProperties.getProperty("WEB_APP_URL", "https://bluegeniemagic.com")}\""
        )
    }

    signingConfigs {
        create("release") {
            // These will be loaded from keystore.properties file
            // You'll create this file after generating your keystore
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))

                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            // applicationIdSuffix = ".debug"  // Comment out - must match Google OAuth package name
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    // Activity Compose
    implementation(libs.activity.compose)

    // ViewModel Compose
    implementation(libs.lifecycle.viewmodel.compose)

    // Navigation Compose
    implementation(libs.navigation.compose)

    // Google AI Client SDK for Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Google Auth for Vertex AI
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")
    implementation("com.google.auth:google-auth-library-credentials:1.23.0")

    // Supabase for user management and subscription tracking
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.3")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.3")
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Image loading
    implementation(libs.coil.compose)
    implementation("io.coil-kt:coil-gif:2.7.0")

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}