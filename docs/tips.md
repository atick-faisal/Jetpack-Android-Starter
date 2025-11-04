# Tips and Tricks

Get the most out of this template with these useful tips and features.

## Core Module Utilities

### Core Android Utilities

The `core:android` module contains several helpful extensions and utilities:

```kotlin
// String Extensions
email.isEmailValid()                 // Email validation
password.isPasswordValid()           // Password validation
name.isValidFullName()              // Name validation

// Number Formatting
number.format(nDecimal = 2)         // Format numbers with specific decimals
timestamp.asFormattedDateTime()      // Convert timestamp to readable date

// Context Extensions
context.showToast(message)          // Show toast message
context.hasPermission(permission)    // Check single permission
context.isAllPermissionsGranted()   // Check multiple permissions
context.createNotificationChannel() // Create notification channel
```

### Core UI Components

The `core:ui/components` directory contains pre-built Material3 composables:

```kotlin
// Pre-built components
JetpackButton(onClick = {})
JetpackOutlinedButton(onClick = {})
JetpackTextButton(onClick = {})
JetpackTextFiled(value = "", onValueChange = {})
JetpackLoadingWheel(contentDesc = "")
SwipeToDismiss(onDelete = {})
```

> [!TIP]
> Always use these pre-built components instead of creating new ones. They provide consistent
> styling and can be modified centrally.

### Theming System

The `core:ui/theme` directory contains theming utilities:

```kotlin
// Custom theme
JetpackTheme(
    darkTheme = isDarkTheme,
    disableDynamicTheming = true
) {
    // Your content
}

// Background with gradient
AppGradientBackground(
    gradientColors = gradientColors
) {
    // Your content
}
```

## Network State Monitoring

The `core:network` module provides network state monitoring:

```kotlin
class YourViewModel @Inject constructor(
    private val networkUtils: NetworkUtils
) : ViewModel() {
    init {
        networkUtils.networkState
            .onEach { state ->
                when (state) {
                    NetworkState.CONNECTED -> // Handle connected state
                        NetworkState.LOST
                    -> // Handle disconnected state
                        NetworkState.UNAVAILABLE
                    -> // Handle unavailable state
                }
            }
            .launchIn(viewModelScope)
    }
}
```

## Data Synchronization

The `sync` module provides a robust way to synchronize local data with remote services:

```kotlin
// 1. Make your repository syncable
interface YourRepository : Syncable {
    suspend fun sync(): Flow<SyncProgress>
}

// 2. Inject SyncManager in the repository implementation
class YourRepositoryImpl @Inject constructor(
    private val syncManager: SyncManager
) : YourRepository {
    fun requestSync() {
        syncManager.requestSync()
    }
}

// 3. Inject repository into SyncWorker
@HiltWorker
class SyncWorker @AssistedInject constructor(
    private val yourRepository: YourRepository
) : CoroutineWorker {
    override suspend fun doWork(): Result {
        yourRepository.sync()
            .collect { progress ->
                setProgress(progress)
            }
        return Result.success()
    }
}
```

> [!NOTE]
> The sync module is already integrated with WorkManager for background synchronization. Just
> implement the Syncable interface in your repositories and inject them into the SyncWorker.

## Secrets Management

The template includes the gradle-secrets-plugin for secure credentials management:

```properties
# local.properties (git-ignored)
API_KEY=your_actual_api_key
API_SECRET=your_actual_secret
# secrets.defaults.properties (version controlled)
API_KEY=dummy_api_key
API_SECRET=dummy_secret
```

Access secrets as `BuildConfig` fields:

```kotlin
    val apiKey = BuildConfig.apiKey
```

> [!TIP]
> Use secrets.defaults.properties to provide dummy values for CI/CD environments while keeping
> sensitive data in local.properties.

## Documentation Generation

The template comes with Dokka setup for documentation generation:

```bash
# Generate markdown documentation
./gradlew dokkaGfmMultiModule --no-configuration-cache

# The generated docs will be available in:
# build/dokka/gfmMultiModule/
```

MkDocs is also configured to create beautiful Material theme documentation:

```yaml
# mkdocs.yml is already configured with:
# - Material theme
# - Search functionality
# - Code highlighting
# - Navigation structure
```

> [!NOTE]
> If you're using GitHub, the documentation is automatically generated and published through the
> docs workflow.

## Error Handling and Loading States

### Using StateFlow Updates

```kotlin
// Regular state updates
_uiState.updateState {
    copy(value = newValue)
}

// Async operations that doesn't return anything
_uiState.updateWith {
    repository.someAsyncOperation()
}

// Async operations that returns results
_uiState.updateStateWith {
    repository.someAsyncOperation()
}
```

### Automatic Error Handling

```kotlin
@Composable
fun YourScreen(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean
) {
    StatefulComposable(
        state = uiState,
        onShowSnackbar = onShowSnackbar
    ) { data ->
        // Your UI content
    }
}
```

> [!NOTE]
> The error snackbar automatically includes a "Report" button that logs the error stack trace to
> Firebase Analytics.

### Custom Snackbar Actions

```kotlin
// In ViewModel
class ScreenViewModel : ViewModel() {
    fun undoDelete(itemId: String) {
        _uiState.updateStateWith {
            repository.undoDelete(itemId)
        }
    }
}

// In UI
StatefulComposable(
    state = screenUiState,
    onShowSnackbar = onShowSnackbar,
) { screenData ->
    LaunchedEffect(screenData.deletedItemId, onShowSnackbar) {
        val itemId = screenData.deletedItemId.getContentIfNotHandled()
        if (itemId != null) {
            val actionPerformed = onShowSnackbar(
                "Item deleted",
                SnackbarAction.UNDO,
                null
            )
            if (actionPerformed) viewModel.undoDelete(itemId)
        }
    }
}
```

## Composable Previews

Use the provided preview annotations for efficient UI development:

```kotlin
@Composable
@PreviewThemes     // Preview in both light and dark themes
@PreviewDevices    // Preview on different device sizes
fun YourComposablePreview() {
    JetpackTheme {
        YourComposable()
    }
}
```

Preview devices include:

- Phone (360x640dp)
- Landscape (640x360dp)
- Foldable (673x841dp)
- Tablet (1280x800dp)

## Coroutine Utilities

```kotlin
// Run with timeout
val result = suspendCoroutineWithTimeout(5.seconds) {
    // Your async operation
}

// Safe coroutine execution
val result = suspendRunCatching {
    // Your operation that might fail
}
```

## Resource Management

```kotlin
// Network-bound resource handling
fun getData(): Flow<Resource<Data>> = networkBoundResource(
        query = { localDataSource.getData() },
        fetch = { apiService.getData() },
        saveFetchResult = { localDataSource.saveData(it) }
    )
```

## UI Extensions

```kotlin
// Lifecycle aware Flow collection
lifeCycleOwner.collectWithLifecycle(flow) { data ->
    // Handle data
}

// LiveData observation
lifeCycleOwner.observe(liveData) { data ->
    // Handle data
}

// One-time event observation
lifeCycleOwner.observeEvent(eventLiveData) { event ->
    // Handle one-time event
}
```

## Multilingual Support

Automatic generation of `LocaleConfig` is enabled in `app/build.gradle.kts`:

```kotlin
androidResources {
    generateLocaleConfig = true
}
```

Also, locale preferences are saved and applied automatically:

```xml
<service
    android:name="androidx.appcompat.app.AppLocalesMetadataHolderService"
    android:enabled="false"
    android:exported="false">
    <meta-data
        android:name="autoStoreLocales"
        android:value="true" />
</service>
```

Read more about automatic locale generation [here](https://developer.android.com/guide/topics/resources/app-languages).

## Best Practices

1. **Use Pre-built Components**
	- Leverage the components in `core:ui/components`
	- Maintain consistent styling across the app
	- Centralize design system changes

2. **Error Handling**
	- Use `suspendRunCatching` in repositories
	- Let `StatefulComposable` handle UI states
	- Leverage the built-in error reporting

3. **State Management**
	- Use state update helpers consistently
	- Leverage `JetpackAppState` for app-wide state
	- Follow unidirectional data flow

4. **Network Handling**
	- Monitor network state with `NetworkUtils`
	- Use network-bound resources for caching
	- Handle offline scenarios gracefully

5. **Resource Loading**
	- Use `DynamicAsyncImage` for images
	- Leverage `JetpackLoadingWheel`
	- Follow Material3 loading patterns

6. **Documentation**
	- Use Dokka for code documentation
	- Keep documentation up to date
	- Leverage the automated docs workflow

7. **Secrets Management**
	- Store sensitive data in local.properties
	- Provide defaults in secrets.defaults.properties
	- Use buildConfigField for config values

> [!TIP]
> Explore the `core` modules thoroughly - they contain many utilities that can save you time and
> ensure consistency across your app.
