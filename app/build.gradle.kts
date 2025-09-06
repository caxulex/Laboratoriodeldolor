import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    // (KSP removed — using KAPT instead)
    // Enable KAPT for annotation processing fallback if needed
}

// Apply KAPT via the legacy apply-style so Gradle will use the Kotlin plugin's
// existing classpath/version instead of trying to resolve a second version
// (avoids the "plugin is already on the classpath with an unknown version" error).
apply(plugin = "org.jetbrains.kotlin.kapt")

android {
    namespace = "com.example.laboratoriodeldolor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.laboratoriodeldolor"
        minSdk = 24
    targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Load signing properties from key.properties at the project root (do NOT commit your real file).
    val keystorePropsFile = rootProject.file("key.properties")
    val keystoreProperties = Properties()
    if (keystorePropsFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropsFile))
    } else {
        // No key.properties found locally. The build will still work with the placeholder keystore
        // file path, but you must create a local key.properties for real signing.
    }

    signingConfigs {
        create("release") {
            val storeFileProp = keystoreProperties.getProperty("storeFile")
            val storePasswordProp = keystoreProperties.getProperty("storePassword")
            val keyAliasProp = keystoreProperties.getProperty("keyAlias")
            val keyPasswordProp = keystoreProperties.getProperty("keyPassword")

            storeFile = if (storeFileProp != null) file(storeFileProp) else file("keystore/release.keystore")
            storePassword = storePasswordProp ?: "changeit"
            keyAlias = keyAliasProp ?: "release_key"
            keyPassword = keyPasswordProp ?: "changeit"
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
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    // Use explicit add("kapt", ...) so the script doesn't require the kapt extension at compile-time.
    add("kapt", libs.room.compiler)

    // Other libraries
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.airbnb.lottie.compose)
    implementation(libs.mpandroidchart)
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    // Core library desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // Local unit test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)

    // Android instrumented test dependencies
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
    androidTestImplementation(libs.androidx.test.core)

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