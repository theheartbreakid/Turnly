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
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Coil Image Loader
-keep class coil.** { *; }
-dontwarn coil.**

# Kyant Backdrop & Shapes
-keep class io.github.kyant0.** { *; }
-dontwarn io.github.kyant0.**

# Prismal Rendering Engine
-keep class com.github.styropyr0.prismal.** { *; }
-dontwarn com.github.styropyr0.prismal.**

# Android Architecture Components
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
