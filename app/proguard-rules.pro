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

# --- kotlinx.serialization ---
# Standard rules from https://github.com/Kotlin/kotlinx.serialization#android
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.example.daypilot_test_desing.**$$serializer { *; }
-keepclassmembers class com.example.daypilot_test_desing.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.daypilot_test_desing.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Supabase-kt / Ktor ---
# Neither library ships consumer ProGuard rules, and both rely on
# kotlinx.serialization + reflection internally to decode Postgrest/Auth/
# Realtime/Storage responses. Keeping the whole namespace avoids stripping
# generated serializers we have no visibility into from the app side.
-keep class io.github.jan.supabase.** { *; }
-keepclassmembers class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.github.jan.supabase.**
-dontwarn io.ktor.**