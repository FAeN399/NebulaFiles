# Hilt Build Failure Deep Diagnostic Protocol

## Core Hypothesis
The `error.NonExistentClass` is a symptom of Hilt's annotation processor failing to generate necessary classes due to a fundamental initialization order conflict between WorkManager and Hilt's dependency graph.

## Diagnostic Questions to Answer

### 1. Initialization Order Analysis
- Is WorkManager trying to initialize before Hilt is ready?
- Does the manifest merger warning indicate conflicting initialization strategies?
- Are we mixing manual WorkManager configuration with Hilt's automatic setup?

### 2. Dependency Graph Integrity
- Why does FileOperationRepository consistently fail injection?
- Is there a hidden circular dependency through WorkManager → Service → Repository?
- Are all required Hilt entry points properly annotated?

### 3. Code Generation Verification
- Are the Hilt-generated classes actually being created in build/generated?
- Is KSP running in the correct order relative to other processors?
- Are there conflicting annotation processors?

### 4. Manifest Configuration Conflicts
- Does the WorkManagerInitializer provider conflict with Hilt's initialization?
- Are there duplicate provider declarations?
- Is the manifest merger creating invalid configurations?

## Investigation Steps

1. **Check Generated Code**
   - Verify existence of: `app/build/generated/ksp/debug/java/com/nebula/files/*_HiltComponents.java`
   - Look for Dagger component generation failures

2. **Trace Initialization Path**
   - WorkManager initialization in Application class
   - Service lifecycle vs Hilt injection timing
   - Provider declaration conflicts

3. **Validate Annotation Usage**
   - Ensure all entry points have correct annotations
   - Verify no missing @AndroidEntryPoint annotations
   - Check for proper @HiltWorker usage

4. **Examine Build Configuration**
   - KSP task dependencies
   - Annotation processor ordering
   - Potential conflicts between androidx.hilt and dagger.hilt

## Expected Root Causes

1. **Double Initialization**: WorkManager being initialized both manually and via Hilt
2. **Missing Entry Points**: Services or Activities lacking proper Hilt annotations
3. **Incompatible Versions**: Mismatch between Hilt, AndroidX Hilt, and WorkManager
4. **Manifest Conflicts**: Provider declarations conflicting with Hilt's expectations