plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    // Temporarily revert to KAPT due to KSP plugin resolution failures. Once KSP version mismatch is resolved,
    // you can switch back to the googleKsp alias.
    id("org.jetbrains.kotlin.kapt")
}

import java.util.Properties
import java.io.FileInputStream

android {
    namespace = "com.example.laboratoriodeldolor"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.laboratoriodeldolor"
        minSdk = 24
    targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        // Package only Spanish resources to enforce Spanish-only UI
        resourceConfigurations.add("es")
    }

    // Load signing properties from key.properties at the project root (do NOT commit your real file).
    val keystorePropsFile = rootProject.file("key.properties")
    val keystoreProperties = Properties()
    if (keystorePropsFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropsFile))
    } else {
        // No key.properties found locally. The build will still work using the debug signing config
        // for local release builds, but you must create a local key.properties for real Play uploads.
    }

    signingConfigs {
        create("release") {
            val storeFileProp = keystoreProperties.getProperty("storeFile")
            val storePasswordProp = keystoreProperties.getProperty("storePassword")
            val keyAliasProp = keystoreProperties.getProperty("keyAlias")
            val keyPasswordProp = keystoreProperties.getProperty("keyPassword")

            // Use provided keystore path if present; otherwise, fall back to a repo-root keystore path.
            // Note: file(...) is relative to the module; use rootProject.file(...) for repo-root paths.
            val fallbackStore = rootProject.file("keystore/release.keystore")
            storeFile = if (storeFileProp != null) file(storeFileProp) else fallbackStore
            storePassword = storePasswordProp ?: "changeit"
            keyAlias = keyAliasProp ?: "release_key"
            keyPassword = keyPasswordProp ?: "changeit"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            isMinifyEnabled = true
            // Remove unused resources in release to reduce APK/AAB size
            isShrinkResources = true
            // Do not crunch PNGs; some source PNGs may be incompatible with the cruncher and fail AAPT compile
            // AAB/App Delivery will handle optimized delivery; prefer webp manually if needed
            isCrunchPngs = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Allow forcing debug signing for release via -PuseDebugSigningForRelease=true
            val forceDebugSigning = project.findProperty("useDebugSigningForRelease")?.toString()?.toBoolean() == true
            // Use release signing ONLY if a proper key.properties is present and the referenced keystore exists.
            val storeFileProp = keystoreProperties.getProperty("storeFile")
            val hasReleaseKeystore = keystorePropsFile.exists() &&
                !storeFileProp.isNullOrBlank() &&
                rootProject.file(storeFileProp).exists()
            signingConfig = if (!forceDebugSigning && hasReleaseKeystore) {
                signingConfigs["release"]
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        // Java 8 compatibility for source and target, and enable core library desugaring
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
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
    // Module-level lint baseline: setting this in the module DSL ensures AGP's
    // updateLintBaseline tasks see the baseline path at configuration time.
    // We point at the CLI-generated baseline file inside the app module so the
    // updater can update it without overwriting your manual baseline file.
    lint {
        baseline = file("lint-baseline.generated.xml")
    }
    // (lintOptions removed) Use the modern `lint { baseline = file(...) }` DSL only.
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Do NOT configure legacy APK splits when producing App Bundles. Google Play will handle dynamic delivery.
}

// Debug helper: when running updateLintBaseline tasks, print the configured
// android.lint.baseline property and whether the repo baseline file exists.
// This helps confirm what the task sees at execution time.
tasks.matching { it.name.startsWith("updateLintBaseline") }.configureEach {
    doFirst {
    logger.lifecycle(">>> DEBUG: project property android.lint.baseline = ${project.findProperty("android.lint.baseline")}")
    logger.lifecycle(">>> DEBUG: rootProject lint-baseline exists = ${rootProject.file("lint-baseline.xml").exists()}")
    logger.lifecycle(">>> DEBUG: module-local app/lint-baseline exists = ${file("lint-baseline.xml").exists()}")
    // Append execution-time info to the lint debug file
    val f = buildDir.resolve("lint-debug.txt")
    f.appendText("EXEC: project property android.lint.baseline=${project.findProperty("android.lint.baseline")}\n")
    f.appendText("EXEC: rootProject lint-baseline exists=${rootProject.file("lint-baseline.xml").exists()}\n")
    f.appendText("EXEC: module-local app/lint-baseline exists=${file("lint-baseline.xml").exists()}\n")
    }
}

// Configuration-time debug: print the discovered property and file existence
// so we can see what AGP sees during the configuration phase (earlier than task execution).
logger.lifecycle(">>> CONFIG-DEBUG: project property android.lint.baseline = ${project.findProperty("android.lint.baseline")}")
logger.lifecycle(">>> CONFIG-DEBUG: rootProject lint-baseline exists = ${rootProject.file("lint-baseline.xml").exists()}")
logger.lifecycle(">>> CONFIG-DEBUG: module-local app/lint-baseline exists = ${file("lint-baseline.xml").exists()}")

// Also write the same information to a file under the module build folder so we
// can inspect it regardless of Gradle console filtering.
val lintDebugFile = buildDir.resolve("lint-debug.txt")
lintDebugFile.parentFile.mkdirs()
lintDebugFile.writeText("CONFIG: project property android.lint.baseline=${project.findProperty("android.lint.baseline")}\n")
lintDebugFile.appendText("CONFIG: rootProject lint-baseline exists=${rootProject.file("lint-baseline.xml").exists()}\n")
lintDebugFile.appendText("CONFIG: module-local app/lint-baseline exists=${file("lint-baseline.xml").exists()}\n")

dependencies {
    // Core App Dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Lifecycle-aware Compose helpers (collectAsStateWithLifecycle)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    // Annotation processing via KAPT (temporary fallback until KSP plugin version issue fixed)
    kapt(libs.room.compiler)

    // Navigation, Work, DataStore, Lottie, and Chart dependencies
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.airbnb.lottie.compose)
    implementation(libs.mpandroidchart)

    // Material icons extended for MenuBook and BarChart
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    // Enable core library desugaring dependency to match compileOptions.isCoreLibraryDesugaringEnabled
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // Local Unit Tests (testImplementation)
    testImplementation(libs.junit)
    // Provide ApplicationProvider and testing utilities for local unit tests
    testImplementation("androidx.test:core:1.5.0")
    // Turbine for Flow testing helpers (awaitItem, cancelAndIgnoreRemainingEvents)
    testImplementation("app.cash.turbine:turbine:0.12.3")
    // Keep kotlinx-coroutines-test aligned with the runtime coroutines version declared in the catalog
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    // Instrumentation Tests (androidTestImplementation)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.ui.automator)
    androidTestImplementation(libs.kotlinx.coroutines.core)
    androidTestImplementation(libs.kotlinx.coroutines.android)

    // Fallback explicit coordinates for known testing artifacts (ensures availability if catalog alias resolution fails)
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    // Use the version defined in the version catalog for uiautomator (avoid 2.4.0 which may not be resolvable in some environments)
    // androidTestImplementation("androidx.test.uiautomator:uiautomator:2.4.0")

    // Debug Dependencies
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Temporary helper task: invoke Lint main via resolved dependency classpath to create/update baseline
// Usage: ./gradlew :app:writeLintBaselineCli -PbaselineFile=lint-baseline.xml
val writeLintBaselineCli by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Run Lint Main via resolved artifact classpath to (re)create baseline file"
    val baseline = project.findProperty("baselineFile")?.toString() ?: "lint-baseline.xml"
    // Create a detached configuration to resolve lint runtime jars
    val cfg = configurations.detachedConfiguration(
        dependencies.create("com.android.tools.lint:lint:31.13.1"),
        dependencies.create("org.jetbrains.kotlin:kotlin-stdlib:1.9.25")
    )
    classpath = cfg
    mainClass.set("com.android.tools.lint.Main")
    // Ensure we point at project root dir for analysis and baseline writing
    args = listOf("--create-baseline", "--baseline", baseline, projectDir.absolutePath)
    // copy stdout/stderr to console
    isIgnoreExitValue = false
}

// New convenience task: run Lint Main and write a generated baseline without overwriting the
// module's existing `lint-baseline.xml`. Produces `lint-baseline.generated.xml` in the module
// directory. Usage: ./gradlew :app:generateLintBaseline
val generateLintBaseline by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Generate a lint baseline file (lint-baseline.generated.xml) using resolved lint artifact"
    // Output baseline file inside module so AGP/Gradle tasks can see it easily
    val outBaseline = file("lint-baseline.generated.xml")
    // Resolve lint and kotlin runtime dependencies via a detached configuration
    val cfg = configurations.detachedConfiguration(
        dependencies.create("com.android.tools.lint:lint:31.13.1"),
        dependencies.create("org.jetbrains.kotlin:kotlin-stdlib:1.9.25")
    )
    classpath = cfg
    mainClass.set("com.android.tools.lint.Main")
    // Ensure Main writes the baseline for the module directory
    args = listOf("--create-baseline", "--baseline", outBaseline.absolutePath, projectDir.absolutePath)
    // Fail build if lint exits non-zero
    isIgnoreExitValue = false
}
// Also register a named task to ensure the task is addressable from the CLI
tasks.register("generateLintBaselineCli", JavaExec::class) {
    group = "verification"
    description = "CLI-visible alias to generate lint-baseline.generated.xml using resolved lint artifact"
    val outBaseline = file("lint-baseline.generated.xml")
    val cfg = configurations.detachedConfiguration(
        dependencies.create("com.android.tools.lint:lint:31.12.2"),
        dependencies.create("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    )
    classpath = cfg
    mainClass.set("com.android.tools.lint.Main")
    args = listOf("--create-baseline", "--baseline", outBaseline.absolutePath, projectDir.absolutePath)
    isIgnoreExitValue = false
}
// Replace eager tasks.create with lazy registration to avoid deprecated APIs
tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Eager CLI-visible alias to generate lint-baseline.generated.xml"
    val outBaseline = file("lint-baseline.generated.xml")
    val cfg = configurations.detachedConfiguration(
        dependencies.create("com.android.tools.lint:lint:31.13.1"),
        dependencies.create("org.jetbrains.kotlin:kotlin-stdlib:1.9.25")
    )
    classpath = cfg
    mainClass.set("com.android.tools.lint.Main")
    args = listOf("--create-baseline", "--baseline", outBaseline.absolutePath, projectDir.absolutePath)
    isIgnoreExitValue = false
}

// Pre-resource-merge cleanup: remove any stray placeholder files with extension .removed
// that cause aapt/resource merger failures ("file name must end with .xml or .png").
tasks.matching { it.name.matches(Regex("merge.*Resources")) }.configureEach {
    doFirst {
        val removed = fileTree("src/main/res") { include("**/*.removed") }.files
        if (removed.isNotEmpty()) {
            logger.lifecycle("[resource-cleanup] Deleting ${removed.size} '*.removed' placeholder resource file(s)...")
            removed.forEach { f ->
                if (!f.delete()) {
                    logger.warn("[resource-cleanup] Failed to delete ${f.absolutePath}; please remove manually.")
                }
            }
        }
    }
}