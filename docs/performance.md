# Performance Optimizations

This guide covers various performance optimization features available in the project.

## Compose Compiler Metrics

The project includes built-in support for Compose compiler metrics to help optimize your
composables.

Check the `gradle.properties` file to ensure the following:

```properties
enableComposeCompilerMetrics=true
enableComposeCompilerReports=true
```

This configuration will generate detailed reports in:

- `build/<module>/compose-metrics`: Composition metrics
- `build/<module>/compose-reports`: Compiler reports

### Using the Reports

1. **Skippability Report**: Shows which composables can't skip recomposition
2. **Events Report**: Details composition events
3. **Metrics Report**: Performance metrics for each composable

### Optimizing Composables

Use the reports to identify and fix recomposition issues:

```kotlin
// Bad: Class not marked as stable
data class UiState(
    val mutableList: MutableList<String> // Unstable type
)

// Good: Stable class with immutable properties
@Immutable
data class UiState(
    val list: List<String> // Immutable type
)
```

Common optimizations:

```kotlin
// Mark data classes as stable
@Stable
data class YourData(...)

// Use immutable collections
val items: ImmutableList<Item>

// Remember expensive computations
val filteredList = remember(items) {
    items.filter { ... }
}
```

> [!TIP]
> Use `@Immutable` for classes that never change and `@Stable` for classes whose properties may
> change but maintain identity.

## LazyList Optimization

The project uses `LazyVerticalStaggeredGrid` in the home screen (see
`feature/home/src/main/kotlin/dev/atick/feature/home/ui/home/HomeScreen.kt:104`). Here are best
practices for LazyList components.

### Use Stable Keys

Always provide stable, unique keys for list items:

```kotlin
// ✅ Good: Stable unique keys (as used in HomeScreen.kt)
LazyVerticalStaggeredGrid(
    columns = StaggeredGridCells.Adaptive(300.dp),
    contentPadding = PaddingValues(16.dp),
    state = state,
) {
    items(items = jetpacks, key = { it.id }) { jetpack ->
        JetpackCard(jetpack = jetpack)
    }
}

// ❌ Bad: No keys or unstable keys
LazyColumn {
    items(items) { item ->  // Missing key
        ItemCard(item)
    }
}

// ❌ Bad: Using index as key (unstable when items change order)
LazyColumn {
    itemsIndexed(items) { index, item ->
        key(index) {  // Index changes when items reorder
            ItemCard(item)
        }
    }
}
```

> [!TIP]
> Keys enable Compose to track items across recompositions, reducing unnecessary recompositions and
> preserving item state.

### Content Type for Mixed Lists

For lists with different item types, use `contentType` to optimize composition:

```kotlin
LazyColumn {
    items(
        items = mixedItems,
        key = { it.id },
        contentType = { it.type }  // Groups similar items for composition optimization
    ) { item ->
        when (item) {
            is HeaderItem -> HeaderCard(item)
            is ContentItem -> ContentCard(item)
            is AdItem -> AdCard(item)
        }
    }
}
```

### Avoid Heavy Computations in Item Composables

Move computations outside the item scope or use `remember`:

```kotlin
// ✅ Good: Computation cached with remember
@Composable
fun ItemCard(item: Item) {
    val formattedDate = remember(item.timestamp) {
        formatDate(item.timestamp)  // Expensive formatting cached
    }

    Text(formattedDate)
}

// ❌ Bad: Computation runs on every recomposition
@Composable
fun ItemCard(item: Item) {
    Text(formatDate(item.timestamp))  // Runs every time
}
```

## Image Loading Best Practices

This template uses Coil for image loading (see
`core/network/src/main/kotlin/dev/atick/core/network/di/coil/CoilModule.kt` and
`core/ui/src/main/kotlin/dev/atick/core/ui/components/DynamicAsyncImage.kt`).

### Coil Configuration

The project provides an `ImageLoader` through Hilt in `CoilModule.kt`:

```kotlin
@Provides
@Singleton
fun provideImageLoader(
    okHttpCallFactory: Call.Factory,
    @ApplicationContext application: Context,
): ImageLoader = ImageLoader.Builder(application)
        .callFactory(okHttpCallFactory)
        .components {
            add(SvgDecoder.Factory())  // SVG support
        }
        .respectCacheHeaders(false)  // Assumes versioned URLs
        .apply {
            if (BuildConfig.DEBUG) {
                logger(DebugLogger())
            }
        }
        .build()
```

The `App` class implements `ImageLoaderFactory` to provide this loader:

```kotlin
@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    override fun newImageLoader(): ImageLoader = imageLoader.get()
}
```

### Using DynamicAsyncImage

The template provides a `DynamicAsyncImage` component in `core/ui` that handles:

- **Loading state**: Displays `JetpackLoadingWheel` while loading
- **Error handling**: Falls back to placeholder image on load failure
- **Theme integration**: Applies tint color from `LocalTintTheme`
- **Preview mode**: Shows placeholder in Android Studio preview

```kotlin
DynamicAsyncImage(
    imageUrl = "https://example.com/avatar.jpg",
    contentDescription = "User avatar",
    placeholder = painterResource(R.drawable.ic_placeholder),
    modifier = Modifier
        .size(80.dp)
        .clip(CircleShape),
)
```

### Custom Image Loading with AsyncImage

For custom loading behavior, use Coil's `AsyncImage` directly:

```kotlin
@Composable
fun UserAvatar(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "User avatar",
        contentScale = ContentScale.Crop,
        modifier = modifier.size(48.dp)
    )
}
```

## Memory Leak Prevention

Common sources of memory leaks in Android apps and how to prevent them.

### Avoid Leaked ViewModels

ViewModels should never hold references to Activities, Fragments, or Views:

```kotlin
// ❌ Bad: Context leak
class HomeViewModel(
    private val context: Context  // Leaked if Activity context
) : ViewModel()

// ✅ Good: Use Application context or dependency injection (as used in this template)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel()
```

### Lifecycle-Aware Flow Collection

Always use `collectAsStateWithLifecycle` in Composables (as used throughout the template):

```kotlin
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    // ✅ Good: Lifecycle-aware collection (used in all feature screens)
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    // ❌ Bad: Collects even when screen not visible
    // val uiState by viewModel.uiState.collectAsState()
}
```

### Cancel Coroutines Properly

Always use `viewModelScope` for ViewModel coroutines (as used in this template):

```kotlin
class HomeViewModel : ViewModel() {
    // ✅ Good: Automatically cancelled when ViewModel cleared
    fun loadData() {
        viewModelScope.launch {
            repository.fetchData()
        }
    }

    // ❌ Bad: GlobalScope never cancelled
    fun loadDataBad() {
        GlobalScope.launch {
            repository.fetchData()  // Leaked coroutine
        }
    }
}
```

### WorkManager and Hilt

When using WorkManager with Hilt, use `HiltWorker` (as implemented in
`sync/src/main/kotlin/dev/atick/sync/worker/SyncWorker.kt`):

```kotlin
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: SyncRepository  // Injected, no leak
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return repository.sync()
    }
}
```

## R8 and ProGuard Optimization

### Project Structure for Obfuscation

The project follows a consistent pattern for data models to simplify ProGuard/R8 rules:

```
- `feature/your-feature/model/` - Models kept unobfuscated
- `core/network/model/` - Data models kept unobfuscated
```

### ProGuard and Consumer Rules

If your app works in `debug` build but not in `release` build that typically indicates obfuscation
issues. In that case you need to add or edit the proguard rules. These can be found in
`<module>/proguard-rules.pro` or `<module>/consumer-rules.pro` files. For example:

```proguard
# Keep all models
-keep class **.model.** { *; }

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
```

> [!NOTE]
> The project's model organization makes it easy to keep data models unobfuscated while allowing
> safe obfuscation of implementation classes.

## Startup Optimization

Reducing app startup time improves user experience and metrics.

### Lazy Initialization

Defer non-critical initialization (as implemented in `App.kt:40`):

```kotlin
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Debug logging initialized only in debug builds
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        // Critical components initialized through Hilt
        // Non-critical initialization deferred or lazy-loaded
    }
}
```

### Optimize Dependency Injection

Hilt creates the dependency graph at startup. Use `@Binds` instead of `@Provides` when possible (as
used throughout this template):

```kotlin
// ✅ Good: @Binds is more efficient (used in data/src/main/kotlin/dev/atick/data/di/RepositoryModule.kt)
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository
}

// ❌ Less efficient: @Provides requires more codegen
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun provideRepository(impl: RepositoryImpl): Repository = impl
}
```

## Gradle Build Optimization

### Build Scan

The project includes Gradle Enterprise build scan support (see `settings.gradle.kts`):

```kotlin
plugins {
    id("com.gradle.develocity") version ("3.19.1")
}

develocity {
    buildScan {
        publishing.onlyIf { !System.getenv("CI").isNullOrEmpty() }
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
    }
}
```

Use build scans to:

- Identify slow tasks
- Find configuration issues
- Optimize dependency resolution

### Multi-Module Build Optimization

Take advantage of the project's modular structure:

1. **Make Module**: Instead of rebuilding the entire project, use Make Module:
    - Android Studio: Right-click module → Make Module
    - Command Line: `./gradlew :module:name:assembleDebug`

2. **Parallel Execution**: Enabled in `gradle.properties`:
   ```properties
   org.gradle.parallel=true
   ```

3. **Configuration Caching**: Already enabled for supported tasks

> [!TIP]
> When working on a feature, use Make Module on just that feature's module to significantly reduce
> build time.

## Baseline Profiles

> [!NOTE]
> Baseline profile support is coming soon to improve app startup performance.

### Planned Implementation

```kotlin
// build.gradle.kts
androidApplication {
    baselineProfile {
        automaticGenerationDuringBuild = true
    }
}
```

This will include:

- Startup trace collection
- Critical user path optimization
- Ahead-of-time compilation for key paths

## Firebase Performance Monitoring

This template includes Firebase Performance Monitoring setup through the Firebase convention
plugin (see
`build-logic/convention/src/main/kotlin/dev/atick/convention/firebase/FirebaseConventionPlugin.kt`):

```kotlin
class FirebaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.firebase.firebase-perf")
            }

            dependencies {
                "implementation"(libs.findLibrary("firebase.perf").get())
            }
        }
    }
}
```

This gives you automatic monitoring of:

- Network requests
- App startup time
- Screen render time
- Frozen frames

You can also add custom traces:

```kotlin
FirebasePerformance.getInstance().newTrace("custom_operation").apply {
    start()
    // Your code here
    stop()
}
```

> [!TIP]
> Use the Firebase Console to view performance metrics and identify bottlenecks in your app.

## Profiling and Monitoring

### Android Studio Profilers

Use Android Studio's built-in profilers:

1. **CPU Profiler**: Identify expensive operations
    - Run → Profile 'app'
    - Record CPU activity during critical operations
    - Look for long-running methods

2. **Memory Profiler**: Detect memory issues
    - Monitor memory allocation during scrolling
    - Take heap dumps to find leaked objects
    - Check for unnecessary object creation

3. **Layout Inspector**: Analyze compose hierarchy
    - Tools → Layout Inspector
    - Check recomposition counts
    - Identify unnecessary recompositions

> [!IMPORTANT]
> Always profile your app's performance using Android Studio's CPU Profiler and Layout Inspector
> before and after optimizations to ensure they're effective.

## Further Reading

- [Useful Tips & Tricks](tips.md): Get useful tips for development and debugging
- [CI/CD Setup](github.md): Set up continuous integration and deployment for the project
- [Publishing to Play Store](fastlane.md): Learn how to publish your app to the Google Play Store
