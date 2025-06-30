# Preserve line number information for debugging
-keepattributes SourceFile,LineNumberTable

# Keep Google Drive API classes
-keep class com.google.api.** { *; }
-keep class com.google.auth.** { *; }
-dontwarn com.google.api.**
-dontwarn com.google.auth.**

# Keep data classes
-keep class com.nebula.files.data.model.** { *; }
-keep class com.nebula.files.domain.model.** { *; }

# Hilt - Critical for preventing NonExistentClass errors
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.Module class * { *; }
-keep @javax.inject.Singleton class * { *; }

# Keep all Hilt generated components
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep class **_HiltComponents { *; }
-keep class **_HiltComponents$* { *; }
-keep class **_Factory { *; }
-keep class **_Factory$* { *; }

# Keep injection targets
-keepclasseswithmembernames class * {
    @javax.inject.Inject <fields>;
}
-keepclasseswithmembernames class * {
    @javax.inject.Inject <init>(...);
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# WorkManager
-keep class androidx.work.** { *; }
-keep class androidx.hilt.work.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Compose
-keep class androidx.compose.** { *; }