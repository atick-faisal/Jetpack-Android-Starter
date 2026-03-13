# Adding a New Feature

This guide walks you through the complete process of adding a new feature to the app, following
established patterns and best practices.

---

## Summary

To add a new feature to this template, follow this workflow:

1. **Define data models** - Create network, database, and repository models
2. **Create data sources** - Implement network and local data sources
3. **Create repository** - Coordinate data sources and handle errors
4. **Create UI layer** - Build ViewModel with Screen Data and composables
5. **Set up navigation** - Define type-safe routes and navigation extensions
6. **Configure DI** - Wire everything together with Hilt modules

This guide provides complete code examples for each step, showing the exact patterns used in this
template.

---

## Overview of Steps

1. [Define data models](#step-1-data-models)
2. [Create/update data sources](#step-2-data-sources)
3. [Create repository layer](#step-3-repository-layer)
4. [Create UI layer](#step-4-ui-layer)
5. [Set up navigation](#step-5-navigation)
6. [Configure dependency injection](#step-6-dependency-injection)

---

## Step 1: Data Models

---

### 1.1 Data Source Models

Create models in the appropriate core module (for example, `core:network` or `core:room`). Create
new core modules if needed:

```kotlin
// core/network/src/main/kotlin/dev/atick/core/network/model/NetworkFeature.kt
@Serializable
data class NetworkFeature(
    val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

// core/room/src/main/kotlin/dev/atick/core/room/model/FeatureEntity.kt
@Entity(tableName = "features")
data class FeatureEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lastSynced: Long = 0,
    val needsSync: Boolean = true
)
```

---

## Step 2: Data Sources

---

### 2.1 Network Data Source

```kotlin
// core/network/src/main/kotlin/dev/atick/core/network/data/
interface NetworkDataSource {
    suspend fun getFeatures(): List<NetworkFeature>
    suspend fun createFeature(feature: NetworkFeature)
}

class NetworkDataSourceImpl @Inject constructor(
    private val api: FeatureApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NetworkDataSource {
    override suspend fun getFeatures(): List<NetworkFeature> =
        withContext(ioDispatcher) {
            api.getFeatures()
        }

    override suspend fun createFeature(feature: NetworkFeature) =
        withContext(ioDispatcher) {
            api.createFeature(feature)
        }
}
```

### 2.2 Local Data Source

```kotlin
// core/room/src/main/kotlin/dev/atick/core/room/data/
interface LocalDataSource {
    fun observeFeatures(): Flow<List<FeatureEntity>>
    suspend fun saveFeatures(features: List<FeatureEntity>)
}

class LocalDataSourceImpl @Inject constructor(
    private val featureDao: FeatureDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LocalDataSource {
    override fun observeFeatures(): Flow<List<FeatureEntity>> =
        featureDao.observeFeatures()
            .flowOn(ioDispatcher)

    override suspend fun saveFeatures(features: List<FeatureEntity>) =
        withContext(ioDispatcher) {
            featureDao.insertFeatures(features)
        }
}
```

> [!NOTE]
> Always use `withContext(ioDispatcher)` in data sources to ensure operations run on the IO thread.

---

## Step 3: Repository Layer

---

### 3.1 Feature Models

```kotlin
// data/src/main/kotlin/dev/atick/data/model/Feature.kt
data class Feature(
    val id: String,
    val name: String,
    val lastSynced: Long = 0
)

// Conversion Functions
fun NetworkFeature.toFeature() = Feature(
    id = id,
    name = name
)

fun FeatureEntity.toFeature() = Feature(
    id = id,
    name = name,
    lastSynced = lastSynced
)

fun Feature.toEntity() = FeatureEntity(
    id = id,
    name = name,
    lastSynced = lastSynced
)
```

### 3.2 Repository Implementation

```kotlin
// data/src/main/kotlin/dev/atick/data/repository/
interface FeatureRepository {
    fun observeFeatures(): Flow<List<Feature>>
    suspend fun createFeature(feature: Feature): Result<Unit>
    suspend fun syncFeatures(): Result<Unit>
}

class FeatureRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val networkDataSource: NetworkDataSource
) : FeatureRepository {
    override fun observeFeatures(): Flow<List<Feature>> =
        localDataSource.observeFeatures()
            .map { entities -> entities.map { it.toFeature() } }

    override suspend fun createFeature(feature: Feature): Result<Unit> =
        suspendRunCatching {
            networkDataSource.createFeature(feature.toNetworkFeature())
            localDataSource.saveFeature(feature.toEntity())
        }

    override suspend fun syncFeatures(): Result<Unit> =
        suspendRunCatching {
            val networkFeatures = networkDataSource.getFeatures()
            val entities = networkFeatures.map { it.toFeature().toEntity() }
            localDataSource.saveFeatures(entities)
        }
}
```

> [!TIP]
> Use `suspendRunCatching` in repositories to handle errors consistently.

> [!NOTE]
> This is a minimal repository example for the tutorial. For detailed repository patterns including
> offline-first with sync metadata, network-only, local-only, error handling, and caching strategies,
> see the [Data Module README](../data/README.md#repository-patterns).

---

## Step 4: UI Layer

---

### 4.1 Screen Data

```kotlin
// feature/feature-name/src/main/kotlin/dev/atick/feature/model/
data class FeatureScreenData(
    val features: List<Feature> = emptyList(),
    val newFeatureName: String = ""
)
```

### 4.2 ViewModel

```kotlin
// feature/feature-name/src/main/kotlin/dev/atick/feature/ui/
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val featureRepository: FeatureRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState(FeatureScreenData()))
    val uiState = _uiState.asStateFlow()

    init {
        observeFeatures()
    }

    private fun observeFeatures() {
        featureRepository.observeFeatures()
            .onEach { features ->
                _uiState.updateState {
                    copy(features = features)
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateFeatureName(name: String) {
        _uiState.updateState {
            copy(newFeatureName = name)
        }
    }

    fun createFeature() {
        _uiState.updateStateWith {
            val feature = Feature(
                id = UUID.randomUUID().toString(),
                name = newFeatureName
            )
            featureRepository.createFeature(feature)
        }
    }
}
```

### 4.3 UI Components

```kotlin
// feature/feature-name/src/main/kotlin/dev/atick/feature/ui/
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
            onNameChange = viewModel::updateFeatureName,
            onCreateFeature = viewModel::createFeature
        )
    }
}

@Composable
private fun FeatureScreen(
    screenData: FeatureScreenData,
    onNameChange: (String) -> Unit,
    onCreateFeature: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // UI Implementation
    }
}
```

---

## Step 5: Navigation

This section shows the minimal navigation code needed when adding a new feature. For comprehensive
navigation patterns and best practices, see the [Navigation Deep Dive](navigation.md) guide.

---

```kotlin
// feature/feature-name/src/main/kotlin/dev/atick/feature/navigation/
@Serializable
data object FeatureNavGraph

@Serializable
data object Feature

fun NavController.navigateToFeature(
    navOptions: NavOptions? = null
) {
    navigate(Feature, navOptions)
}

fun NavGraphBuilder.featureScreen(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean
) {
    composable<Feature> {
        FeatureRoute(onShowSnackbar = onShowSnackbar)
    }
}

fun NavGraphBuilder.featureNavGraph(
    nestedGraphs: NavGraphBuilder.() -> Unit
) {
    navigation<FeatureNavGraph>(
        startDestination = Feature
    ) {
        nestedGraphs()
    }
}
```

---

## Step 6: Dependency Injection

---

### 6.1 Data Source Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun bindNetworkDataSource(
        impl: NetworkDataSourceImpl
    ): NetworkDataSource
}
```

### 6.2 Repository Module

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

---

## Best Practices Reminder

**Data Sources:**

- Use `withContext(ioDispatcher)` for IO operations
- Handle raw data models
- One responsibility per data source

**Repositories:**

- Use `suspendRunCatching` for error handling
- Convert between data models
- Coordinate between data sources

**ViewModels:**

- Use `updateState`, `updateStateWith`, and `updateWith` utilities
- Handle UI logic and state management
- Convert to Screen Data

**UI Components:**

- Use `StatefulComposable` for consistent loading/error handling
- Keep composables pure and state-driven
- Separate route from screen implementation

> [!IMPORTANT]
> Always follow the unidirectional data flow pattern: UI Events → ViewModel → Repository → Data
> Sources → Back to UI through StateFlow.

---

## Testing

> [!NOTE]
> Testing infrastructure is upcoming. The template doesn't currently include test examples, but you
> should add tests for production apps.

Remember to add tests for your new feature:

**Data Source Tests** - Test IO operations

**Repository Tests** - Test business logic

**ViewModel Tests** - Test state management

**UI Tests** - Test composables

**Integration Tests** - Test full feature flow

---

## Further Reading

- [Architecture Overview](architecture.md) - Understand the app's architecture
- [Design Philosophy](philosophy.md) - Learn about design principles
- [State Management](state-management.md) - Deep dive into UiState patterns
- [Data Flow](data-flow.md) - Understand data flow patterns
- [Navigation](navigation.md) - Type-safe navigation guide
- [Dependency Injection](dependency-injection.md) - Complete DI guide
- [Firebase Setup Guide](firebase.md) - Add Firebase to your app
- [Dependency Management](dependency.md) - Version catalogs and configurations
