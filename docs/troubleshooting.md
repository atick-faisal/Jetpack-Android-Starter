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
4. Reference
   issue: [google/play-services-plugins#246](https://github.com/google/play-services-plugins/issues/246)

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

#### Backstack Issues

**Problem:**
Unexpected backstack behavior (duplicate screens, can't go back, wrong screen when pressing back)

**Solution:**

1. For "pop to specific destination", use `popUpTo` with `inclusive`:
   ```kotlin
   navController.navigate(Home) {
       popUpTo(Login) { inclusive = true }  // Remove Login from backstack
   }
   ```
2. For "single instance" screens (like Home), use `launchSingleTop`:
   ```kotlin
   navController.navigate(Home) {
       launchSingleTop = true
       restoreState = true
   }
   ```
3. For bottom navigation, use proper state restoration:
   ```kotlin
   navController.navigate(destination) {
       popUpTo(navController.graph.findStartDestination().id) {
           saveState = true
       }
       launchSingleTop = true
       restoreState = true
   }
   ```
4. To clear entire backstack and start fresh:
   ```kotlin
   navController.navigate(Home) {
       popUpTo(0) { inclusive = true }  // Clear all backstack
   }
   ```
5. Debug backstack state:
   ```kotlin
   Timber.d("Backstack: ${navController.currentBackStack.value.map { it.destination.route }}")
   ```

**References:**

- [Navigation Deep Dive](navigation.md#navigation-options)
- app/src/main/kotlin/dev/atick/compose/navigation/TopLevelNavigation.kt

---

#### Nested Navigation Graph Issues

**Problem:**
Nested graphs not working, start destination errors, or can't navigate to nested destinations

**Solution:**

1. Ensure nested graph has explicit start destination:
   ```kotlin
   @Serializable
   data object AuthNavGraph

   @Serializable
   data object Login

   fun NavGraphBuilder.authNavGraph() {
       navigation<AuthNavGraph>(startDestination = Login) {  // Must specify startDestination
           composable<Login> { LoginRoute(...) }
           composable<Register> { RegisterRoute(...) }
       }
   }
   ```
2. Navigate to nested graph's start destination (not the graph itself):
   ```kotlin
   // Wrong - can't navigate to graph
   navController.navigate(AuthNavGraph)

   // Correct - navigate to start destination
   navController.navigate(Login)
   ```
3. For deep links into nested graphs, ensure route hierarchy is correct:
   ```kotlin
   // Nested graph route hierarchy: AuthNavGraph > Login
   composable<Login>(
       deepLinks = listOf(navDeepLink<Login>(basePath = "app://auth/login"))
   ) { ... }
   ```
4. When popping from nested graph, pop to parent graph's destination:
   ```kotlin
   navController.navigate(Home) {
       popUpTo(AuthNavGraph) { inclusive = true }  // Remove entire auth flow
   }
   ```
5. Check if parent NavHost includes nested graph:
   ```kotlin
   NavHost(...) {
       authNavGraph()  // Must be called to register nested graph
       homeNavGraph()
   }
   ```

**References:**

- [Navigation Deep Dive](navigation.md#nested-navigation)
- app/src/main/kotlin/dev/atick/compose/navigation/JetpackNavHost.kt

---

#### Navigation Arguments Not Received

**Problem:**
Arguments passed to destination are null or have default values instead of passed values

**Solution:**

1. Ensure destination parameter names match navigation arguments:
   ```kotlin
   @Serializable
   data class Profile(val userId: String)  // Parameter name must match

   // In composable
   composable<Profile> { backStackEntry ->
       val profile: Profile = backStackEntry.toRoute()
       ProfileRoute(userId = profile.userId)  // Use parameter from route
   }
   ```
2. Verify arguments are passed when navigating:
   ```kotlin
   // Wrong - missing argument
   navController.navigate(Profile(""))

   // Correct - pass actual argument
   navController.navigate(Profile(userId = currentUserId))
   ```
3. For optional arguments, use nullable types or default values:
   ```kotlin
   @Serializable
   data class Profile(
       val userId: String,
       val tab: String? = null  // Optional argument with default
   )
   ```
4. Check for serialization issues (see "Navigation Argument Serialization Errors" above)
5. Debug arguments:
   ```kotlin
   composable<Profile> { backStackEntry ->
       val profile: Profile = backStackEntry.toRoute()
       Timber.d("Received userId: ${profile.userId}")
       ProfileRoute(userId = profile.userId)
   }
   ```

**References:**

- [Navigation Deep Dive](navigation.md#passing-arguments)

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

#### Multiple Loading States Simultaneously

**Problem:**
Multiple loading indicators showing at once or loading state stuck

**Solution:**

1. Use single `UiState<T>` wrapper per screen (not multiple):
   ```kotlin
   // Wrong - multiple loading states
   data class ScreenData(
       val data1Loading: Boolean,
       val data2Loading: Boolean
   )

   // Correct - single loading state in UiState wrapper
   data class ScreenData(
       val data1: List<Item>,
       val data2: List<Item>
   )
   // UiState<ScreenData> has single loading field
   ```
2. If truly need multiple loading states, manage them explicitly:
   ```kotlin
   data class ScreenData(
       val items: List<Item> = emptyList(),
       val isRefreshing: Boolean = false  // Separate from main loading
   )
   ```
3. Ensure `updateStateWith` completes properly (sets loading = false)
4. Check for exception swallowing that prevents loading state reset

**References:**

- core/ui/src/main/kotlin/dev/atick/core/ui/utils/UiState.kt
- [State Management Guide](state-management.md#handling-multiple-async-operations)

---

#### updateStateWith Not Working

**Problem:**
`updateStateWith` or `updateWith` doesn't update state or shows compilation error

**Solution:**

1. Ensure Kotlin context parameters feature is enabled (already configured):
   ```kotlin
   // In build.gradle.kts
   freeCompilerArgs += "-Xcontext-receivers"
   ```
2. Verify you're calling from ViewModel (context parameters require ViewModel scope):
   ```kotlin
   @HiltViewModel
   class MyViewModel @Inject constructor() : ViewModel() {
       fun loadData() {
           _uiState.updateStateWith {  // Has implicit access to viewModelScope
               repository.getData()
           }
       }
   }
   ```
3. For `updateStateWith`, repository must return `Result<T>`:
   ```kotlin
   // Repository
   override suspend fun getData(): Result<Data> = suspendRunCatching { ... }
   ```
4. For `updateWith`, repository must return `Result<Unit>`:
   ```kotlin
   // Repository
   override suspend fun saveData(): Result<Unit> = suspendRunCatching { ... }
   ```
5. If still issues, use explicit `viewModelScope.launch` as fallback

**References:**

- core/ui/src/main/kotlin/dev/atick/core/ui/utils/StateFlowExtensions.kt
- [State Management Guide](state-management.md#kotlin-context-parameters)

---

### Lifecycle Issues

#### Compose Recomposition Not Triggering

**Problem:**
UI doesn't update even though state has changed

**Solution:**

1. Ensure using `collectAsStateWithLifecycle()` instead of `collectAsState()`:
   ```kotlin
   // Wrong - doesn't respect lifecycle
   val uiState by viewModel.uiState.collectAsState()

   // Correct - lifecycle-aware collection
   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
   ```
2. Verify state is immutable data class with `copy()` for updates:
   ```kotlin
   data class ScreenData(val name: String)

   // Updates must create new instance
   _uiState.updateState { copy(name = newName) }
   ```
3. Check if accidentally mutating state instead of replacing it
4. Ensure `StateFlow` is being used, not `Flow`

**References:**

- core/ui/src/main/kotlin/dev/atick/core/ui/extensions/LifecycleExtensions.kt
- feature/home/src/main/kotlin/dev/atick/feature/home/ui/home/HomeScreen.kt:68

---

#### ViewModel Outliving Composable

**Problem:**
ViewModel continues executing after screen is destroyed

**Solution:**

1. Always use `viewModelScope` for coroutines in ViewModel:
   ```kotlin
   fun loadData() {
       viewModelScope.launch {
           // Automatically cancelled when ViewModel is cleared
       }
   }
   ```
2. For background operations, use `updateStateWith` (uses context parameters):
   ```kotlin
   fun loadData() {
       _uiState.updateStateWith {  // Auto-uses viewModelScope
           repository.getData()
       }
   }
   ```
3. Never launch coroutines with `GlobalScope`
4. Verify ViewModel is scoped to navigation destination, not activity

**References:**

- core/ui/src/main/kotlin/dev/atick/core/ui/utils/StateFlowExtensions.kt
- [State Management Guide](state-management.md#kotlin-context-parameters)

---

#### Snackbar Showing After Navigation

**Problem:**
Error snackbar appears after user has navigated away

**Solution:**

1. This is expected behavior when using `StatefulComposable`
2. To prevent, handle navigation before error occurs:
   ```kotlin
   fun saveAndNavigate() {
       viewModelScope.launch {
           repository.save().onSuccess {
               navigator.navigate(NextScreen)  // Navigate before error can trigger
           }.onFailure {
               _uiState.updateState { copy(error = OneTimeEvent(it)) }
           }
       }
   }
   ```
3. Or consume errors before navigation:
   ```kotlin
   // In composable
   LaunchedEffect(saveSuccess) {
       if (saveSuccess) {
           navigator.navigate(NextScreen)
       }
   }
   ```
4. Consider using navigation with result pattern if needed

**References:**

- core/ui/src/main/kotlin/dev/atick/core/ui/utils/StatefulComposable.kt
- core/android/src/main/kotlin/dev/atick/core/android/utils/OneTimeEvent.kt

---

#### Activity Recreated on Configuration Change

**Problem:**
App state lost during rotation or configuration change

**Solution:**

1. ViewModels survive configuration changes automatically
2. Ensure state is in ViewModel, not composable:
   ```kotlin
   // Wrong - state lost on rotation
   @Composable
   fun MyScreen() {
       var name by remember { mutableStateOf("") }
   }

   // Correct - state survives rotation
   @HiltViewModel
   class MyViewModel @Inject constructor() : ViewModel() {
       private val _uiState = MutableStateFlow(UiState(ScreenData()))
       val uiState = _uiState.asStateFlow()
   }
   ```
3. For non-ViewModel state that should persist, use `rememberSaveable`:
   ```kotlin
   var searchQuery by rememberSaveable { mutableStateOf("") }
   ```
4. Complex objects need custom Saver implementation

**References:**

- [State Management Guide](state-management.md)
- feature/home/src/main/kotlin/dev/atick/feature/home/ui/home/HomeViewModel.kt

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

- [Firebase Setup Guide](firebase.md)
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
- [Firebase Setup Guide](firebase.md#firestore-security-rules)

---

### Firebase Analytics Not Tracking

**Problem:**
Events not appearing in Firebase Analytics console

**Solution:**

1. Verify Firebase Analytics is initialized (happens automatically with Firebase SDK)
2. Check if Analytics logging is enabled:
   ```kotlin
   // In debug builds, enable verbose logging
   Firebase.analytics.setAnalyticsCollectionEnabled(true)
   ```
3. For debug testing, enable debug mode via ADB:
   ```bash
   # Enable Analytics debug mode
   adb shell setprop debug.firebase.analytics.app dev.atick.compose

   # View events in real-time
   adb logcat -s FA
   ```
4. Check if events are being logged correctly:
   ```kotlin
   Firebase.analytics.logEvent("button_click") {
       param("button_name", "login")
       param("screen_name", "auth")
   }
   ```
5. Events may take 24 hours to appear in console (use DebugView for immediate feedback)
6. Verify `google-services.json` has correct Analytics project configuration

**References:**

- firebase/analytics/src/main/kotlin/dev/atick/firebase/analytics/AnalyticsLogger.kt
- [Firebase Setup Guide](firebase.md#firebase-analytics)

---

### Crashlytics Not Reporting

**Problem:**
Crashes not appearing in Firebase Crashlytics console

**Solution:**

1. Ensure Crashlytics is enabled in `build.gradle.kts`:
   ```kotlin
   buildTypes {
       release {
           firebaseCrashlytics {
               mappingFileUploadEnabled = true
           }
       }
   }
   ```
2. Verify Firebase Crashlytics plugin is applied (happens via `FirebaseConventionPlugin`)
3. Check if Crashlytics is initialized:
   ```kotlin
   // Crashlytics initializes automatically with Firebase SDK
   Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
   ```
4. For testing, force a crash:
   ```kotlin
   Firebase.crashlytics.log("Test crash triggered")
   throw RuntimeException("Test crash")
   ```
5. Crashes may take a few minutes to appear in console
6. For release builds, ensure ProGuard mapping files are uploaded:
   ```bash
   ./gradlew assembleRelease
   # Mapping files automatically uploaded if mappingFileUploadEnabled = true
   ```
7. Check logcat for Crashlytics errors:
   ```bash
   adb logcat -s FirebaseCrashlytics
   ```

**References:**

- firebase/analytics/src/main/kotlin/dev/atick/firebase/analytics/AnalyticsLogger.kt
- build-logic/convention/src/main/kotlin/FirebaseConventionPlugin.kt
- [Firebase Setup Guide](firebase.md)

---

### Firebase Initialization Failures

**Problem:**
Firebase not initializing properly, causing crashes or missing functionality

**Solution:**

1. See detailed Firebase initialization troubleshooting in **Runtime Errors → Application Crashes on
   Startup → Firebase Initialization Failure** (line 195)
2. Quick checklist:
    - ✅ `google-services.json` exists in `app/` directory
    - ✅ Firebase plugin applied via convention plugin
    - ✅ Package name matches `applicationId`
    - ✅ Firebase project properly configured in console
3. For emulator testing, use Firebase Emulator Suite:
   ```bash
   firebase emulators:start
   ```
4. Check Firebase SDK versions in `gradle/libs.versions.toml`:
   ```toml
   [versions]
   firebase-bom = "34.4.0"
   ```

**References:**

- app/build.gradle.kts:30
- build-logic/convention/src/main/kotlin/FirebaseConventionPlugin.kt
- [Firebase Setup Guide](firebase.md)
- See also: **Runtime Errors → Firebase Initialization Failure** (line 195)

---

## Compose Issues

### Recomposition Issues

#### Composable Not Recomposing

**Problem:**
UI doesn't update when state changes

**Solution:**

1. Ensure using `collectAsStateWithLifecycle()` for Flow collection:
   ```kotlin
   // Wrong - doesn't observe lifecycle
   val uiState by viewModel.uiState.collectAsState()

   // Correct - lifecycle-aware
   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
   ```
2. Verify state is immutable and creates new instances:
   ```kotlin
   // Wrong - mutating state
   data.items.add(newItem)

   // Correct - creating new instance
   _uiState.updateState { copy(items = items + newItem) }
   ```
3. Check if using `remember` correctly:
   ```kotlin
   // Wrong - doesn't recompose on state change
   val items = remember { mutableStateListOf<Item>() }

   // Correct - observes ViewModel state
   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
   ```
4. For derived state, use `derivedStateOf`:
   ```kotlin
   val filteredItems by remember {
       derivedStateOf {
           items.filter { it.isActive }
       }
   }
   ```

**References:**

- core/ui/src/main/kotlin/dev/atick/core/ui/extensions/LifecycleExtensions.kt
- [State Management Guide](state-management.md)
- See also: **Lifecycle Issues → Compose Recomposition Not Triggering** (line 613)

---

#### Excessive Recomposition

**Problem:**
UI stutters or battery drains due to too frequent recomposition

**Solution:**

1. Use stable parameters in composables:
   ```kotlin
   // Wrong - lambda recreated on every recomposition
   Button(onClick = { viewModel.loadData() })

   // Correct - stable reference
   Button(onClick = viewModel::loadData)
   ```
2. Mark data classes as stable when appropriate:
   ```kotlin
   @Immutable
   data class ScreenData(val items: List<Item>)
   ```
3. Use `key` parameter in lists to prevent unnecessary recomposition:
   ```kotlin
   LazyColumn {
       items(items = jetpacks, key = { it.id }) { jetpack ->
           JetpackCard(jetpack)
       }
   }
   ```
4. Use `derivedStateOf` for computed values:
   ```kotlin
   val filteredItems by remember {
       derivedStateOf {
           items.filter { it.isActive }
       }
   }
   ```
5. Avoid reading state that doesn't affect UI:
   ```kotlin
   // Wrong - unnecessary recomposition on timestamp change
   Text("Updated: ${state.lastUpdateTimestamp}")

   // Better - only show meaningful state
   Text("Items: ${state.items.size}")
   ```
6. Use Layout Inspector to identify recomposition hotspots:
    - Android Studio → View → Tool Windows → Layout Inspector
    - Enable "Show Recomposition Counts"

**References:**

- [Performance Guide](performance.md#recomposition-optimization)
- See also: **Memory Issues → Compose Recomposing Too Often** (line 1295)

---

### Compose Preview Issues

#### Previews Not Rendering

**Problem:**
Compose previews don't render or show errors

**Solution:**

1. Ensure using Android Studio Hedgehog (2023.1.1) or newer
2. Enable Compose Preview features:
    - **Settings → Experimental → Compose**
    - Enable "Live Edit of Literals"
3. Verify preview annotations are correct:
   ```kotlin
   @PreviewDevices
   @PreviewThemes
   @Composable
   private fun HomeScreenPreview() {
       JetpackTheme {
           HomeScreen(
               screenData = HomeScreenData(),
               onAction = {}
           )
       }
   }
   ```
4. Ensure preview composables are private (not public)
5. Refresh preview (toolbar icon or Ctrl+Shift+F5 / Cmd+Shift+F5)
6. If still failing, try:
    - Build → Refresh All Previews
    - File → Invalidate Caches / Restart
    - Clean and rebuild project

**References:**

- core/ui/src/main/kotlin/dev/atick/core/ui/utils/PreviewDevices.kt
- core/ui/src/main/kotlin/dev/atick/core/ui/utils/PreviewThemes.kt
- See also: **Development Environment Issues → Compose Preview Not Working** (line 1020)

---

#### Preview Shows Wrong Theme

**Problem:**
Preview doesn't reflect light/dark theme correctly

**Solution:**

1. Use `@PreviewThemes` annotation (includes both light and dark):
   ```kotlin
   @PreviewThemes
   @Composable
   private fun MyScreenPreview() {
       JetpackTheme {  // Theme wrapper required
           MyScreen(...)
       }
   }
   ```
2. For manual theme control, use `uiMode` parameter:
   ```kotlin
   @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
   @Composable
   private fun MyScreenDarkPreview() {
       JetpackTheme {
           MyScreen(...)
       }
   }
   ```
3. Always wrap preview content in `JetpackTheme { }`
4. Check if using correct `@PreviewThemes` annotation:
   ```kotlin
   // Correct - custom multi-preview annotation
   @PreviewThemes  // Defined in core/ui

   // Not - standard Preview
   @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
   ```

**References:**

- core/ui/src/main/kotlin/dev/atick/core/ui/utils/PreviewThemes.kt
- core/ui/src/main/kotlin/dev/atick/core/ui/theme/Theme.kt

---

#### Preview Shows Hardcoded Data Instead of Real State

**Problem:**
Preview shows placeholder data, not actual ViewModel state

**Solution:**

1. This is expected behavior - previews should use fake data
2. For preview data, create sample data objects:
   ```kotlin
   @PreviewDevices
   @PreviewThemes
   @Composable
   private fun HomeScreenPreview() {
       JetpackTheme {
           HomeScreen(
               screenData = HomeScreenData(
                   items = listOf(
                       Item(id = "1", name = "Preview Item 1"),
                       Item(id = "2", name = "Preview Item 2")
                   )
               ),
               onAction = {}  // No-op for preview
           )
       }
   }
   ```
3. For complex preview data, create preview data factories:
   ```kotlin
   object PreviewData {
       val sampleItems = listOf(
           Item(id = "1", name = "Item 1"),
           Item(id = "2", name = "Item 2")
       )
   }

   @PreviewThemes
   @Composable
   private fun HomeScreenPreview() {
       JetpackTheme {
           HomeScreen(
               screenData = HomeScreenData(items = PreviewData.sampleItems),
               onAction = {}
           )
       }
   }
   ```
4. Never access ViewModel in preview composables
5. This is why Screen composables are separated from Route composables

**References:**

- [State Management Guide](state-management.md#composable-structure)
- feature/home/src/main/kotlin/dev/atick/feature/home/ui/home/HomeScreen.kt

---

### Compose Performance Issues

#### LazyList Scrolling Lag

**Problem:**
Scrolling through lists is janky or slow

**Solution:**

1. Always provide `key` parameter:
   ```kotlin
   LazyColumn {
       items(items = jetpacks, key = { it.id }) { jetpack ->
           JetpackCard(jetpack)
       }
   }
   ```
2. Use `contentType` for heterogeneous lists:
   ```kotlin
   items(
       items = items,
       key = { it.id },
       contentType = { it.type }  // Helps Compose reuse compositions
   ) { item ->
       when (item.type) {
           "header" -> HeaderItem(item)
           "content" -> ContentItem(item)
       }
   }
   ```
3. Avoid heavy computations in item composables:
   ```kotlin
   // Wrong - computation in composable
   JetpackCard(
       jetpack = jetpack,
       formattedDate = formatDate(jetpack.timestamp)  // Recomputed on every scroll
   )

   // Correct - computation in data layer
   data class Jetpack(
       val id: String,
       val timestamp: Long,
       val formattedDate: String  // Pre-computed
   )
   ```
4. Use `Modifier.drawWithCache` for custom drawing:
   ```kotlin
   Modifier.drawWithCache {
       val path = Path()  // Cached between recompositions
       onDrawBehind { drawPath(path, color) }
   }
   ```
5. Check for image loading issues (see Memory Issues → Image Loading)

**References:**

- feature/home/src/main/kotlin/dev/atick/feature/home/ui/home/HomeScreen.kt:104
- [Performance Guide](performance.md#lazylist-optimization)
- See also: **Memory Issues → Large List Performance Issues** (line 1250)

---

#### Compose UI Jank or Frame Drops

**Problem:**
UI animation stutters or drops frames

**Solution:**

1. Use `animateFloatAsState` for smooth animations:
   ```kotlin
   val scale by animateFloatAsState(
       targetValue = if (isPressed) 0.95f else 1f,
       animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
   )
   ```
2. Avoid heavy operations during composition:
   ```kotlin
   // Wrong - heavy operation in composition
   val result = heavyComputation(data)

   // Correct - use LaunchedEffect
   var result by remember { mutableStateOf<Result?>(null) }
   LaunchedEffect(data) {
       result = withContext(Dispatchers.Default) {
           heavyComputation(data)
       }
   }
   ```
3. Profile with Android Studio Profiler:
    - View → Tool Windows → Profiler
    - Check CPU usage during jank
    - Identify slow composables
4. Use Layout Inspector to check composition counts:
    - Enable "Show Recomposition Counts"
    - Identify composables recomposing too frequently
5. Consider using `Modifier.graphicsLayer` for transform animations:
   ```kotlin
   Modifier.graphicsLayer {
       scaleX = scale
       scaleY = scale
   }
   ```

**References:**

- [Performance Guide](performance.md#recomposition-optimization)

---

### Compose State Issues

#### remember State Lost on Recomposition

**Problem:**
State stored with `remember` resets unexpectedly

**Solution:**

1. For configuration changes (rotation), use `rememberSaveable`:
   ```kotlin
   // Wrong - lost on rotation
   var searchQuery by remember { mutableStateOf("") }

   // Correct - survives rotation
   var searchQuery by rememberSaveable { mutableStateOf("") }
   ```
2. For complex objects, provide custom Saver:
   ```kotlin
   val customSaver = Saver<CustomState, Bundle>(
       save = { state -> Bundle().apply { putString("key", state.value) } },
       restore = { bundle -> CustomState(bundle.getString("key") ?: "") }
   )

   val state by rememberSaveable(stateSaver = customSaver) {
       mutableStateOf(CustomState())
   }
   ```
3. For screen-level state, use ViewModel instead:
   ```kotlin
   @HiltViewModel
   class MyViewModel @Inject constructor() : ViewModel() {
       private val _uiState = MutableStateFlow(UiState(ScreenData()))
       val uiState = _uiState.asStateFlow()
   }
   ```

**References:**

- [State Management Guide](state-management.md)

---

#### LaunchedEffect Runs Multiple Times

**Problem:**
`LaunchedEffect` executes more than expected

**Solution:**

1. Check key parameters - effect relaunches when keys change:
   ```kotlin
   // Runs on every recomposition (Unit key never changes after first run)
   LaunchedEffect(Unit) {
       // Runs once
   }

   // Runs every time userId changes
   LaunchedEffect(userId) {
       loadUserData(userId)
   }
   ```
2. For one-time effects, use `Unit` or `true` as key:
   ```kotlin
   LaunchedEffect(Unit) {
       // Runs only once
       analytics.logScreenView("home")
   }
   ```
3. For multiple dependencies, use multiple keys:
   ```kotlin
   LaunchedEffect(userId, categoryId) {
       // Runs when either userId or categoryId changes
       loadData(userId, categoryId)
   }
   ```
4. Avoid using mutable state as keys unless intended:
   ```kotlin
   // Wrong - relaunches on every state change
   LaunchedEffect(uiState) {
       // This is almost never what you want
   }

   // Correct - specific property
   LaunchedEffect(uiState.userId) {
       loadUserData(uiState.userId)
   }
   ```

**References:**

- [State Management Guide](state-management.md)

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

### App Running Out of Memory

**Error (Logcat):**

```
java.lang.OutOfMemoryError: Failed to allocate
```

**Solution:**

1. Check for image loading issues - ensure using Coil properly:
   ```kotlin
   // Use DynamicAsyncImage component (handles memory efficiently)
   DynamicAsyncImage(
       imageUrl = imageUrl,
       contentDescription = "Image",
       modifier = Modifier.size(200.dp)
   )
   ```
2. Verify Coil configuration uses memory cache (already configured):
   ```kotlin
   // In CoilModule.kt
   .memoryCache {
       MemoryCache.Builder(context)
           .maxSizePercent(0.25)  // Use 25% of app memory
           .build()
   }
   ```
3. For large lists, ensure using `LazyColumn`/`LazyRow` (not regular Column/Row)
4. Check if loading too many high-resolution images simultaneously
5. Limit image dimensions:
   ```kotlin
   AsyncImage(
       model = ImageRequest.Builder(LocalContext.current)
           .data(imageUrl)
           .size(800)  // Limit dimensions
           .build()
   )
   ```

**References:**

- core/network/src/main/kotlin/dev/atick/core/network/di/CoilModule.kt
- core/ui/src/main/kotlin/dev/atick/core/ui/image/DynamicAsyncImage.kt
- [Performance Guide](performance.md#image-loading)

---

### Large List Performance Issues

**Problem:**
App lags or crashes when scrolling through large lists

**Solution:**

1. Always use `LazyColumn`/`LazyRow` for lists (not Column/Row):
   ```kotlin
   // Wrong - loads all items at once
   Column {
       items.forEach { item ->
           ItemCard(item)
       }
   }

   // Correct - lazy loading
   LazyColumn {
       items(items) { item ->
           ItemCard(item)
       }
   }
   ```
2. Provide `key` parameter for stable list items:
   ```kotlin
   LazyColumn {
       items(items = jetpacks, key = { it.id }) { jetpack ->
           JetpackCard(jetpack)
       }
   }
   ```
3. Use `contentType` for heterogeneous lists:
   ```kotlin
   items(items, key = { it.id }, contentType = { it.type }) { item ->
       // Compose can reuse layouts for same content type
   }
   ```
4. Avoid heavy computations in list items
5. Consider using `StaggeredGrid` for varying item sizes

**References:**

- feature/home/src/main/kotlin/dev/atick/feature/home/ui/home/HomeScreen.kt:104
- [Performance Guide](performance.md#lazylist-optimization)

---

### Compose Recomposing Too Often

**Problem:**
UI stutters or battery drains due to excessive recomposition

**Solution:**

1. Use `derivedStateOf` for computed state:
   ```kotlin
   val filteredItems by remember {
       derivedStateOf {
           items.filter { it.isActive }
       }
   }
   ```
2. Pass stable parameters to composables:
   ```kotlin
   // Wrong - lambda creates new instance every recomposition
   Button(onClick = { viewModel.loadData() })

   // Correct - stable reference
   Button(onClick = viewModel::loadData)
   ```
3. Mark data classes as `@Stable` or `@Immutable` when appropriate:
   ```kotlin
   @Immutable
   data class ScreenData(val items: List<Item>)
   ```
4. Use `key` parameter in loops to prevent unnecessary recomposition
5. Avoid reading state in composition that doesn't affect UI

**References:**

- [Performance Guide](performance.md#recomposition-optimization)
- [State Management Guide](state-management.md)

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
    - Check existing
      issues: [GitHub Issues](https://github.com/atick-faisal/Jetpack-Android-Starter/issues)
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
