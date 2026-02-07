# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============================================================
# COMPREHENSIVE PROGUARD RULES FOR WELLTRACK ANDROID APP
# ============================================================

# Keep debug information
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes *Annotation*

# ============================================================
# ROOM DATABASE RULES
# ============================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.paging.**

# Keep Room entity classes
-keep class com.beaconledger.welltrack.data.model.** { *; }
-keep class com.beaconledger.welltrack.data.compliance.** { *; }

# Keep Room DAOs
-keep interface com.beaconledger.welltrack.data.database.dao.** { *; }

# Keep Room Database
-keep class com.beaconledger.welltrack.data.database.WellTrackDatabase { *; }
-keep class com.beaconledger.welltrack.data.database.Converters { *; }

# Room migration classes
-keep class com.beaconledger.welltrack.data.database.migrations.** { *; }

# ============================================================
# HILT/DAGGER RULES
# ============================================================
-dontwarn com.google.errorprone.annotations.*

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent

# Keep Hilt modules and components
-keep @dagger.hilt.InstallIn class *
-keep @dagger.Module class *
-keep @dagger.Component interface *
-keep @dagger.Subcomponent interface *

# Keep classes that use Hilt injection
-keepclasseswithmembers class * {
    @dagger.hilt.android.qualifiers.* <fields>;
}

# Keep Hilt entry points
-keep @dagger.hilt.android.AndroidEntryPoint class * {
    <init>(...);
}

# ============================================================
# SUPABASE SDK RULES
# ============================================================
-keep class io.github.jan.supabase.** { *; }
-keep class kotlinx.serialization.** { *; }

# Keep Supabase models
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Keep classes used by Supabase serialization
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# ============================================================
# KTOR HTTP CLIENT RULES
# ============================================================
-keep class io.ktor.** { *; }
-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.**

# ============================================================
# ML KIT RULES
# ============================================================
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.common.** { *; }

# Keep ML Kit model classes
-keepclassmembers class * extends com.google.mlkit.common.model.** {
    <init>(...);
}

# ============================================================
# RETROFIT/OKHTTP RULES
# ============================================================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ============================================================
# GSON RULES
# ============================================================
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Keep generic signature of TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep all model classes for Gson
-keep class com.beaconledger.welltrack.data.model.** { <fields>; }
-keep class com.beaconledger.welltrack.data.compliance.** { <fields>; }

# ============================================================
# ANDROIDX RULES
# ============================================================
# Keep lifecycle components
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# Health Connect (API 36 compatible)
-keep class androidx.health.connect.client.** { *; }
-keep class androidx.health.connect.client.records.** { *; }
-keep class androidx.health.connect.client.permission.** { *; }
-keep class androidx.health.connect.client.request.** { *; }
-keep class androidx.health.connect.client.response.** { *; }
-dontwarn androidx.health.connect.client.**

# Biometric
-keep class androidx.biometric.** { *; }

# Security Crypto
-keep class androidx.security.crypto.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.InputMerger
-keep class androidx.work.impl.background.systemalarm.RescheduleReceiver

# CameraX
-keep class androidx.camera.** { *; }

# ============================================================
# COMPOSE RULES
# ============================================================
-keep class androidx.compose.** { *; }
-keep @androidx.compose.runtime.Stable class *
-keep class kotlin.coroutines.Continuation

# ============================================================
# KOTLIN COROUTINES RULES
# ============================================================
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ============================================================
# COIL IMAGE LOADING RULES
# ============================================================
-keep class coil.** { *; }
-keep interface coil.** { *; }

# ============================================================
# JSOUP RULES
# ============================================================
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# ============================================================
# ACCOMPANIST RULES
# ============================================================
-keep class com.google.accompanist.** { *; }

# ============================================================
# WELLTRACK APP SPECIFIC RULES
# ============================================================
# Keep all ViewModel classes
-keep class com.beaconledger.welltrack.presentation.**.viewmodel.** { *; }
-keep class com.beaconledger.welltrack.presentation.**.*ViewModel { *; }

# Keep all Repository implementations
-keep class com.beaconledger.welltrack.data.repository.**Impl { *; }

# Keep all Use Case classes
-keep class com.beaconledger.welltrack.domain.usecase.** { *; }

# Keep all managers and services
-keep class com.beaconledger.welltrack.data.**.manager.** { *; }
-keep class com.beaconledger.welltrack.data.**.service.** { *; }

# Keep navigation classes
-keep class com.beaconledger.welltrack.presentation.navigation.** { *; }

# Keep security classes
-keep class com.beaconledger.welltrack.data.security.** { *; }

# Keep compliance classes
-keep class com.beaconledger.welltrack.data.compliance.** { *; }

# Keep enum classes
-keep enum com.beaconledger.welltrack.** { *; }

# Keep data classes
-keep @kotlinx.parcelize.Parcelize class * { *; }

# ============================================================
# HEALTH APP SPECIFIC RULES
# ============================================================
# Keep health data models
-keep class com.beaconledger.welltrack.data.model.HealthMetric { *; }
-keep class com.beaconledger.welltrack.data.model.Biomarker { *; }
-keep class com.beaconledger.welltrack.data.model.Supplement { *; }

# Keep health managers
-keep class com.beaconledger.welltrack.data.health.** { *; }

# Keep external platform integrations
-keep class com.beaconledger.welltrack.integration.** { *; }

# ============================================================
# ACCESSIBILITY RULES
# ============================================================
-keep class com.beaconledger.welltrack.accessibility.** { *; }

# ============================================================
# PERFORMANCE MONITORING RULES
# ============================================================
-keep class com.beaconledger.welltrack.monitoring.** { *; }
-keep class com.beaconledger.welltrack.optimization.** { *; }

# ============================================================
# GENERAL ANDROID RULES
# ============================================================
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep setters in Views so that animations can still work.
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# Keep Activity subclasses
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep members of R class
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ============================================================
# DEBUG RULES
# ============================================================
# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove debug code
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}