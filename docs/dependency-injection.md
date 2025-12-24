# Dependency Injection Guide

This guide covers how dependency injection (DI) works in this template using **Dagger Hilt**. All
examples are based on the actual implementation patterns used throughout the codebase.

## Overview

This template uses **Dagger Hilt** for dependency injection, which is the recommended DI solution
for Android. Hilt is built on top of Dagger and provides:

- Simplified setup with convention-based configuration
- Integration with Android components (Activity, ViewModel, Worker, etc.)
- Compile-time safety and performance
- Automatic dependency graph generation
- Scoping strategies for managing object lifecycles

## Quick Start

### 1. Enable Hilt in Your Module

All modules that need DI apply the Hilt convention plugin:

```kotlin
// feature/home/build.gradle.kts
plugins {
    alias(libs.plugins.jetpack.ui.library)
    alias(libs.plugins.jetpack.dagger.hilt) // Add this
    alias(libs.plugins.jetpack.dokka)
}
```

The `jetpack.dagger.hilt` convention plugin automatically:

- Applies `com.google.dagger.hilt.android` plugin
- Applies `com.google.devtools.ksp` plugin
- Adds Hilt runtime and compiler dependencies

### 2. Annotate Your Application Class

The `Application` class must be annotated with `@HiltAndroidApp`:

```kotlin
@HiltAndroidApp
class JetpackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Hilt is automatically initialized before this
    }
}
```

### 3. Inject into Android Components

Use `@AndroidEntryPoint` on Activities, Fragments, etc., and `@Inject` for field injection:

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Inject
    lateinit var crashReporter: CrashReporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dependencies are injected before onCreate()
        // You can use networkUtils and crashReporter here
    }
}
```

### 4. Inject into ViewModels

Use `@HiltViewModel` and constructor injection:

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    // Repository is automatically injected
}
```

## Core Concepts

### @Binds vs @Provides

Hilt modules use two different methods for providing dependencies:

#### @Binds (Preferred for Interfaces)

Use `@Binds` when you have an interface and a single implementation. It's more efficient than
`@Provides` because it generates less code.

**Pattern:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    internal abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
```

**Real Example from `data/src/main/kotlin/dev/atick/data/di/RepositoryModule.kt`:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    internal abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    internal abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl,
    ): HomeRepository

    // ... more bindings
}
```

**When to use:**

- Binding interfaces to implementations
- When the implementation has `@Inject constructor`
- When you don't need any custom logic to create the object

#### @Provides (For Complex Construction)

Use `@Provides` when you need custom logic to create objects, or when the type can't have `@Inject`
on its constructor (e.g., third-party libraries, builders).

**Real Example from `core/room/src/main/kotlin/dev/atick/core/room/di/DatabaseModule.kt`:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val ROOM_DATABASE_NAME = "dev.atick.jetpack.room"

    @Singleton
    @Provides
    fun provideRoomDatabase(
        @ApplicationContext appContext: Context,
    ): JetpackDatabase {
        return Room.databaseBuilder(
            appContext,
            JetpackDatabase::class.java,
            ROOM_DATABASE_NAME,
        ).fallbackToDestructiveMigration(true).build()
    }
}
```

**Real Example
from `core/network/src/main/kotlin/dev/atick/core/network/di/retrofit/RetrofitModule.kt`:**

```kotlin
@Module(
    includes = [
        OkHttpClientModule::class,
    ],
)
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Singleton
    @Provides
    fun provideRetrofitClient(
        converterFactory: Converter.Factory,
        okHttpClient: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_URL)
            .addConverterFactory(converterFactory)
            .client(okHttpClient)
            .build()
    }
}
```

**When to use:**

- Creating objects with builders (Retrofit, Room, OkHttp)
- Third-party library types
- Objects requiring complex initialization logic
- When you need to use `@ApplicationContext` or other qualifiers

### Scoping

Scoping controls the lifecycle of dependencies. This template uses three main scopes:

#### @Singleton (Application Scope)

Objects live as long as the application process.

**Example:**

```kotlin
@Binds
@Singleton
internal abstract fun bindAuthRepository(
    authRepositoryImpl: AuthRepositoryImpl,
): AuthRepository
```

**Use for:**

- Repositories
- Data sources (Room DAOs, Retrofit services, DataStore)
- Network clients (OkHttp, Retrofit)
- Utilities (CrashReporter, NetworkUtils)

#### @ViewModelScoped

Objects live as long as the ViewModel. Useful for dependencies that should be recreated when the
ViewModel is recreated.

**Example:**

```kotlin
@Provides
@ViewModelScoped
fun provideSpecialUseCase(): SpecialUseCase {
    return SpecialUseCase()
}
```

#### @ActivityScoped

Objects live as long as the Activity. Less commonly used in this template.

### Qualifier Annotations

Qualifiers differentiate between multiple instances of the same type.

**Real Example from `core/android/src/main/kotlin/dev/atick/core/di/DispatcherModule.kt`:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher
```

**Usage in Data Source:**

```kotlin
class NetworkDataSourceImpl @Inject constructor(
    private val restApi: RestApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : NetworkDataSource {

    override suspend fun getData(): List<JetpackDto> = withContext(ioDispatcher) {
        restApi.getData()
    }
}
```

**When to create qualifiers:**

- Multiple instances of the same type with different configurations
- Different implementations of the same interface for different purposes
- Named instances (like different dispatchers, different databases)

## Injection Patterns

### Constructor Injection (Preferred)

This is the most common and recommended pattern. Works for:

- ViewModels (`@HiltViewModel`)
- Repositories
- Data sources
- Any class you control

**ViewModel Example:**

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    // Use settingsRepository
}
```

**Repository Example:**

```kotlin
internal class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : AuthRepository {
    // Use data sources
}
```

**Data Source Example:**

```kotlin
class FirebaseDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FirebaseDataSource {
    // Use firestore and ioDispatcher
}
```

### Field Injection

Used for Android framework components where you can't control constructor.

**Activity Example from `app/src/main/kotlin/dev/atick/compose/MainActivity.kt`:**

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Inject
    lateinit var crashReporter: CrashReporter

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // networkUtils and crashReporter are now injected
    }
}
```

> [!IMPORTANT]
> **Field Injection Requirements:**
> - Use `lateinit var` for field injection
> - Fields must not be private
> - Injection happens before `onCreate()` for Activities
> - For ViewModels, use `by viewModels()` delegate (Hilt integration)

### AssistedInject (For Workers)

WorkManager Workers need special handling because they receive runtime parameters from WorkManager.

**Real Example from `sync/src/main/kotlin/dev/atick/sync/worker/SyncWorker.kt`:**

```kotlin
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val homeRepository: HomeRepository,
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        // Use injected dependencies
        return withContext(ioDispatcher) {
            homeRepository.sync()
            Result.success()
        }
    }
}
```

**Key Points:**

- Use `@HiltWorker` annotation
- Use `@AssistedInject` for constructor
- Mark WorkManager-provided params with `@Assisted`
- Regular dependencies are injected normally

> [!NOTE]
> You must also use `DelegatingWorker` when enqueuing:

```kotlin
fun startUpSyncWork(): OneTimeWorkRequest {
    return OneTimeWorkRequestBuilder<DelegatingWorker>()
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .setConstraints(SyncConstraints)
        .setInputData(SyncWorker::class.delegatedData())
        .build()
}
```

## Module Organization

Modules are organized by layer and purpose. Each module contains a `di/` package with Hilt modules.

### Core Module DI Structure

- `core/android/src/main/kotlin/dev/atick/core/di/`
    - `DispatcherModule.kt` - Coroutine dispatchers
    - `CoroutineModule.kt` - CoroutineScope
    - `StringDecoderModule.kt` - URI decoder
- `core/network/src/main/kotlin/dev/atick/core/network/di/`
    - `retrofit/`
        - `RetrofitModule.kt` - Retrofit instance
        - `ConverterModule.kt` - JSON converter
    - `okhttp/`
        - `OkHttpClientModule.kt` - OkHttp client
        - `InterceptorModule.kt` - Interceptors
    - `coil/`
        - `CoilModule.kt` - Image loader
    - `NetworkUtilsModule.kt` - Network utilities
    - `DataSourceModule.kt` - Network data source
- `core/room/src/main/kotlin/dev/atick/core/room/di/`
    - `DatabaseModule.kt` - Room database
    - `DaoModule.kt` - DAOs
    - `DataSourceModule.kt` - Local data source
- `core/preferences/src/main/kotlin/dev/atick/core/preferences/di/`
    - `DatastoreModule.kt` - DataStore
    - `PreferencesDataSourceModule.kt` - Preferences data source

### Data Module DI Structure

- `data/src/main/kotlin/dev/atick/data/di/`
    - `RepositoryModule.kt` - All repository bindings

### Firebase Module DI Structure

- `firebase/analytics/src/main/kotlin/dev/atick/firebase/analytics/di/`
    - `FirebaseModule.kt` - Firebase Analytics
    - `CrashlyticsModule.kt` - CrashReporter
- `firebase/auth/src/main/kotlin/dev/atick/firebase/auth/di/`
    - `FirebaseAuthModule.kt` - Firebase Auth
    - `CredentialManagerModule.kt` - Credential Manager
    - `DataSourceModule.kt` - Auth data source
- `firebase/firestore/src/main/kotlin/dev/atick/firebase/firestore/di/`
    - `FirebaseModule.kt` - Firestore instance
    - `DataSourceModule.kt` - Firestore data source

### Sync Module DI Structure

- `sync/src/main/kotlin/dev/atick/sync/di/`
    - `SyncModule.kt` - SyncManager binding

### Module Inclusion Pattern

Modules can include other modules to establish dependencies:

**Example from `core/room/di/DaoModule.kt`:**

```kotlin
@Module(
    includes = [
        DatabaseModule::class,
    ],
)
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Singleton
    @Provides
    fun provideJetpackDao(jetpackDatabase: JetpackDatabase) =
        jetpackDatabase.getJetpackDao()
}
```

This ensures `DatabaseModule` is processed before `DaoModule`, so the database is available when
creating the DAO.

## Common Patterns

### Pattern 1: Repository with Multiple Data Sources

**Real Example from `data/src/main/kotlin/dev/atick/data/repository/auth/AuthRepositoryImpl.kt`:**

```kotlin
internal class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : AuthRepository {

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String,
    ): Result<Unit> {
        return suspendRunCatching {
            val user = authDataSource.signInWithEmailAndPassword(email, password)
            userPreferencesDataSource.setUserProfile(user.asPreferencesUserProfile())
        }
    }
}
```

**Binding:**

```kotlin
@Binds
@Singleton
internal abstract fun bindAuthRepository(
    authRepositoryImpl: AuthRepositoryImpl,
): AuthRepository
```

### Pattern 2: DataStore with Dispatcher

**Real Example from `core/preferences/di/DatastoreModule.kt`:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatastoreModule {

    private const val DATA_STORE_FILE_NAME = "user_preferences.json"

    @Singleton
    @Provides
    fun providePreferencesDataStore(
        @ApplicationContext appContext: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): DataStore<UserDataPreferences> {
        return DataStoreFactory.create(
            serializer = UserDataSerializer,
            produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) },
            scope = CoroutineScope(ioDispatcher + SupervisorJob()),
        )
    }
}
```

**Key points:**

- Uses `@ApplicationContext` qualifier for Context
- Uses `@IoDispatcher` qualifier for CoroutineDispatcher
- Creates DataStore with custom scope on IO dispatcher

### Pattern 3: Android System Service

**Real Example from `firebase/auth/di/CredentialManagerModule.kt`:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object CredentialManagerModule {

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager {
        return CredentialManager.create(context)
    }
}
```

### Pattern 4: Third-Party Library with Builder

**Real Example from Retrofit setup:**

```kotlin
@Module(includes = [OkHttpClientModule::class])
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Singleton
    @Provides
    fun provideRetrofitClient(
        converterFactory: Converter.Factory,
        okHttpClient: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_URL)
            .addConverterFactory(converterFactory)
            .client(okHttpClient)
            .build()
    }
}
```

## Convention Plugin Pattern

This template uses a **Gradle Convention Plugin** to simplify Hilt setup across modules.

**Convention Plugin (`build-logic/convention/src/main/kotlin/DaggerHiltConventionPlugin.kt`):**

```kotlin
class DaggerHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
            }

            dependencies {
                "implementation"(libs.findLibrary("dagger.hilt.android").get())
                "ksp"(libs.findLibrary("dagger.hilt.compiler").get())
            }
        }
    }
}
```

**Usage in `feature/home/build.gradle.kts`:**

```kotlin
plugins {
    alias(libs.plugins.jetpack.ui.library)
    alias(libs.plugins.jetpack.dagger.hilt) // One line to add Hilt
    alias(libs.plugins.jetpack.dokka)
}
```

**Benefits:**

- Consistent Hilt setup across all modules
- Single source of truth for Hilt configuration
- Easy to update Hilt version across the entire project
- Reduces boilerplate in build files

## Testing with Hilt

### Unit Testing Repositories and ViewModels

For unit tests, you can use test doubles (fakes or mocks) instead of real implementations.

**Example (Not in codebase, but standard pattern):**

```kotlin
class AuthRepositoryTest {

    private lateinit var repository: AuthRepository
    private val fakeAuthDataSource = FakeAuthDataSource()
    private val fakePreferencesDataSource = FakeUserPreferencesDataSource()

    @Before
    fun setup() {
        repository = AuthRepositoryImpl(
            authDataSource = fakeAuthDataSource,
            userPreferencesDataSource = fakePreferencesDataSource
        )
    }

    @Test
    fun signIn_success_savesUserToPreferences() = runTest {
        // Test implementation
    }
}
```

**Key points:**

- No Hilt in unit tests
- Use constructor injection (makes testing easier)
- Inject fake/mock implementations manually

### Integration Testing with Hilt

For Android instrumentation tests, use Hilt's testing library.

**Example (Not in codebase, but standard pattern):**

```kotlin
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class FeatureIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val repository: AuthRepository = FakeAuthRepository()

    @Test
    fun testFeature() {
        // Test with fake repository
    }
}
```

## Troubleshooting

### Common Issues

#### 1. "Cannot find symbol: DaggerApplicationComponent"

**Cause:** Hilt annotation processor hasn't run yet.

**Solution:**

- Build the project: `./gradlew build`
- Clean and rebuild: `./gradlew clean build`
- Ensure `@HiltAndroidApp` is on your Application class

#### 2. "Missing binding for [Type]"

**Cause:** No Hilt module provides this type.

**Solution:**

- Create a module with `@Provides` or `@Binds` for the type
- If using `@Binds`, ensure the implementation has `@Inject constructor`
- Check that the module is installed in the correct component (`@InstallIn`)

**Example:**

```kotlin
// Problem: AuthRepository not bound
@Binds
@Singleton
internal abstract fun bindAuthRepository(
    authRepositoryImpl: AuthRepositoryImpl,
): AuthRepository
```

#### 3. "Injected field must not be private"

**Cause:** Field injection doesn't work with private fields.

**Solution:**

```kotlin
// ❌ Wrong
@Inject
private lateinit var repository: Repository

// ✅ Correct
@Inject
lateinit var repository: Repository
```

#### 4. "@Inject constructor required" (when using @Binds)

**Cause:** `@Binds` requires the implementation to have `@Inject constructor`.

**Solution:**

```kotlin
// ❌ Wrong
class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource,
) : AuthRepository

// ✅ Correct
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
) : AuthRepository
```

#### 5. "Scoping mismatch"

**Cause:** Dependency has a shorter scope than the class requesting it.

**Example of problem:**

```kotlin
@Singleton
class Repository @Inject constructor(
    @ActivityScoped val activityDep: ActivityDependency  // ❌ Wrong
)
```

**Solution:** Ensure dependencies have equal or longer scopes:

- `@Singleton` can depend on `@Singleton`
- `@ActivityScoped` can depend on `@Singleton` or `@ActivityScoped`
- `@ViewModelScoped` can depend on `@Singleton` or `@ViewModelScoped`

#### 6. KSP not generating code

**Cause:** KSP configuration issue or cache corruption.

**Solution:**

- Ensure convention plugin applies KSP: `apply("com.google.devtools.ksp")`
- Invalidate caches and restart Android Studio
- Clean build directory: `./gradlew clean`
- Check KSP version compatibility with Kotlin version

#### 7. Circular dependency error

**Cause:** Two classes depend on each other.

**Example:**

```kotlin
class A @Inject constructor(val b: B)
class B @Inject constructor(val a: A)  // Circular!
```

**Solutions:**

- Use `Lazy<T>` for one dependency
- Use `Provider<T>` for one dependency
- Refactor to break the cycle (extract common logic)

**Example fix:**

```kotlin
class A @Inject constructor(val b: Lazy<B>)  // Break cycle with Lazy
class B @Inject constructor(val a: A)
```

## Best Practices

### Do's

1. **Use constructor injection** whenever possible
   ```kotlin
   class Repository @Inject constructor(
       private val dataSource: DataSource
   ) : RepositoryInterface
   ```

2. **Prefer @Binds over @Provides** for interface bindings
   ```kotlin
   @Binds
   abstract fun bindRepository(impl: RepositoryImpl): Repository
   ```

3. **Use qualifiers for multiple instances** of the same type
   ```kotlin
   @IoDispatcher private val ioDispatcher: CoroutineDispatcher
   ```

4. **Scope appropriately** based on lifecycle needs
   ```kotlin
   @Singleton  // Lives as long as app
   @Binds
   abstract fun bindRepository(impl: RepositoryImpl): Repository
   ```

5. **Organize modules by layer** (following the existing pattern)
    - `core/*/di/` for core infrastructure
    - `data/di/` for repositories
    - `firebase/*/di/` for Firebase integrations

6. **Use `internal` for implementation bindings**
   ```kotlin
   @Binds
   @Singleton
   internal abstract fun bindAuthRepository(
       authRepositoryImpl: AuthRepositoryImpl,
   ): AuthRepository
   ```

7. **Inject dispatchers** instead of hardcoding `Dispatchers.IO`
   ```kotlin
   class DataSource @Inject constructor(
       @IoDispatcher private val ioDispatcher: CoroutineDispatcher
   ) {
       suspend fun getData() = withContext(ioDispatcher) { /* ... */ }
   }
   ```

### Don'ts

1. **Don't use field injection** when constructor injection is possible
   ```kotlin
   // ❌ Avoid
   class Repository {
       @Inject lateinit var dataSource: DataSource
   }

   // ✅ Prefer
   class Repository @Inject constructor(
       private val dataSource: DataSource
   )
   ```

2. **Don't inject Android components** (Context, Activity, etc.) into Singletons
   ```kotlin
   // ❌ Wrong - Activity will leak
   @Singleton
   class BadRepository @Inject constructor(private val activity: Activity)

   // ✅ Correct - Use ApplicationContext
   @Singleton
   class GoodRepository @Inject constructor(
       @ApplicationContext private val context: Context
   )
   ```

3. **Don't hardcode Dispatchers**
   ```kotlin
   // ❌ Wrong
   withContext(Dispatchers.IO) { /* ... */ }

   // ✅ Correct
   @Inject constructor(@IoDispatcher private val ioDispatcher: CoroutineDispatcher)
   withContext(ioDispatcher) { /* ... */ }
   ```

4. **Don't over-scope** (don't make everything `@Singleton` unnecessarily)

5. **Don't create modules in feature packages** - follow the `di/` package pattern

6. **Don't mix Hilt with other DI frameworks** (like Koin, manual DI)

## Advanced Topics

### Custom Components (Not Used in This Template)

Hilt allows creating custom components for specific lifecycles. This template doesn't use custom
components, but they can be useful for:

- Fragment-specific dependencies
- Service-specific dependencies
- Custom lifecycle scopes

### Entry Points

If you need to inject dependencies into a class that Hilt doesn't support, use `@EntryPoint`.

**Example (not in codebase):**

```kotlin
@EntryPoint
@InstallIn(SingletonComponent::class)
interface MyEntryPoint {
    fun repository(): Repository
}

// In non-Hilt class
val entryPoint = EntryPointAccessors.fromApplication(
    context.applicationContext,
    MyEntryPoint::class.java
)
val repository = entryPoint.repository()
```

### Multibindings

For providing multiple implementations of the same interface (e.g., list of plugins, interceptors).

**Example (not in codebase):**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class InterceptorModule {

    @Binds
    @IntoSet
    abstract fun bindLoggingInterceptor(impl: LoggingInterceptor): Interceptor

    @Binds
    @IntoSet
    abstract fun bindAuthInterceptor(impl: AuthInterceptor): Interceptor
}

// Inject
class Client @Inject constructor(
    private val interceptors: Set<@JvmSuppressWildcards Interceptor>
)
```

## Migration Guide

If you're adding Hilt to an existing project or module:

### Step 1: Add Convention Plugin

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.jetpack.dagger.hilt)
}
```

### Step 2: Annotate Application Class

```kotlin
@HiltAndroidApp
class YourApplication : Application()
```

### Step 3: Create Modules

- Create `di/` package in each module
- Create Hilt modules for existing dependencies
- Use `@Binds` for interfaces, `@Provides` for complex types

### Step 4: Migrate Injection

- Replace manual injection with `@Inject constructor`
- Annotate Activities with `@AndroidEntryPoint`
- Annotate ViewModels with `@HiltViewModel`

### Step 5: Test

- Build the project
- Verify all dependencies are injected
- Run tests to ensure nothing broke

## Further Reading

- [Dagger Hilt Official Guide](https://dagger.dev/hilt/)
- [Hilt Codelab](https://developer.android.com/codelabs/android-hilt)
- [Dependency Injection on Android with Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [Hilt Testing Guide](https://developer.android.com/training/dependency-injection/hilt-testing)

## Summary

This template follows these DI principles:

1. **Convention over configuration** - Convention plugin simplifies setup
2. **Constructor injection first** - Easier to test, explicit dependencies
3. **Organized by layer** - Modules follow project structure
4. **Scoped appropriately** - Most dependencies are `@Singleton`
5. **Qualifier annotations** - Used for dispatchers and multiple instances
6. **Compile-time safety** - Hilt validates dependency graph at build time

By following these patterns, you'll have a maintainable, testable, and efficient DI setup that
scales with your project.
