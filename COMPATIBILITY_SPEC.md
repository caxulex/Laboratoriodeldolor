# Laboratoriodeldolor - Functional Compatibility Specification

> **Last Updated:** September 25, 2025  
> **Status:** ✅ VERIFIED WORKING  
> **Git Commit:** d615f64 (Post-modernization with Testing Infrastructure)

## 📋 Overview

This document defines the **EXACT** dependency versions and configurations that are **VERIFIED WORKING** for the Laboratoriodeldolor project. Any changes to these specifications must be tested thoroughly and this document updated accordingly.

## 🧪 Testing Infrastructure (COMPLETED ✅)

The project now includes comprehensive testing infrastructure following this compatibility specification:

### Testing Framework Components
- **Unit Testing**: JUnit 5 (5.8.2) with JUnit 4 backward compatibility (4.13.2)
- **Mocking**: MockK (1.13.8) for Kotlin-friendly mocking
- **Coroutines Testing**: kotlinx-coroutines-test (1.7.3) for async testing
- **Repository Testing**: Room testing (2.6.1) for database integration tests
- **Test Data Management**: Custom FakeRepositoryProvider and TestDataBuilder
- **Accessibility Testing**: Compose UI testing with semantic verification
- **Code Coverage**: Built-in Gradle test reporting

## 🎯 Core Principle

**Use these exact versions for stability.** Any deviation from these specifications should be done incrementally with full testing and rollback capability.

## 🔧 Verified Working Configuration

### Build Environment
```
Gradle Wrapper: 8.13.0
Android Gradle Plugin: 8.13.0  
Kotlin: 1.9.25
KSP: 1.9.0-1.0.13
CompileSdk: 35
TargetSdk: 35
MinSdk: 24
```

### Core Dependencies (libs.versions.toml)
```toml
[versions]
agp = "8.13.0"
kotlin = "1.9.25" 
ksp = "1.9.0-1.0.13"
coreKtx = "1.13.1"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.10.00"
room = "2.6.1"
lifecycleViewmodelKtx = "2.8.7"
lifecycleViewmodelCompose = "2.8.7"
navigationCompose = "2.8.4"
appcompat = "1.7.0"
coroutines = "1.7.3"
```

### Compose Configuration
```kotlin
// In app/build.gradle.kts
composeOptions {
    kotlinCompilerExtensionVersion = "1.5.15"  // Compatible with Kotlin 1.9.25
}
```

### Additional Working Dependencies
```kotlin
// Hardcoded versions that work with the above configuration
androidx-work = "2.10.0"
androidx-datastore-preferences = "1.1.1" 
airbnb-lottie-compose = "6.6.0"
mpandroidchart = "3.1.0"
lifecycle-runtime-compose = "2.7.0"
material-icons-extended = "1.6.8"
desugar_jdk_libs = "2.1.2"
```

## ⚠️ Known Incompatibilities 

### Hilt Dependency Injection
**Status:** ❌ PROBLEMATIC - DO NOT USE until resolved

**Issue:** JavaPoet version conflicts causing build failures
```
Error: 'java.lang.String com.squareup.javapoet.ClassName.canonicalName()'
```

**Tested Versions (All Failed):**
- Hilt 2.52 + Kotlin 1.9.25 ❌
- Hilt 2.51.1 + AGP 8.2.2 ❌  
- Hilt 2.48 + Kotlin 1.8.10 ❌
- Hilt 2.44 + AGP 8.0.2 ❌
- Hilt 2.50 + Various combinations ❌

**Recommendation:** Defer Hilt implementation. Use Repository Pattern with manual DI for now.

### Version Constraint Matrix

| Component | Version | Compatible With | Incompatible With |
|-----------|---------|----------------|-------------------|
| Kotlin | 1.9.25 | Compose Compiler 1.5.15 | Compose Compiler 1.5.8 |
| AGP | 8.13.0 | API 35, WorkManager 2.10.0 | API 36 (doesn't exist) |
| Compose BOM | 2024.10.00 | Kotlin 1.9.25 | Older Kotlin versions |
| WorkManager | 2.10.0 | API 35+ | API 34 and below |

## 🛡️ Safe Modernization Approach

### ✅ Low Risk Modernizations (Proceed First)
1. **Repository Pattern** - No version dependencies
2. **Accessibility Enhancements** - Framework features only  
3. **Testing Infrastructure** - Build on Repository Pattern
4. **Code Quality Improvements** - Lint rules, documentation

### ⚠️ Medium Risk Modernizations (Test Thoroughly)
1. **Build System Optimizations** - May affect compatibility
2. **Performance Monitoring** - Additional dependencies
3. **Advanced Compose Features** - Version-sensitive

### ❌ High Risk Modernizations (Defer)
1. **Hilt Dependency Injection** - Known compatibility issues
2. **Major Version Upgrades** - Complex compatibility matrix
3. **New Architecture Components** - Unproven compatibility

## 🔄 Update Protocol

When updating this specification:

1. **Create Checkpoint** - `git commit -m "checkpoint: Pre-[change] state"`
2. **Test Changes** - Verify full build + app functionality
3. **Update This File** - Document new working configuration
4. **Commit Changes** - Include updated spec in commit

### Testing Checklist
- [ ] `./gradlew clean build` succeeds
- [ ] App launches without crashes
- [ ] Core functionality works (mood tracking, pain map, exercises)
- [ ] No new lint errors introduced
- [ ] Existing tests pass

## 📁 Project Architecture (Current)

### Working Patterns
- **ViewModels** with manual factory pattern
- **Room Database** with KSP annotation processing
- **Jetpack Compose** with material3
- **Coroutines + StateFlow** for reactive data
- **Manual Dependency Injection** via Application class

### Dependencies Structure
```
MoodApplication (Manual DI)
├── AppDatabase (Room)
├── UserPreferencesRepository (DataStore)
├── ViewModelFactory classes (Manual)
└── ViewModels (Constructor injection)
```

## 🧪 Testing Infrastructure Details

### Test Structure (Implemented)
```
app/src/test/java/
├── com/example/laboratoriodeldolor/
│   ├── data/                    # Repository unit tests with MockK
│   ├── repository/              # Repository implementation tests
│   ├── testing/                 # Test utilities and builders
│   │   ├── FakeRepositoryProvider.kt   # Mock repository setup
│   │   ├── TestDataBuilder.kt          # Test data generation
│   │   └── TestUtils.kt                # Test helper functions
│   └── [existing test files]
```

### Test Dependencies (Verified Working)
```kotlin
// Unit Testing
testImplementation 'junit:junit:4.13.2'
testImplementation 'org.junit.jupiter:junit-jupiter:5.8.2'

// Mocking and Coroutines
testImplementation 'io.mockk:mockk:1.13.8'
testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3'

// Room Testing
testImplementation 'androidx.room:room-testing:2.6.1'

// Compose Testing (for accessibility tests)
testImplementation 'androidx.compose.ui:ui-test-junit4:2024.10.00'
```

### Running Tests
```bash
# Run all unit tests
./gradlew :app:testDebugUnitTest

# Run specific test class
./gradlew :app:testDebugUnitTest --tests="*MoodRepositoryImplUnitTest"

# Run tests with coverage
./gradlew :app:testDebugUnitTestCoverage
```

## 🎯 Completed Modernization Steps

**✅ Repository Pattern Implementation** (Commit: 382b776)
- ✅ Low risk (no version dependencies)
- ✅ High value (better separation of concerns)
- ✅ Foundation for future DI (when Hilt issues resolved)
- ✅ Improves testability

**✅ Accessibility Enhancement** (Commit: 5c97862)
- ✅ Jetpack Compose semantics integration
- ✅ Screen reader compatibility
- ✅ Accessibility testing framework

**✅ Testing Infrastructure** (Commit: d615f64)  
- ✅ Comprehensive unit testing setup
- ✅ MockK integration for repository mocking
- ✅ Test data builders and utilities
- ✅ JUnit 5 with backward compatibility

## 📞 Emergency Rollback

If builds break after changes:
```bash
# Back to latest working state (with Testing Infrastructure)
git reset --hard d615f64

# Back to pre-modernization baseline
git reset --hard df55c0b

# Always verify after rollback
./gradlew clean build
./gradlew :app:testDebugUnitTest
```

---

**⚠️ CRITICAL:** This specification is the source of truth for working configurations. Any deviation must be documented and tested thoroughly.