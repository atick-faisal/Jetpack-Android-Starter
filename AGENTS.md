# Jetpack Android Starter - AI Agent Instructions

This file provides AI coding agents with project-specific instructions, conventions, and boundaries for working with this Android codebase.

## Essential Commands

### Build & Run
```bash
# Build debug variant
./gradlew assembleDebug

# Build and install on connected device
./gradlew installDebug

# Clean build artifacts
./gradlew clean

# Build release (requires keystore.properties)
./gradlew assembleRelease
```

### Code Quality (ALWAYS RUN BEFORE COMMITTING)
```bash
# Auto-format all code with ktlint
./gradlew spotlessApply

# Check formatting
./gradlew spotlessCheck

# Run all checks
./gradlew check
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run tests for specific module
./gradlew :feature:home:test

# Run instrumentation tests
./gradlew connectedAndroidTest
```

### Documentation
```bash
# Generate API documentation with Dokka
./gradlew dokkaHtmlMultiModule
# Output: build/dokka/htmlMultiModule/
```

### Firebase Setup
```bash
# Get SHA-1 fingerprint for Firebase console
./gradlew signingReport
```

## Project Context

### Tech Stack Overview
- **Language**: Kotlin 2.3.20 with coroutines & Flow
- **UI**: Jetpack Compose with Material3 (declarative UI)
- **Architecture**: Two-layer MVVM (UI + Data, intentionally no Domain layer)
- **DI**: Dagger Hilt (compile-time injection)
- **Local Storage**: Room (SQL) + DataStore (key-value)
- **Networking**: Retrofit + OkHttp + Kotlinx Serialization
- **Backend**: Firebase (Auth, Firestore, Crashlytics, Performance)
- **Background Work**: WorkManager with sync constraints
- **Build**: Gradle 8.11.1, AGP 9.1.0, Java 21

### Architecture Pattern
**Two-Layer Architecture** (simplified from Android's three-layer approach):
1. **UI Layer**: `feature/*` modules with Composables + ViewModels (MVVM)
2. **Data Layer**: `data/` module with Repositories + Data Sources

**Why no Domain layer?** Intentionally omitted to reduce complexity. Add it only when you have complex business logic or need to share logic between multiple ViewModels.

### State Management Pattern
All screens follow a consistent state pattern using `UiState<T>` wrapper (defined in `core:ui`):

```kotlin
// 1. Define screen data (immutable state)
data class HomeScreenData(
    val items: List<Item> = emptyList()
)

// 2. ViewModel with UiState
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState(HomeScreenData()))
    val uiState = _uiState.asStateFlow()

    // Sync state update
    fun updateItems(items: List<Item>) {
        _uiState.updateState { copy(items = items) }
    }

    // Async state update (auto handles loading/error)
    fun fetchItems() {
        _uiState.updateStateWith {
            repository.fetchItems()
        }
    }
}

// 3. Composable with StatefulComposable wrapper
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatefulComposable(
        state = uiState,
        onRetry = { viewModel.fetchItems() }
    ) { data ->
        HomeScreen(data = data)
    }
}
```

**Key utilities** (in `core:ui/utils/`):
- `UiState<T>`: Wrapper with data, loading, error
- `updateState {}`: Synchronous state updates
- `updateStateWith {}`: Async operations with automatic loading/error handling
- `StatefulComposable`: Consistent loading/error UI
- `OneTimeEvent<T>`: Thread-safe one-time event consumption (navigation, snackbars)

### Navigation Pattern
Type-safe navigation using Kotlin serialization (no string-based routes):

```kotlin
// 1. Define route with @Serializable
@Serializable
data class ProfileRoute(val userId: String)

// 2. NavController extension for navigation
fun NavController.navigateToProfile(userId: String) {
    navigate(ProfileRoute(userId = userId))
}

// 3. NavGraph extension for screen registration
fun NavGraphBuilder.profileScreen(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean
) {
    composable<ProfileRoute> {
        ProfileRoute(onShowSnackbar = onShowSnackbar)
    }
}

// 4. Extract params in ViewModel via SavedStateHandle
@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val userId: String = savedStateHandle.toRoute<ProfileRoute>().userId
}
```

### Data Flow Pattern (Offline-First)
1. **UI observes repository Flow** (local database is single source of truth)
2. **Repository returns Flow from Room database**
3. **WorkManager syncs in background** (network, battery, storage constraints)
4. **Sync updates local database**
5. **UI automatically updates via Flow observation**

```kotlin
// Repository pattern example
class HomeRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val networkDataSource: NetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HomeRepository {

    // UI observes this Flow
    override fun observeData(): Flow<List<Data>> =
        localDataSource.observeData()
            .map { entities -> entities.map { it.toDomain() } }

    // Background sync calls this
    override suspend fun sync(): Result<Unit> = withContext(ioDispatcher) {
        suspendRunCatching {
            val remoteData = networkDataSource.fetchData()
            localDataSource.saveData(remoteData.map { it.toEntity() })
        }
    }
}
```

### Dependency Injection Pattern
All modules use Hilt with clear scoping:

```kotlin
// Data sources in SingletonComponent
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
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
fun HomeRoute(viewModel: HomeViewModel = hiltViewModel()) { }
```

### Module Dependencies (Respect These Boundaries)
```
feature/* → data → core:* → firebase:*
                  ↓
                sync
```

**Rules**:
- Features depend on data + core:ui only
- Data depends on core:* + firebase:*
- Core modules are independent (except core:ui can use core:android)
- Firebase modules are independent

## Conventions & Patterns

### File Naming
- **ViewModels**: `{Feature}ViewModel.kt` (e.g., `HomeViewModel.kt`)
- **Repositories**: `{Feature}Repository.kt` + `{Feature}RepositoryImpl.kt`
- **Data Sources**: `{Type}DataSource.kt` (e.g., `NetworkDataSource.kt`, `LocalDataSource.kt`)
- **Screen Data**: `{Feature}ScreenData.kt` (immutable state)
- **Routes**: `{Feature}Route` (e.g., `@Serializable data class HomeRoute`)
- **Composables**: Separate `*Route` (stateful) from `*Screen` (stateless)

### Code Organization
- **Screen composables**: `feature/{name}/ui/{Name}Screen.kt`
- **ViewModels**: `feature/{name}/viewmodel/{Name}ViewModel.kt`
- **Navigation**: `feature/{name}/navigation/{Name}Navigation.kt`
- **Repositories**: `data/repository/{name}/{Name}Repository.kt`
- **Models**: Network models in `core:network/model/`, Domain models in `data/model/`, Entities in `core:room/entity/`

### Threading & Coroutines
- Use `@IoDispatcher` for IO operations (network, database, file I/O)
- Use `@MainDispatcher` for UI operations
- Use `@DefaultDispatcher` for CPU-intensive work
- Wrap IO operations with `withContext(ioDispatcher)`
- Use `suspendRunCatching` for error handling in repositories

### Error Handling
- Repositories return `Result<T>` for one-time operations
- Repositories return `Flow<T>` for observable data
- Use `suspendRunCatching` to wrap suspend functions and convert exceptions to Results
- UI layer handles errors via `UiState.error: OneTimeEvent<Throwable?>`

### Kotlin Compiler Features
- **Context parameters enabled** (`-Xcontext-parameters`): `updateStateWith` and `updateWith` automatically access ViewModel scope
- **Material3 experimental APIs** opted-in globally
- **RequiresOptIn** enabled

## Known Gotchas & Special Notes

### AGP 9 Migration (Completed March 2026)
⚠️ Project recently migrated to Android Gradle Plugin 9.1.0. Known issues:
- **Dokka warnings**: `AndroidExtensionWrapper could not get Android Extension` (harmless)
- **Spotless task discovery**: Tasks not visible in `./gradlew tasks` but still execute
- **Custom APK naming**: Temporarily disabled due to API changes

### Gradle Configuration
- **Configuration cache enabled**: May be discarded by OssLicensesTask (harmless)
- **JVM heap**: 8GB configured in `gradle.properties`
- **Parallel builds**: Enabled for performance

### Spotless & Code Formatting
- **CRITICAL**: Always run `./gradlew spotlessApply` before committing
- CI will fail if code is not formatted
- Configuration in `gradle/init.gradle.kts`
- Uses ktlint + custom Compose rules

### Convention Plugins
Located in `build-logic/convention/`. Do NOT modify these unless you understand the full impact:
- `dev.atick.application`: App module setup
- `dev.atick.library`: Base library setup
- `dev.atick.ui.library`: UI library with Compose
- `dev.atick.dagger.hilt`: Hilt DI setup
- `dev.atick.firebase`: Firebase services

### Version Management
- All dependencies in `gradle/libs.versions.toml` (Gradle Version Catalog)
- Use `libs.{name}` in build files
- Renovate & Dependabot configured for auto-updates

### Release Builds
- Requires `keystore.properties` in project root (NOT committed to git)
- Keystore file should be in `app/` directory
- Never commit keystore files or credentials

### Firebase
- Debug build has template `google-services.json` (features won't work until configured)
- Requires Firebase project setup with package name `dev.atick.compose`
- See `docs/firebase.md` for detailed setup

### LeakCanary
- Enabled in debug builds by default
- Comment out in `app/build.gradle.kts` to disable: `debugImplementation(libs.leakcanary)`

## Boundaries & Guidelines

### ALWAYS DO
✅ Run `./gradlew spotlessApply` before any commit
✅ Use existing state management patterns (`UiState<T>`, `updateStateWith`)
✅ Use type-safe navigation with `@Serializable` routes
✅ Separate stateful Route from stateless Screen composables
✅ Use Hilt for dependency injection (`@HiltViewModel`, `@Inject`)
✅ Use `suspendRunCatching` for error handling in repositories
✅ Use dispatcher qualifiers (`@IoDispatcher`, `@MainDispatcher`)
✅ Return `Flow` for observable data, `Result<T>` for one-time operations
✅ Follow offline-first pattern (local database as source of truth)
✅ Respect module boundaries (features → data → core)
✅ Add integration tests for new features when test infrastructure exists

### ASK FIRST
⚠️ Adding new Gradle dependencies
⚠️ Modifying convention plugins in `build-logic/`
⚠️ Changing AGP, Kotlin, or Compose versions
⚠️ Adding new modules
⚠️ Modifying Firebase configuration
⚠️ Changing ProGuard rules
⚠️ Adding domain layer (currently intentionally omitted)
⚠️ Modifying Spotless configuration
⚠️ Changing min/target SDK versions

### NEVER DO
❌ Commit without running `./gradlew spotlessApply`
❌ Commit keystore files or `keystore.properties`
❌ Use string-based navigation (always use `@Serializable` routes)
❌ Directly access `viewModelScope` in state updates (use `updateStateWith` which handles it via context parameters)
❌ Make blocking IO calls on main thread
❌ Use `GlobalScope` or unstructured concurrency
❌ Bypass Hilt and manually create ViewModels or repositories
❌ Mix UI logic in ViewModels (keep ViewModels UI-agnostic)
❌ Ignore module boundaries (e.g., feature modules depending on other features)
❌ Add dependencies without using version catalog (`gradle/libs.versions.toml`)
❌ Force push to main/master branch
❌ Modify existing database entities without migration strategy

## Example: Adding a New Feature

When implementing a new feature, follow this workflow:

1. **Define models** in appropriate layers:
   - Network DTOs in `core:network/model/` (with `@Serializable`)
   - Database entities in `core:room/entity/` (with `@Entity`)
   - Domain models in `data/model/`

2. **Create data sources** (if needed):
   - Network: interface in `core:network/datasource/`
   - Local: interface in `core:room/datasource/`, DAOs in `core:room/dao/`

3. **Create repository**:
   - Interface in `data/repository/{feature}/`
   - Implementation with `suspendRunCatching` error handling
   - Return `Flow` for observable data, `Result<T>` for one-shot operations

4. **Create UI layer in `feature/{name}/`**:
   - `ui/{Name}Screen.kt`: Screen data class + Route + Screen composables
   - `viewmodel/{Name}ViewModel.kt`: `@HiltViewModel` with `UiState<ScreenData>`
   - `navigation/{Name}Navigation.kt`: NavController extension + NavGraphBuilder extension

5. **Set up navigation**:
   - Define `@Serializable` route object
   - Create `NavController.navigateTo{Name}()` extension
   - Create `NavGraphBuilder.{name}Screen()` extension
   - Add to main NavHost in `app/navigation/NavHost.kt`

6. **Configure DI**:
   - Bind data sources in appropriate modules
   - Bind repository interface to implementation
   - ViewModels auto-discovered via `@HiltViewModel`

7. **Run quality checks**:
   ```bash
   ./gradlew spotlessApply
   ./gradlew check
   ```

## Additional Resources

- **Comprehensive documentation**: `docs/` directory
  - `architecture.md`: Architecture deep dive
  - `state-management.md`: State patterns & utilities
  - `navigation.md`: Navigation patterns
  - `dependency-injection.md`: Complete DI guide
  - `data-flow.md`: Offline-first, caching, sync patterns
  - `firebase.md`: Firebase setup & troubleshooting
  - `plugins.md`: Convention plugins guide
- **Live documentation**: https://atick.dev/Jetpack-Android-Starter
- **CI/CD workflows**: `.github/workflows/` (ci.yml, cd.yml, docs.yml)

## Questions or Issues?

If you encounter issues or have questions:
1. Check `docs/troubleshooting.md` for common problems
2. Review relevant documentation in `docs/`
3. Check GitHub issues for known problems
4. Refer to the comprehensive inline documentation in the codebase
