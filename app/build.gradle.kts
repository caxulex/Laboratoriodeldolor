plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.googleKsp)
}

android {
    namespace = "com.example.laboratoriodeldolor"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.laboratoriodeldolor"
        minSdk = 24
    targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            // These are placeholders. Replace with your release keystore and secure credentials.
            storeFile = file("keystore/release.keystore")
            storePassword = "changeit"
            keyAlias = "release_key"
            keyPassword = "changeit"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use safe access to the signing config created above
            signingConfig = signingConfigs["release"]
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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

    // Google Fonts (Compose) - used to load Nunito Sans and Inter at runtime
    implementation("androidx.compose.ui:ui-text-google-fonts:1.5.1")
    // Optional: Google Play fonts provider (used by the runtime font loader on devices)
    implementation("com.google.android.gms:play-services-base:18.2.0")

    // DataStore Preferences (for simple key-value persistence)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Lottie for Compose
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // (Removed accompanist shared-element due to resolution issues.)

    // Rive: removed unresolved dependency; add correct Rive runtime when available

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.2")

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // WorkManager for background reminders
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    // MPAndroidChart for mood history line chart (JitPack tag-style coordinate)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}