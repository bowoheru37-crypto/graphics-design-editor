# Optimization passes
-optimizationpasses 5
-verbose
-dontpreverify

# Native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# View constructors for inflation
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Callback interfaces
-keep class * implements android.content.ComponentCallbacks2 { *; }
-keep class * implements android.os.Parcelable { *; }

# R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Gson
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Application classes
-keep class com.canvastyle.editor.** { *; }
-keep interface com.canvastyle.editor.** { *; }

# AndroidX
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Material Design
-keep class com.google.android.material.** { *; }
-keep interface com.google.android.material.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Kotlin (if used)
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Security
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** println(...);
}

# Performance
-repackageclasses 'com.canvastyle.editor.opt'
-allowaccessmodification
-overloadaggressively

# Keep source file names for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
