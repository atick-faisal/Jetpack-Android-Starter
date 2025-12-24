# Quick Reference

Quick reference guide for the most commonly used patterns, utilities, and functions in this
template.

---

## Summary

This quick reference provides:

- **State Management** - UiState wrapper, update functions, StatefulComposable pattern
- **Navigation** - Type-safe routes with Kotlin Serialization
- **Dependency Injection** - Hilt patterns, module setup, injected dispatchers
- **Error Handling** - suspendRunCatching in repositories, automatic error display
- **Coroutines & Threading** - ViewModel scope, lifecycle-aware collection, context switching
- **Common Extensions** - StateFlow updates, Flow collection, context utilities
- **Complete Example** - End-to-end feature implementation

For detailed explanations, see the [full documentation](index.md).

> [!NOTE]
> API documentation is available after running `./gradlew dokkaGeneratePublicationHtml`. The
> generated docs will be at `build/dokka/html/index.html` and can be deployed to `docs/api/` for
> viewing at `../api/index.html`.

---

## Table of Contents

- [State Management](#state-management)
- [Navigation](#navigation)
- [Dependency Injection](#dependency-injection)
- [Error Handling](#error-handling)
- [Coroutines & Threading](#coroutines--threading)
- [Common Extensions](#common-extensions)
- [Complete Example](#complete-example-feature-implementation)
- [Quick Commands](#quick-commands)

---

## State Management

### UiState Wrapper

All screen state is wrapped in `UiState<T>`:

```kotlin
data class UiState<T : Any>(
    val data: T,
    val loading: Boolean = false,
    val error: OneTimeEvent<Throwable?> = OneTimeEvent(null)
)
```

**Initialize in ViewModel:**

```kotlin
private val _uiState = MutableStateFlow(UiState(YourScreenData()))
val uiState = _uiState.asStateFlow()
```

### State Update Functions

| Function          | When to Use                               | Returns        | Example                                             |
|-------------------|-------------------------------------------|----------------|-----------------------------------------------------|
| `updateState`     | Synchronous updates (text input, toggles) | Immediate      | `_uiState.updateState { copy(name = newName) }`     |
| `updateStateWith` | Async operations returning new data       | `Result<T>`    | `_uiState.updateStateWith { repository.getData() }` |
| `updateWith`      | Async operations returning Unit           | `Result<Unit>` | `_uiState.updateWith { repository.saveData() }`     |

**Quick Examples:**

```kotlin
// Synchronous update
fun updateName(name: String) {
    _uiState.updateState {
        copy(name = name)
    }
}

// Async update with new data
fun loadData() {
    _uiState.updateStateWith {
        repository.getData() // Returns Result<ScreenData>
    }
}

// Async update without new data
fun saveData() {
    _uiState.updateWith {
        repository.saveData() // Returns Result<Unit>
    }
}
```

### StatefulComposable Pattern

**Route Composable (with ViewModel):**

```kotlin
@Composable
fun FeatureRoute(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatefulComposable(
        state = uiState,
        onShowSnackbar = onShowSnackbar
    ) { screenData ->
        FeatureScreen(
            screenData = screenData,
            onAction = viewModel::handleAction
        )
    }
}
```

**Screen Composable (pure UI):**

```kotlin
@Composable
fun FeatureScreen(
    screenData: FeatureScreenData,
    onAction: (FeatureAction) -> Unit
) {
    // Pure UI only
}
```

**[📚 Full API Documentation](state-management.md)** - See State Management guide for detailed
UiState patterns

> [!NOTE]
> Complete API documentation is available after running `./gradlew dokkaGeneratePublicationHtml`.

---

## Navigation

### Define Routes with Kotlin Serialization

```kotlin
@Serializable
data object FeatureNavGraph

@Serializable
data object Feature

@Serializable
data class FeatureDetail(val id: String)
```

### Navigation Extensions

**Navigate to a destination:**

```kotlin
fun NavController.navigateToFeature(navOptions: NavOptions? = null) {
    navigate(Feature, navOptions)
}

fun NavController.navigateToFeatureDetail(id: String) {
    navigate(FeatureDetail(id))
}
```

**Define screen in NavGraph:**

```kotlin
fun NavGraphBuilder.featureScreen(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    onNavigateToDetail: (String) -> Unit
) {
    composable<Feature> {
        FeatureRoute(
            onShowSnackbar = onShowSnackbar,
            onNavigateToDetail = onNavigateToDetail
        )
    }
}
```

**Using in Navigation Setup:**

```kotlin
NavHost(navController, startDestination = Feature) {
    featureScreen(
        onShowSnackbar = ::showSnackbar,
        onNavigateToDetail = { id -> navController.navigateToFeatureDetail(id) }
    )
}
```

---

## Dependency Injection

### Hilt ViewModel

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val repository: FeatureRepository
) : ViewModel()
```

### Repository Binding

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindFeatureRepository(
        impl: FeatureRepositoryImpl
    ): FeatureRepository
}
```

### Injected Dispatchers

Always use injected dispatchers:

```kotlin
class DataSourceImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun fetchData() = withContext(ioDispatcher) {
        // IO operation
    }
}
```

**Available Dispatchers:**

| Qualifier            | Use Case                                |
|----------------------|-----------------------------------------|
| `@IoDispatcher`      | IO operations (network, database, file) |
| `@DefaultDispatcher` | CPU-intensive work                      |
| `@MainDispatcher`    | UI updates                              |

---

## Error Handling

### Repository Layer - suspendRunCatching

**Always** use `suspendRunCatching` in repositories:

```kotlin
class FeatureRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FeatureRepository {

    override suspend fun getData(): Result<Data> = suspendRunCatching {
        withContext(ioDispatcher) {
            networkDataSource.getData()
        }
    }
}
```

**Why not `runCatching`?**
Standard `runCatching` catches `CancellationException`, which breaks coroutine cancellation.
`suspendRunCatching` re-throws it.

### ViewModel Layer

Errors are automatically handled by `updateStateWith`/`updateWith`:

```kotlin
fun loadData() {
    _uiState.updateStateWith {
        repository.getData() // Error automatically captured
    }
}
```

### UI Layer

`StatefulComposable` automatically displays errors via snackbar:

```kotlin
StatefulComposable(
    state = uiState,
    onShowSnackbar = onShowSnackbar
) { screenData ->
    // Errors shown automatically
}
```

---

## Coroutines & Threading

### Common Patterns

**ViewModel Scope:**

```kotlin
viewModelScope.launch {
    // Coroutine automatically cancelled when ViewModel is cleared
}
```

**Collect State with Lifecycle:**

```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

**Switch Context for IO:**

```kotlin
suspend fun loadFromDatabase() = withContext(ioDispatcher) {
    database.query()
}
```

**Timeout Operations:**

```kotlin
suspend fun connectWithTimeout(): Device {
    return suspendCoroutineWithTimeout(30.seconds) { continuation ->
        device.connect { result ->
            continuation.resume(result)
        }
    }
}
```

---

## Common Extensions

### StateFlow Extensions

```kotlin
// Update state synchronously
_uiState.updateState { copy(value = newValue) }

// Update state with async operation returning new data
_uiState.updateStateWith { repository.getData() }

// Update state with async operation returning Unit
_uiState.updateWith { repository.saveData() }
```

### Flow Extensions

```kotlin
// Collect in ViewModel
viewModelScope.launch {
    repository.observeData().collect { data ->
        _uiState.update { it.copy(data = data) }
    }
}

// Collect in Composable (lifecycle-aware)
val data by remember { repository.observeData() }
    .collectAsStateWithLifecycle(initialValue = emptyList())
```

### Context Extensions

```kotlin
// Using injected dispatcher
suspend fun loadData() = withContext(ioDispatcher) {
    // IO work here
}
```

---

## Complete Example: Feature Implementation

### 1. Define Screen Data

```kotlin
data class ProfileScreenData(
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null
)
```

### 2. Create ViewModel

```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState(ProfileScreenData()))
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _uiState.updateStateWith {
            repository.getProfile()
        }
    }

    fun updateName(name: String) {
        _uiState.updateState {
            copy(name = name)
        }
    }

    fun saveProfile() {
        _uiState.updateWith {
            repository.saveProfile(this)
        }
    }
}
```

### 3. Create Repository

```kotlin
class ProfileRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ProfileRepository {

    override suspend fun getProfile(): Result<ProfileScreenData> = suspendRunCatching {
        withContext(ioDispatcher) {
            networkDataSource.getProfile().toScreenData()
        }
    }

    override suspend fun saveProfile(data: ProfileScreenData): Result<Unit> = suspendRunCatching {
        withContext(ioDispatcher) {
            networkDataSource.saveProfile(data.toNetwork())
        }
    }
}
```

### 4. Create UI

```kotlin
@Composable
fun ProfileRoute(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatefulComposable(
        state = uiState,
        onShowSnackbar = onShowSnackbar
    ) { screenData ->
        ProfileScreen(
            screenData = screenData,
            onNameChange = viewModel::updateName,
            onSave = viewModel::saveProfile
        )
    }
}

@Composable
fun ProfileScreen(
    screenData: ProfileScreenData,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column {
        TextField(
            value = screenData.name,
            onValueChange = onNameChange,
            label = { Text("Name") }
        )
        Button(onClick = onSave) {
            Text("Save")
        }
    }
}
```

### 5. Setup Navigation

```kotlin
@Serializable
data object Profile

fun NavController.navigateToProfile(navOptions: NavOptions? = null) {
    navigate(Profile, navOptions)
}

fun NavGraphBuilder.profileScreen(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean
) {
    composable<Profile> {
        ProfileRoute(onShowSnackbar = onShowSnackbar)
    }
}
```

---

---

## Further Reading

- [Architecture Guide](architecture.md) - Deep dive into architecture decisions
- [Getting Started](getting-started.md) - Step-by-step setup guide
- [Development Guide](guide.md) - Comprehensive development patterns
- [State Management](state-management.md) - Complete state management guide with detailed UiState
  patterns
- [Data Flow](data-flow.md) - Data flow patterns
- [Navigation](navigation.md) - Type-safe navigation deep dive
- [Dependency Injection](dependency-injection.md) - Complete DI guide
- [Troubleshooting](troubleshooting.md) - Common issues and solutions
- [FAQ](faq.md) - Frequently asked questions
- [Tips & Tricks](tips.md) - Best practices and advanced techniques

---

## Quick Commands

### Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Clean build
./gradlew clean build
```

### Code Quality

```bash
# Check formatting
./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache

# Auto-format
./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache
```

### Documentation

```bash
# Generate API docs
./gradlew dokkaGeneratePublicationHtml

# View at: build/dokka/html/index.html
```

---

## Need Help?

- Check the [full documentation](index.md) for detailed guides
- Review the [Troubleshooting Guide](troubleshooting.md) for common issues
- See the [FAQ](faq.md) for frequently asked questions
- Open an issue on [GitHub](https://github.com/atick-faisal/Jetpack-Android-Starter/issues)
