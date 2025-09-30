# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Room and lifecycle-related classes
-keep class androidx.room.** { *; }
-keepclassmembers class * {
	@androidx.room.* <methods>;
}
-keepclassmembers class kotlinx.coroutines.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }
# Keep app model/data classes
-keep class com.example.laboratoriodeldolor.** { *; }

# Keep Lottie model objects (reflection-based JSON -> model mapping)
-keep class com.airbnb.lottie.** { *; }

# Keep Google Fonts runtime reflection classes
-keep class androidx.compose.ui.text.googlefonts.** { *; }

# Keep Gson/JSON model classes if present (conservative)
-keep class com.google.gson.** { *; }

# Keep resources referenced by reflection
-keepclassmembers class * {
	@com.google.gson.annotations.SerializedName <fields>;
}

# Keep annotations (Room, Parcelable, etc.)
-keepattributes *Annotation*

# ===== Q-CLINIC INTEGRATION PROGUARD RULES =====

# Keep QClinic classes and models
-keep class com.laboratoriodeldolor.qclinic.** { *; }
-keep class com.example.laboratoriodeldolor.qclinic.** { *; }

# Keep Retrofit and Gson for API communication
-keep class retrofit2.** { *; }
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep security-crypto classes for secure token storage
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }

# Keep ErrorProne annotations to fix R8 minification issues
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.errorprone.annotations.** { *; }

# Keep OkHttp and interceptors
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Keep JWT and authentication classes
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep audio recording MediaRecorder related classes
-keep class android.media.MediaRecorder { *; }
-keep class android.media.MediaPlayer { *; }

# Keep coroutines for async operations
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep reflection-based JSON model classes
-keepclassmembers class * {
    @com.google.gson.annotations.Expose <fields>;
}

# Ensure no warnings from missing ErrorProne classes
-dontwarn com.google.errorprone.**

# Keep Google API client classes (used by security-crypto)
-dontwarn com.google.api.client.**
-keep class com.google.api.client.** { *; }

# Keep Joda Time classes (used by security-crypto)
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }

# Keep Google HTTP client classes
-dontwarn com.google.api.client.http.**
-keep class com.google.api.client.http.** { *; }

# Additional warnings suppressions for Tink crypto library
-dontwarn javax.annotation.**
-dontwarn java.lang.ClassValue
-dontwarn org.conscrypt.**