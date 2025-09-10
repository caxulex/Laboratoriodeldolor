pluginManagement {
    repositories {
    // Plugin portal (default) and explicit plugins Maven endpoint for environments
    // that prefer direct maven access.
    gradlePluginPortal()
    maven("https://plugins.gradle.org/m2/")
    google()
    mavenCentral()
    maven("https://jitpack.io")
    }
    // Force a known-good KSP plugin module if requested; some environments
    // may attempt to resolve an unavailable patch version (1.0.14) so map
    // it to the catalog-supported version (1.9.0-1.0.11).
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.devtools.ksp") {
                // The correct Gradle module for the KSP Gradle plugin is
                // 'com.google.devtools.ksp:symbol-processing-gradle-plugin:<version>'
                // (not com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:...).
                useModule("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.9.10-1.0.14")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
    // Ensure plugin artifacts and build-time deps can be resolved
    maven("https://plugins.gradle.org/m2/")
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://jitpack.io")
    }
}
rootProject.name = "LaboratorioDelDolor"
include(":app")