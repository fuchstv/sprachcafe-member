# SprachCafé Member App ProGuard & R8 Configuration

# Preserve data models & API responses
-keep class org.sprachcafe.member.data.** { *; }
-keepclassmembers class org.sprachcafe.member.data.** { *; }
-keep class org.sprachcafe.member.ui.** { *; }

# Preserve ZXing QR Code generator
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Coroutines and standard reflection
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontwarn kotlinx.coroutines.**

# Compose runtime optimizations
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Google Play in-app update
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**
