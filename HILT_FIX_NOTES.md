# Hilt Configuration Fix for error.NonExistentClass

## Root Cause Analysis

The `error.NonExistentClass` occurs when Hilt's annotation processor (via KSP) cannot resolve dependencies during code generation. This typically happens when:

1. Dependencies are injected without proper providers
2. Missing compiler dependencies
3. Circular dependency references
4. Incorrect annotation processor configuration

## Applied Fixes

### 1. Added Missing Dependencies
- `androidx.core:core-splashscreen` - Required by MainActivity
- `androidx.hilt:hilt-compiler` - KSP processor for Hilt-WorkManager integration

### 2. Fixed Dependency Injection Setup
- Created explicit providers in AppModule for:
  - `TermuxFileProvider`
  - `FileOperationRepository`
- These were using @Inject constructors but Hilt needed explicit bindings

### 3. Resolved KSP Configuration
- Removed duplicate KSP declarations
- Ensured both Dagger Hilt and AndroidX Hilt compilers are included:
  ```kotlin
  ksp(libs.hilt.compiler)           // For Dagger Hilt
  ksp(libs.androidx.hilt.compiler)  // For Hilt-WorkManager
  ```

### 4. Created Worker Example
- Added ExampleWorker with @HiltWorker annotation
- This ensures HiltWorkerFactory has at least one worker to process

## Build Commands

```bash
# Clean everything
rm -rf .gradle build app/build

# Rebuild with fresh cache
./gradlew clean assembleDebug --no-build-cache

# If still failing, try:
./gradlew clean
./gradlew --stop
rm -rf ~/.gradle/caches/modules-2/files-2.1/com.google.dagger
./gradlew assembleDebug --refresh-dependencies
```

## Verification Checklist

- [ ] All @Inject fields have corresponding @Provides or @Binds
- [ ] No circular dependencies in injection graph
- [ ] Both hilt-compiler and androidx.hilt.compiler are in dependencies
- [ ] @HiltAndroidApp on Application class
- [ ] @AndroidEntryPoint on all Activities/Services using injection
- [ ] At least one @HiltWorker if using HiltWorkerFactory

## KSP vs KAPT

This project uses KSP (Kotlin Symbol Processing) for better performance:
- Faster builds
- Better incremental compilation
- Lower memory usage

Ensure all annotation processors use `ksp()` not `kapt()`.

## Version Compatibility

- Kotlin 2.0.21
- KSP 2.0.21-1.0.28 (must match Kotlin version)
- Hilt 2.52
- AndroidX Hilt 1.2.0