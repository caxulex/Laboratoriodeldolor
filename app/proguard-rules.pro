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