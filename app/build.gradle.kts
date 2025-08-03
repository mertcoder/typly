import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlinx.serialization)
    kotlin("kapt")
}

// Load local.properties file safely
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

// Helper function to get property with fallback
fun String.getLocalProperty(defaultValue: String = ""): String {
    return localProperties.getProperty(this) ?: defaultValue
}

android {
    namespace = "com.typly.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.typly.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Add NDK configuration for Agora
        ndk {
            abiFilters += listOf("x86_64","arm64-v8a", "armeabi-v7a","x86")
        }
        
        // Load sensitive configuration from local.properties with fallbacks
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${"GOOGLE_WEB_CLIENT_ID".getLocalProperty("YOUR_GOOGLE_CLIENT_ID_HERE")}\"")
        buildConfigField("String", "SECRET_KEY", "\"${"SECRET_KEY".getLocalProperty("DefaultSecretKey16")}\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "DEBUG_LOGS", "false")
            
            // Production values from local.properties  
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${"GOOGLE_WEB_CLIENT_ID".getLocalProperty("YOUR_GOOGLE_CLIENT_ID_HERE")}\"")
            buildConfigField("String", "SECRET_KEY", "\"${"SECRET_KEY".getLocalProperty("DefaultSecretKey16")}\"")

        }
        debug{
            buildConfigField("boolean", "DEBUG_LOGS", "true")
            
            // Debug values from local.properties
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${"GOOGLE_WEB_CLIENT_ID".getLocalProperty("YOUR_GOOGLE_CLIENT_ID_HERE")}\"")
            buildConfigField("String", "SECRET_KEY", "\"${"SECRET_KEY".getLocalProperty("DefaultSecretKey16")}\"")

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

    packagingOptions {
        pickFirst("**/libc++_shared.so")
        pickFirst("**/libjsc.so")
        pickFirst("**/libfbjni.so")
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

    // Material Icons Extended - provides more icon options
    implementation(libs.androidx.material.icons.extended)

    // Firebase & Google
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.googleid)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.firebase.messaging)
    kapt(libs.hilt.android.compiler)

    // Coil, Permissions, Serialization
    implementation(libs.coil.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.kotlinx.serialization.json)


    //Voice Chat
    implementation("io.agora.rtc:full-sdk:4.5.2")
    implementation(libs.easypermissions)


    // Test
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.0")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}