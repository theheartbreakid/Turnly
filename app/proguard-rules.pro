# Turnly Proguard / R8 Rules

# KotlinX Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class * implements androidx.room.migration.Migration
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}

# ZXing QR Code
-dontwarn com.google.zxing.**

# CameraX
-dontwarn androidx.camera.**
-dontwarn androidx.camera.core.impl.**

# Coil Image Loader
-dontwarn coil.**

# Kyant Backdrop & Shapes
-dontwarn io.github.kyant0.**

# Prismal Rendering Engine
-dontwarn com.github.styropyr0.prismal.**
-keepclasseswithmembernames class com.github.styropyr0.prismal.** {
    native <methods>;
}

# Android Architecture Components
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
