# Adding a New Feature

This guide walks through the process of adding a new feature to the app, following our established
patterns and best practices.

## Overview of Steps

1. Define data models
2. Create/update data sources
3. Create repository layer
4. Create UI layer
5. Set up navigation
6. Configure dependency injection

## Step 1: Data Models

### 1.1 Data Source Models

Create models in appropriate core module (for example, `core:network` or `core:room`). Feel free to
create your own if its not present in the template:

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

## Step 2: Data Sources

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

## Step 3: Repository Layer

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

## Step 4: UI Layer

### 4.1 UI Models

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

## Step 5: Navigation

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

## Step 6: Dependency Injection

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

## Best Practices Reminder

1. **Data Sources**:
	- Use `withContext(ioDispatcher)` for IO operations
	- Handle raw data models
	- One responsibility per data source

2. **Repositories**:
	- Use `suspendRunCatching` for error handling
	- Convert between data models
	- Coordinate between data sources

3. **ViewModels**:
	- Use `updateState` and `updateStateWith` utilities
	- Handle UI logic and state management
	- Convert to UI models

4. **UI Components**:
	- Use `StatefulComposable` for consistent loading/error handling
	- Keep composables pure and state-driven
	- Separate route from screen implementation

> [!IMPORTANT]
> Always follow the unidirectional data flow pattern: UI Events → ViewModel → Repository → Data
> Sources → Back to UI through StateFlow.

## Testing (Upcoming 🚧)

Remember to add tests for your new feature:

1. **Data Source Tests**: Test IO operations
2. **Repository Tests**: Test business logic
3. **ViewModel Tests**: Test state management
4. **UI Tests**: Test composables
5. **Integration Tests**: Test full feature flow

## Further Reading
- [Firebase Setup Guide](firebase.md): Add Firebase to your app
- [Dependency Management](dependency.md): Learn about version catalogs and plugin configurations
- [Architecture Overview](architecture.md): Understand the app's architecture
- [Design Philosophy](philosophy.md): Learn about design principles
