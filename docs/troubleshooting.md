# Troubleshooting Guide

This guide helps you resolve common issues when working with this Android starter template.

## Build Errors

### Gradle Sync Failures

#### JDK Version Mismatch

**Error:**
```
Jetpack requires JDK 17+ but it is currently using JDK 11.
Java Home: [/path/to/jdk-11]
```

**Solution:**
1. Install JDK 21 (required by this template)
2. Configure Android Studio to use JDK 21:
   - **File → Project Structure → SDK Location → Gradle Settings**
   - Set **Gradle JDK** to version 21
3. Verify in `settings.gradle.kts`:
   ```kotlin
   check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17))
   ```

**References:**
- settings.gradle.kts:88-94
- [Android Studio JDK Configuration](https://developer.android.com/build/jdks#jdk-config-in-studio)

---

#### Repository Access Issues

**Error:**
```
Could not resolve com.google.firebase:firebase-bom:34.4.0
```

**Solution:**
1. Check `settings.gradle.kts` repository configuration:
   ```kotlin
   repositories {
       google {
           content {
               includeGroupByRegex("com\\.google.*")
           }
       }
       mavenCentral()
   }
   ```
2. Verify internet connection
3. Clear Gradle cache:
   ```bash
   ./gradlew clean --refresh-dependencies
   ```
4. Check if behind a corporate proxy (configure in `gradle.properties`)

**References:**
- settings.gradle.kts:32-44

---

#### Version Catalog Issues

**Error:**
```
Could not resolve libs.androidx.core.ktx
```

**Solution:**
1. Ensure `gradle/libs.versions.toml` exists and is valid
2. Check version catalog syntax:
   ```toml
   [libraries]
   androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "androidxCore" }
   ```
3. Verify version references exist in `[versions]` section
4. Sync project with Gradle files

**References:**
- gradle/libs.versions.toml

---

### KSP/Kapt Errors

#### Hilt Compilation Errors

**Error:**
```
[Dagger/MissingBinding] Cannot be provided without an @Inject constructor or an @Provides-annotated method
```

**Solution:**
1. Verify Hilt plugin is applied in module's `build.gradle.kts`:
   ```kotlin
   plugins {
       alias(libs.plugins.jetpack.dagger.hilt)
   }
   ```
2. Check if class is annotated properly:
   ```kotlin
   @HiltViewModel
   class MyViewModel @Inject constructor(...)
   ```
3. Ensure repository has `@Binds` or `@Provides` in a Hilt module
4. Clean and rebuild:
   ```bash
   ./gradlew clean build
   ```

**References:**
- build-logic/convention/src/main/kotlin/DaggerHiltConventionPlugin.kt
- See [Dependency Injection Guide](dependency-injection.md)

---

#### Room Database Compilation Errors

**Error:**
```
error: Cannot find setter for field
```

**Solution:**
1. Ensure entity class properties match DAO query column names
2. Add `@ColumnInfo` annotation if database column name differs:
   ```kotlin
   @Entity
   data class MyEntity(
       @ColumnInfo(name = "user_id") val userId: String
   )
   ```
3. Verify `@PrimaryKey` is present
4. Clean and rebuild project

**References:**
- core/room/src/main/kotlin/dev/atick/core/room/

---

### Dependency Resolution Issues

#### Duplicate Class Errors

**Error:**
```
Duplicate class kotlin.collections.CollectionsKt found in modules
```

**Solution:**
1. Check for conflicting dependency versions in `gradle/libs.versions.toml`
2. Use BOM (Bill of Materials) for consistent versioning:
   ```kotlin
   implementation(platform(libs.firebase.bom))
   ```
3. Exclude transitive dependencies if needed:
   ```kotlin
   implementation(libs.some.library) {
       exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
   }
   ```

**References:**
- gradle/libs.versions.toml
- build-logic/convention/src/main/kotlin/FirebaseConventionPlugin.kt:35

---

#### Configuration Cache Warnings

**Error:**
```
Configuration cache problems found in this build
```

**Solution:**
1. This is expected due to google-services plugin (see gradle.properties:28)
2. Warning mode is configured intentionally:
   ```properties
   org.gradle.configuration-cache.problems=warn
   ```
3. Build will complete successfully - these are warnings, not errors
4. Reference issue: [google/play-services-plugins#246](https://github.com/google/play-services-plugins/issues/246)

**References:**
- gradle.properties:24-28

---

## Runtime Errors

### Application Crashes on Startup

#### Firebase Initialization Failure

**Error (Logcat):**
```
java.lang.IllegalStateException: Default FirebaseApp is not initialized
```

**Solution:**
1. Verify `google-services.json` exists in `app/` directory
2. Check Firebase plugin is applied in `app/build.gradle.kts`:
   ```kotlin
   plugins {
       alias(libs.plugins.jetpack.firebase)
   }
   ```
3. Ensure `google-services` plugin is applied (happens automatically via convention plugin)
4. If using custom `google-services.json`:
   - Verify package name matches `applicationId` in `build.gradle.kts`
   - Check Firebase project configuration in Firebase Console

**References:**
- app/build.gradle.kts:30
- build-logic/convention/src/main/kotlin/FirebaseConventionPlugin.kt:30
- [Firebase Setup Guide](firebase.md)

---

#### Hilt Injection Failures

**Error (Logcat):**
```
java.lang.RuntimeException: Unable to create application:
java.lang.IllegalStateException: Hilt entry point not found
```

**Solution:**
1. Verify `Application` class is annotated with `@HiltAndroidApp`:
   ```kotlin
   @HiltAndroidApp
   class JetpackApplication : Application()
   ```
2. Check activities are annotated with `@AndroidEntryPoint`:
   ```kotlin
   @AndroidEntryPoint
   class MainActivity : ComponentActivity()
   ```
3. Ensure ViewModel uses `@HiltViewModel`:
   ```kotlin
   @HiltViewModel
   class MyViewModel @Inject constructor(...) : ViewModel()
   ```
4. Clean and rebuild project

**References:**
- app/src/main/kotlin/dev/atick/compose/JetpackApplication.kt
- app/src/main/kotlin/dev/atick/compose/ui/MainActivity.kt

---

### Navigation Errors

#### Navigation Destination Not Found

**Error (Logcat):**
```
java.lang.IllegalArgumentException: Navigation destination that matches request NavDeepLinkRequest cannot be found
```

**Solution:**
1. Verify destination is defined in navigation graph:
   ```kotlin
   @Serializable
   data object MyDestination

   fun NavGraphBuilder.myScreen(...) {
       composable<MyDestination> { ... }
   }
   ```
2. Ensure navigation graph is added to `NavHost`:
   ```kotlin
   NavHost(...) {
       myScreen(...)
   }
   ```
3. Check if using correct navigation route type
4. Verify nested graphs have correct start destination

**References:**
- app/src/main/kotlin/dev/atick/compose/navigation/
- [Navigation Deep Dive](navigation.md)

---

#### Navigation Argument Serialization Errors

**Error (Logcat):**
```
kotlinx.serialization.SerializationException: Serializer for class 'MyData' is not found
```

**Solution:**
1. Add `@Serializable` annotation to data class:
   ```kotlin
   @Serializable
   data class MyDestination(val id: String, val data: MyData)

   @Serializable
   data class MyData(val name: String)
   ```
2. Ensure Kotlin serialization plugin is applied:
   ```kotlin
   plugins {
       alias(libs.plugins.kotlin.serialization)
   }
   ```
3. For custom types, provide custom serializer

**References:**
- [Navigation Deep Dive](navigation.md#complex-types)

---

### State Management Issues

#### State Not Updating in UI

**Problem:**
UI doesn't reflect ViewModel state changes

**Solution:**
1. Ensure using `collectAsStateWithLifecycle()` in composables:
   ```kotlin
   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
   ```
2. Verify ViewModel uses `MutableStateFlow`:
   ```kotlin
   private val _uiState = MutableStateFlow(UiState(MyScreenData()))
   val uiState = _uiState.asStateFlow()
   ```
3. Use proper state update functions:
   ```kotlin
   // Synchronous updates
   _uiState.updateState { copy(name = newName) }

   // Async operations with Result<T>
   _uiState.updateStateWith { repository.getData() }

   // Async operations with Result<Unit>
   _uiState.updateWith { repository.saveData() }
   ```

**References:**
- core/ui/src/main/kotlin/dev/atick/core/ui/utils/StatefulComposable.kt
- [State Management Guide](state-management.md)

---

#### OneTimeEvent Not Consumed

**Problem:**
Error messages or navigation events trigger multiple times

**Solution:**
1. Use `OneTimeEvent` wrapper for single-consumption events:
   ```kotlin
   data class UiState<T>(
       val data: T,
       val error: OneTimeEvent<Throwable?> = OneTimeEvent(null)
   )
   ```
2. Consume event properly in UI:
   ```kotlin
   StatefulComposable(
       state = uiState,
       onShowSnackbar = onShowSnackbar
   ) { ... }
   ```
3. `StatefulComposable` handles event consumption automatically

**References:**
- core/android/src/main/kotlin/dev/atick/core/android/utils/OneTimeEvent.kt
- core/ui/src/main/kotlin/dev/atick/core/ui/utils/StatefulComposable.kt

---

## Firebase Issues

### Authentication Not Working

#### Google Sign-In Fails

**Error:**
```
com.google.android.gms.common.api.ApiException: 10:
```

**Solution:**
1. Add SHA-1 fingerprint to Firebase Console:
   ```bash
   # Get debug SHA-1
   ./gradlew signingReport
   ```
2. Copy SHA-1 from output under "Variant: debug, Config: debug"
3. Add to Firebase Console:
   - **Project Settings → Your apps → SHA certificate fingerprints**
4. Download new `google-services.json` and replace in `app/`
5. Rebuild and reinstall app

**References:**
- [Firebase Setup Guide](firebase.md#authentication-setup)
- firebase/auth/src/main/kotlin/dev/atick/firebase/auth/data/AuthDataSource.kt

---

#### Credential Manager Not Found

**Error (Logcat):**
```
CredentialManager is not available
```

**Solution:**
1. Ensure device/emulator runs Android 14+ or has Google Play Services
2. For devices below Android 14, add Jetpack library:
   ```kotlin
   implementation(libs.androidx.credentials)
   implementation(libs.credentials.play.services.auth)
   ```
   (Already included in template)
3. Verify Google Play Services is up-to-date on device

**References:**
- firebase/auth/src/main/kotlin/dev/atick/firebase/auth/data/AuthDataSource.kt
- gradle/libs.versions.toml:160-162

---

### Firestore Permission Denied

**Error (Logcat):**
```
PERMISSION_DENIED: Missing or insufficient permissions
```

**Solution:**
1. Check Firestore Security Rules in Firebase Console
2. For development, use permissive rules (⚠️ not for production):
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```
3. For production, implement proper security rules
4. Ensure user is authenticated before accessing Firestore

**References:**
- firebase/firestore/src/main/kotlin/dev/atick/firebase/firestore/data/FirebaseDataSource.kt
- [Firebase Setup Guide](firebase.md#firestore-setup)

---

## Code Quality Issues

### Spotless Formatting Errors

#### Copyright Header Missing

**Error:**
```
Step 'licenseHeaderFile' found problem in 'src/main/kotlin/MyFile.kt':
  License header mismatch
```

**Solution:**
1. Run Spotless Apply to auto-fix:
   ```bash
   ./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache
   ```
2. Manually add copyright header from `spotless/copyright.kt`:
   ```kotlin
   /*
    * Copyright 2023 Atick Faisal
    *
    * Licensed under the Apache License, Version 2.0 (the "License");
    * ...
    */
   ```
3. For custom copyright, modify files in `spotless/` directory

**References:**
- gradle/init.gradle.kts:47
- spotless/copyright.kt
- [Spotless Setup Guide](spotless.md)

---

#### Ktlint Violations

**Error:**
```
Step 'ktlint' found problem in 'MyFile.kt':
  Exceeded max line length (120)
```

**Solution:**
1. Run Spotless Apply to auto-fix most issues:
   ```bash
   ./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache
   ```
2. For line length violations, break lines appropriately:
   ```kotlin
   // Too long
   fun myFunction(param1: String, param2: String, param3: String, param4: String): Result<Data>

   // Fixed
   fun myFunction(
       param1: String,
       param2: String,
       param3: String,
       param4: String
   ): Result<Data>
   ```
3. For Compose-specific violations, follow custom rules from `io.nlopez.compose.rules:ktlint`

**References:**
- gradle/init.gradle.kts:38-46
- .editorconfig
- [Spotless Setup Guide](spotless.md)

---

#### CI Build Fails on Spotless Check

**Error (GitHub Actions):**
```
Task :spotlessCheck FAILED
```

**Solution:**
1. Run Spotless Check locally before pushing:
   ```bash
   ./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache
   ```
2. Fix issues with Spotless Apply:
   ```bash
   ./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache
   ```
3. Commit and push fixes
4. **Best Practice:** Set up pre-commit hook to run `spotlessApply`

**References:**
- .github/workflows/ci.yml:35-36
- [Spotless Setup Guide](spotless.md#ci-cd-integration)

---

## Development Environment Issues

### Android Studio Setup Problems

#### Compose Preview Not Working

**Problem:**
Compose previews don't render or show errors

**Solution:**
1. Ensure using Android Studio Hedgehog or newer
2. Enable Compose Preview:
   - **Settings → Experimental → Compose**
   - Enable "Live Edit of Literals"
3. Verify preview annotations are correct:
   ```kotlin
   @PreviewDevices
   @PreviewThemes
   @Composable
   private fun MyScreenPreview() {
       JetpackTheme {
           MyScreen(...)
       }
   }
   ```
4. Refresh preview (toolbar icon or Ctrl+Shift+F5)
5. If still failing, invalidate caches and restart

**References:**
- core/ui/src/main/kotlin/dev/atick/core/ui/utils/PreviewDevices.kt
- core/ui/src/main/kotlin/dev/atick/core/ui/utils/PreviewThemes.kt

---

#### Gradle Build Too Slow

**Problem:**
Gradle builds take too long

**Solution:**
1. Verify Gradle daemon settings in `gradle.properties`:
   ```properties
   org.gradle.jvmargs=-Xmx8g -XX:+HeapDumpOnOutOfMemoryError
   org.gradle.parallel=true
   org.gradle.caching=true
   org.gradle.configuration-cache=true
   ```
2. Enable build cache (already configured in template)
3. Use `--no-configuration-cache` flag only when necessary
4. Close unnecessary background processes
5. Consider increasing heap size in `gradle.properties` if you have more RAM

**References:**
- gradle.properties:10-28

---

#### KSP/Kapt Takes Too Long

**Problem:**
Annotation processing slow during builds

**Solution:**
1. Use KSP instead of Kapt (template already uses KSP for Hilt and Room)
2. Verify KSP is being used:
   ```kotlin
   dependencies {
       "ksp"(libs.dagger.hilt.compiler)  // Not "kapt"
   }
   ```
3. Increase Gradle heap size if needed
4. Close other IDEs/applications consuming memory

**References:**
- build-logic/convention/src/main/kotlin/DaggerHiltConventionPlugin.kt:35

---

### Emulator Issues

#### App Not Installing on Emulator

**Problem:**
Installation fails or emulator not detected

**Solution:**
1. Verify emulator is running:
   ```bash
   adb devices
   ```
2. If no devices listed, restart emulator
3. If multiple devices, specify target:
   ```bash
   ./gradlew installDebug -Pandroid.device=emulator-5554
   ```
4. Clear app data and reinstall:
   ```bash
   adb uninstall dev.atick.compose
   ./gradlew installDebug
   ```
5. Check min SDK version matches emulator API level (minSdk: 24)

**References:**
- gradle/libs.versions.toml:68

---

## Build Configuration Issues

### Release Build Problems

#### Keystore Not Found

**Error:**
```
keystore.properties file not found. Using debug key.
```

**Solution:**
1. This is expected for debug builds and template usage
2. For release builds, create `keystore.properties` in project root:
   ```properties
   storePassword=your-store-password
   keyPassword=your-key-password
   keyAlias=your-key-alias
   storeFile=your-keystore-file.jks
   ```
3. Generate keystore if needed:
   - Android Studio: **Build → Generate Signed Bundle/APK**
   - Or use command line:
     ```bash
     keytool -genkey -v -keystore release-keystore.jks \
       -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
     ```
4. Place keystore in `app/` directory

**References:**
- app/build.gradle.kts:25, 84-92
- [Getting Started Guide](getting-started.md#release-build-setup)

---

#### ProGuard/R8 Errors

**Error:**
```
Missing class com.google.firebase.FirebaseApp
```

**Solution:**
1. Add ProGuard rules in `app/proguard-rules.pro`:
   ```proguard
   -keep class com.google.firebase.** { *; }
   -keep class com.google.android.gms.** { *; }
   ```
2. For serialization issues, add:
   ```proguard
   -keepattributes *Annotation*, InnerClasses
   -dontnote kotlinx.serialization.AnnotationsKt
   ```
3. Test release builds thoroughly
4. Check R8 full mode documentation if using

**References:**
- app/proguard-rules.pro
- app/build.gradle.kts:94-97

---

## Data Layer Issues

### Repository Errors Not Handled

**Problem:**
Repository errors crash app instead of showing in UI

**Solution:**
1. Use `suspendRunCatching` in repositories:
   ```kotlin
   override suspend fun getData(): Result<Data> = suspendRunCatching {
       networkDataSource.getData()
   }
   ```
2. Use `updateStateWith` or `updateWith` in ViewModels:
   ```kotlin
   fun loadData() {
       _uiState.updateStateWith {
           repository.getData()
       }
   }
   ```
3. `StatefulComposable` will automatically show errors via snackbar

**References:**
- core/android/src/main/kotlin/dev/atick/core/android/utils/CoroutineUtils.kt
- [State Management Guide](state-management.md#error-handling)
- [Data Flow Guide](data-flow.md#error-handling)

---

### Room Database Migration Issues

**Error (Logcat):**
```
java.lang.IllegalStateException: Room cannot verify the data integrity
```

**Solution:**
1. For development, use destructive migration:
   ```kotlin
   Room.databaseBuilder(context, AppDatabase::class.java, "database-name")
       .fallbackToDestructiveMigration()  // Development only
       .build()
   ```
2. For production, implement proper migrations
3. Bump database version number when schema changes
4. Clear app data and reinstall for testing

**References:**
- core/room/src/main/kotlin/dev/atick/core/room/di/DatabaseModule.kt

---

## WorkManager Sync Issues

### Background Sync Not Running

**Problem:**
Sync operations don't execute

**Solution:**
1. Verify WorkManager is initialized in `Application.onCreate()`:
   ```kotlin
   @HiltAndroidApp
   class JetpackApplication : Application() {
       override fun onCreate() {
           super.onCreate()
           Sync.initialize(context = this)
       }
   }
   ```
2. Check WorkManager constraints are satisfied (network, battery, etc.)
3. Verify worker is using `@HiltWorker` and `@AssistedInject`:
   ```kotlin
   @HiltWorker
   class SyncWorker @AssistedInject constructor(
       @Assisted appContext: Context,
       @Assisted workerParams: WorkerParameters,
       ...
   ) : CoroutineWorker(appContext, workerParams)
   ```
4. Check logs for WorkManager errors:
   ```bash
   adb logcat -s WM-WorkerWrapper
   ```

**References:**
- sync/src/main/kotlin/dev/atick/sync/utils/Sync.kt
- sync/src/main/kotlin/dev/atick/sync/workers/SyncWorker.kt
- app/src/main/kotlin/dev/atick/compose/JetpackApplication.kt

---

## Memory Issues

### LeakCanary Detecting Leaks

**Problem:**
LeakCanary reports memory leaks

**Solution:**
1. Check ViewModel lifecycle - ensure not storing Activity/Context
2. Verify Flow collection uses lifecycle-aware collectors:
   ```kotlin
   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
   ```
3. Cancel coroutines properly in repositories
4. Don't hold references to composables in ViewModel
5. For known library leaks, suppress in LeakCanary config
6. Disable LeakCanary in release builds (already configured)

**References:**
- app/build.gradle.kts:138
- core/ui/src/main/kotlin/dev/atick/core/ui/extensions/LifecycleExtensions.kt

---

## Testing Issues

### Cannot Run Tests

**Problem:**
Test infrastructure not yet implemented

**Solution:**
1. Testing infrastructure is marked as **Upcoming 🚧** in this template
2. For now, manual testing is required
3. Future updates will include:
   - Unit test setup for ViewModels
   - Repository tests
   - UI tests with Compose Test
4. You can add your own testing framework following standard Android practices

**References:**
- docs/guide.md:343-351

---

## Getting Additional Help

If you encounter issues not covered in this guide:

1. **Check Related Guides:**
   - [Getting Started](getting-started.md) - Setup and initial configuration
   - [Architecture Overview](architecture.md) - Understanding the app structure
   - [State Management](state-management.md) - State-related issues
   - [Navigation Deep Dive](navigation.md) - Navigation problems
   - [Dependency Injection](dependency-injection.md) - DI issues
   - [Firebase Setup](firebase.md) - Firebase-specific problems
   - [Spotless Setup](spotless.md) - Code formatting issues

2. **Search GitHub Issues:**
   - Check existing issues: [GitHub Issues](https://github.com/atick-faisal/Jetpack-Android-Starter/issues)
   - Search closed issues for solutions

3. **Enable Debug Logging:**
   - Timber is included in this template
   - Add logging to identify issues:
     ```kotlin
     Timber.d("Debug message: $variable")
     Timber.e(throwable, "Error occurred")
     ```

4. **Clean Build:**
   - Often resolves mysterious build issues:
     ```bash
     ./gradlew clean
     ./gradlew build --refresh-dependencies
     ```

5. **Invalidate Caches:**
   - Android Studio: **File → Invalidate Caches / Restart**

6. **Report a Bug:**
   - If you've found a genuine issue with the template, please report it on GitHub with:
     - Android Studio version
     - Gradle version
     - Error logs
     - Steps to reproduce
