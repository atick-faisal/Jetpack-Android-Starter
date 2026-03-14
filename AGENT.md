# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project Overview

This is a production-ready Android starter template built on modern Jetpack libraries, following the
architecture patterns from [Now In Android](https://github.com/android/nowinandroid). It uses a *
*two-layer architecture** (UI + Data) with clean separation of concerns, Firebase integration, and
comprehensive tooling for Android development.

**Key Technologies:**

- Jetpack Compose with Material3
- Dagger Hilt for dependency injection
- Kotlin Coroutines & Flow
- Room + DataStore for local storage
- Retrofit + OkHttp for networking
- Firebase (Auth, Firestore, Analytics, Crashlytics)
- WorkManager for background sync

## Common Commands

### Building and Running

```bash
# Build debug variant
./gradlew assembleDebug

# Build release variant (requires keystore.properties)
./gradlew assembleRelease

# Run app on connected device
./gradlew installDebug

# Clean build
./gradlew clean
```

### Code Quality and Formatting

```bash
# Check code formatting (uses ktlint via Spotless)
./gradlew spotlessCheck

# Auto-format all code (ALWAYS run before committing)
./gradlew spotlessApply

# Run all checks
./gradlew check
```

**IMPORTANT:** Always run `./gradlew spotlessApply` before committing to avoid CI failures. The
project uses Spotless with ktlint and custom Compose rules configured in `gradle/init.gradle.kts`.

### Documentation

```bash
# Generate API documentation with Dokka
./gradlew dokkaHtmlMultiModule

# Documentation is output to build/dokka/htmlMultiModule/
```

### Testing

```bash
# Run unit tests
./gradlew test

# Run tests for specific module
./gradlew :feature:home:test

# Run Android instrumentation tests
./gradlew connectedAndroidTest
```

### Signing and Release

```bash
# Get SHA-1 fingerprint for Firebase setup
./gradlew signingReport

# Or use the "Signing Report" run configuration in Android Studio
```

## Module Structure

The project follows a modular architecture organized by feature and layer:

```
.
├── app/                      # Main application module
├── build-logic/              # Custom Gradle convention plugins
│   └── convention/           # Plugin implementations
├── core/                     # Core infrastructure modules
│   ├── android/              # Android utilities, coroutines, extensions
│   ├── network/              # Retrofit, OkHttp, network data sources
│   ├── preferences/          # DataStore for user preferences
│   ├── room/                 # Room database, DAOs, local data sources
│   └── ui/                   # Compose components, theme, state utilities
├── data/                     # Repository layer (domain models & repos)
├── feature/                  # Feature modules (UI layer)
│   ├── auth/                 # Authentication screens
│   ├── home/                 # Home screen
│   ├── profile/              # Profile screen
│   └── settings/             # Settings dialog
├── firebase/                 # Firebase integration modules
│   ├── analytics/            # Crashlytics and analytics
│   ├── auth/                 # Firebase authentication
│   └── firestore/            # Firestore data source
└── sync/                     # WorkManager background sync
```

## Architecture Patterns

### Two-Layer Architecture

1. **UI Layer**: Composables + ViewModels using MVVM pattern
2. **Data Layer**: Repositories + Data Sources (Network, Local, Firebase)

**Note:** Unlike official Android guidelines, this template intentionally omits the domain layer to
reduce complexity. Add a domain layer (with use cases) only when you have complex business logic or
need to share logic between multiple ViewModels.

### State Management Pattern

All UI state follows a consistent pattern using `UiState<T>` wrapper:

```kotlin
// Screen data (immutable state)
data class HomeScreenData(
    val items: List<Item> = emptyList()
)

// ViewModel with UiState
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState(HomeScreenData()))
    val uiState = _uiState.asStateFlow()

    // Update state directly
    _uiState.updateState
    { copy(items = newItems) }

    // Update state with async operation
    _uiState.updateStateWith
    {
        repository.fetchItems()
    }
}
```

**Key State Utilities** (in `core:ui`):

- `UiState<T>`: Wrapper with data, loading, and error handling
- `updateState {}`: Synchronous state updates
- `updateStateWith {}`: Async operations with automatic loading/error handling
- `StatefulComposable`: Consistent loading/error UI presentation

**Context Parameters:** The project uses Kotlin's `-Xcontext-parameters` compiler flag, so
`updateStateWith` and `updateWith` automatically access the ViewModel's scope without explicitly
passing `viewModelScope`.

### Navigation Pattern

Type-safe navigation using Kotlin serialization:

```kotlin
// Define route with parameters
@Serializable
data class ProfileRoute(val userId: String)

// Navigate
navController.navigateToProfile(userId = "123")

// NavGraph extension
fun NavGraphBuilder.profileScreen(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean
) {
    composable<ProfileRoute> { backStackEntry ->
        ProfileRoute(onShowSnackbar = onShowSnackbar)
    }
}

// Extract params in ViewModel
@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val userId: String = savedStateHandle.toRoute<ProfileRoute>().userId
}
```

### Data Flow Pattern

**Offline-First with Background Sync:**

1. UI observes repository Flow (local database is single source of truth)
2. Repository returns Flow from Room database
3. WorkManager periodically syncs in background
4. Sync updates local database
5. UI automatically updates via Flow observation

```kotlin
// Repository pattern
class HomeRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val networkDataSource: NetworkDataSource
) : HomeRepository {
    // UI observes this
    override fun observeData(): Flow<List<Data>> =
        localDataSource.observeData()
            .map { entities -> entities.map { it.toDomain() } }

    // Background sync calls this
    override suspend fun sync(): Result<Unit> =
        suspendRunCatching {
            val remoteData = networkDataSource.fetchData()
            localDataSource.saveData(remoteData.map { it.toEntity() })
        }
}
```

### Dependency Injection Pattern

All modules use Hilt with clear scoping:

```kotlin
// Data sources provided in SingletonComponent
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideApiService(): ApiService = /* ... */
}

// Repositories use @Binds for interface binding
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRepository(impl: RepoImpl): Repository
}

// ViewModels use @HiltViewModel
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel()

// Composables use hiltViewModel()
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel()
) { /* ... */
}
```

## Convention Plugins

The `build-logic/convention/` directory contains custom Gradle convention plugins to avoid build
configuration duplication:

- **`dev.atick.application`**: Application module setup (Compose, BuildConfig, Kotlin)
- **`dev.atick.library`**: Base library setup (Kotlin, KotlinX Serialization)
- **`dev.atick.ui.library`**: UI library setup (extends library + Compose)
- **`dev.atick.dagger.hilt`**: Hilt dependency injection setup (KSP)
- **`dev.atick.firebase`**: Firebase services (BoM, Analytics, Crashlytics, Performance)

**Usage in module build files:**

```kotlin
plugins {
    alias(libs.plugins.jetpack.ui.library)  // For feature modules
    alias(libs.plugins.jetpack.dagger.hilt)
}
```

**Key Configuration:**

- Java 21 target (defined in version catalog)
- Kotlin context parameters enabled (`-Xcontext-parameters`)
- Material3 experimental APIs opted-in
- Compose compiler metrics/reports enabled (see `gradle.properties`)

## Adding New Features

Follow this workflow when implementing new features:

1. **Define models** in appropriate layers:
    - Network models in `core:network` (with `@Serializable`)
    - Database entities in `core:room` (with `@Entity`)
    - Domain models in `data` module

2. **Create data sources** (if needed):
    - Network: extends `NetworkDataSource` in `core:network`
    - Local: extends `LocalDataSource` in `core:room`
    - Use `@IoDispatcher` and wrap IO operations with `withContext(ioDispatcher)`

3. **Create repository**:
    - Interface and implementation in `data/repository`
    - Use `suspendRunCatching` for error handling
    - Return `Flow` for observable data, `Result<T>` for one-time operations

4. **Create UI layer**:
    - Screen data class (immutable state)
    - `@HiltViewModel` with `UiState<ScreenData>`
    - Composable with `StatefulComposable` wrapper
    - Separate `*Route` (stateful) from `*Screen` (stateless) composables

5. **Set up navigation**:
    - Define `@Serializable` route object
    - Create `NavController.navigateTo*()` extension
    - Create `NavGraphBuilder.*Screen()` extension

6. **Configure DI**:
    - Bind data sources in appropriate modules
    - Bind repository interface to implementation
    - ViewModels auto-discovered via `@HiltViewModel`

## Firebase Setup

The debug build includes a template `google-services.json` but Firebase features won't work until
configured:

1. Create Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add Android app with package name `dev.atick.compose`
3. Enable Authentication (Google Sign-In + Email/Password)
4. Create Firestore database
5. Download `google-services.json` to `app/`
6. Add SHA-1 fingerprint: `./gradlew signingReport`

See `docs/firebase.md` for detailed setup instructions.

## Version Management

Dependencies are managed via **Gradle Version Catalog** in `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.1.0"
compose = "1.8.0"

[libraries]
androidx-compose-ui = { module = "androidx.compose.ui:ui", version.ref = "compose" }

[plugins]
jetpack-application = { id = "dev.atick.application", version = "unspecified" }
```

**Adding dependencies:**

1. Add version to `[versions]` section
2. Add library/plugin reference in appropriate section
3. Use in modules: `implementation(libs.androidx.compose.ui)`

## Code Style and Formatting

**Spotless Configuration** (`gradle/init.gradle.kts`):

- ktlint for Kotlin formatting
- Custom Compose rules from `io.nlopez.compose.rules:ktlint`
- License headers auto-applied from `spotless/copyright.*` files
- `.editorconfig` for IDE integration

**Before every commit:**

```bash
./gradlew spotlessApply
```

**Kotlin Compiler Flags:**

- Context parameters enabled (`-Xcontext-parameters`)
- Material3 experimental APIs opted-in
- `kotlin.RequiresOptIn` enabled globally

## Release Build Setup

1. Create keystore via Android Studio's "Generate Signed Bundle/APK"
2. Create `keystore.properties` in project root:
   ```properties
   storePassword=your-password
   keyPassword=your-key-password
   keyAlias=your-alias
   storeFile=path/to/keystore.jks
   ```
3. Place keystore in `app/` directory
4. **Never commit** `keystore.properties` or keystore file

APK naming pattern: `Jetpack_release_v{version}_{timestamp}.apk`

## CI/CD

GitHub Actions workflows in `.github/workflows/`:

- **`ci.yml`**: Build, lint (spotlessCheck), test on PRs
- **`cd.yml`**: Release builds and GitHub releases
- **`docs.yml`**: Deploy MkDocs documentation to GitHub Pages

**Required secrets** for CD:

- `KEYSTORE`: Base64-encoded keystore file
- `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
- `GOOGLE_SERVICES_JSON`: Firebase config

## Important Files

- `gradle/init.gradle.kts`: Spotless configuration (applied to all subprojects)
- `gradle/libs.versions.toml`: Centralized dependency versions
- `settings.gradle.kts`: Module registry and build configuration
- `build-logic/`: Custom convention plugins
- `.editorconfig`: Code style configuration
- `mkdocs.yml`: Documentation site configuration

## Development Guidelines

1. **Module boundaries**: Respect dependency directions (features → data → core)
2. **Error handling**: Use `Result<T>` and `suspendRunCatching` in repositories
3. **Threading**: Use `@IoDispatcher` for IO operations, wrap with `withContext()`
4. **State updates**: Use `updateState` for sync, `updateStateWith` for async
5. **Composable separation**: Keep Route (stateful) separate from Screen (stateless)
6. **Navigation**: Always use type-safe navigation with Kotlin serialization
7. **Testing**: Add tests for new features (infrastructure is upcoming)

## Documentation

**Comprehensive guides** in `docs/`:

- `architecture.md`: Architecture deep dive
- `guide.md`: Step-by-step feature implementation
- `state-management.md`: State patterns and utilities
- `navigation.md`: Navigation patterns
- `dependency-injection.md`: Complete DI guide (993 lines)
- `data-flow.md`: Offline-first, caching, sync patterns
- `firebase.md`: Firebase setup and troubleshooting
- `spotless.md`: Code formatting and license headers
- `plugins.md`: Convention plugins guide
- `troubleshooting.md`: Common issues and solutions

**Live documentation
**: [atick.dev/Jetpack-Android-Starter](https://atick.dev/Jetpack-Android-Starter)

## AGP 9 Migration Notes

This project has been migrated to **Android Gradle Plugin 9.1.0** (March 2026). Key changes:

### Breaking Changes Applied

1. **Built-in Kotlin Support**: Removed `kotlin-android` plugin from all convention plugins
    - AGP 9+ includes Kotlin support by default
    - See: https://kotl.in/gradle/agp-built-in-kotlin

2. **New Variant API**: Migrated from `applicationVariants` to `androidComponents.onVariants`
    - Old variant API completely removed in AGP 9
    - Direct output file manipulation no longer supported

3. **DSL Updates**: Updated to new DSL interfaces
    - Changed `com.android.build.gradle.LibraryExtension` →
      `com.android.build.api.dsl.LibraryExtension`

### Known Issues & Workarounds

⚠️ **Dokka AGP 9 Compatibility** (Issue #578)

- Warnings during configuration: `AndroidExtensionWrapper could not get Android Extension`
- Workaround: Configuration wrapped in `afterEvaluate` + using `configureEach`
- **TODO**: Upgrade to Dokka 2.2.0-Beta when stable

⚠️ **Custom APK Naming** (Issue #579)

- Previous custom naming removed (AGP 9 API change)
- Currently using default APK naming scheme
- **TODO**: Implement AGP 9-compatible approach using variant artifacts API

⚠️ **Spotless Task Discovery** (Issue #580)

- Spotless tasks not discoverable via `./gradlew tasks`
- Tasks may still execute during builds
- **TODO**: Investigate Gradle 9.4.0 compatibility

📋 **Migration Tracking**: See Issue #581 for complete migration status

### Version Requirements

- **AGP**: 9.1.0 (requires Gradle 9.1+, JDK 17+, SDK Build Tools 36.0.0)
- **Gradle**: 9.4.0 (auto-upgraded from 8.11.1)
- **Kotlin**: 2.3.10
- **Hilt**: 2.59.2 (AGP 9 compatible)
- **Dokka**: 2.1.0 (needs upgrade to 2.2.0-Beta)

## Special Notes

- **JDK 17+ required**: Enforced in `settings.gradle.kts` startup check
- **Gradle 9.4.0**: Uses configuration cache and build scans (CI only)
- **LeakCanary**: Enabled in debug builds (comment out in `app/build.gradle.kts` to disable)
- **Compose Metrics**: Generated when `enableComposeCompilerMetrics=true` in `gradle.properties`
- **Configuration Cache**: May be discarded due to incompatible tasks (e.g., OssLicensesTask) - this
  is harmless
