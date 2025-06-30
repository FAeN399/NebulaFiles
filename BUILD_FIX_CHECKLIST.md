# Kotlin Annotation Processing Error Prevention Checklist

## ✅ Fixed Issues

1. **Missing Hilt WorkManager Integration**
   - Added `androidx.hilt:hilt-work` dependency
   - Added `androidx.hilt:hilt-compiler` for KSP processing
   - Created WorkerModule for proper DI setup

2. **Missing Import in build.gradle.kts**
   - Added `import java.io.File` for File class usage

3. **Proper KSP Configuration**
   - Using KSP instead of KAPT for better performance
   - Ensuring all annotation processors use KSP

## 🛡️ Prevention Checklist

### When Adding Dependency Injection:
- [ ] Ensure all required Hilt modules are included
- [ ] Add both implementation and KSP compiler dependencies
- [ ] For WorkManager: include `hilt-work` and `hilt-compiler`
- [ ] Disable default WorkManager initializer in manifest
- [ ] Create proper @Module classes for all injected dependencies

### Build Configuration:
- [ ] Use consistent annotation processor (KSP vs KAPT)
- [ ] Match Kotlin version with KSP version
- [ ] Include all necessary imports in build scripts
- [ ] Clear build cache if switching processors: `./gradlew clean`

### Common Fixes for `error.NonExistentClass`:
1. **Sync dependencies**: File → Sync Project with Gradle Files
2. **Clean build**: `./gradlew clean build`
3. **Invalidate caches**: `rm -rf .gradle/caches/`
4. **Check for circular dependencies**
5. **Ensure all @Inject fields have providers**

### Version Compatibility:
- Kotlin 2.0.21 → KSP 2.0.21-1.0.28
- Hilt 2.52 → androidx.hilt 1.2.0
- WorkManager 2.10.0 → compatible with Hilt 1.2.0

## 🔧 Build Commands

```bash
# Clean and rebuild
./gradlew clean assembleDebug

# With more detailed output
./gradlew assembleDebug --stacktrace

# Force dependency refresh
./gradlew assembleDebug --refresh-dependencies
```

## 📋 Dependency Verification

Ensure these are in your `app/build.gradle.kts`:
```kotlin
implementation(libs.hilt.android)
ksp(libs.hilt.compiler)
implementation(libs.androidx.hilt.work)
ksp(libs.androidx.hilt.compiler)
```

And in `libs.versions.toml`:
```toml
androidx-hilt-work = { group = "androidx.hilt", name = "hilt-work", version = "1.2.0" }
androidx-hilt-compiler = { group = "androidx.hilt", name = "hilt-compiler", version = "1.2.0" }
```