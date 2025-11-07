# Data Flow Guide

This guide explains how data flows through the application layers, covering different architectural patterns for network-only, local-only, and offline-first (network + local) data sources.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Data Flow Patterns](#data-flow-patterns)
3. [Network-Only Pattern](#network-only-pattern)
4. [Local-Only Pattern](#local-only-pattern)
5. [Offline-First Pattern (Network + Local)](#offline-first-pattern-network--local)
6. [Real-Time Data Updates](#real-time-data-updates)
7. [Caching Strategies](#caching-strategies)
8. [Error Handling](#error-handling)

---

## Architecture Overview

### Two-Layer Architecture

This template intentionally uses a simplified **two-layer architecture** (UI + Data) instead of the traditional three-layer approach. There is **no domain layer** by design to reduce complexity.

```
┌─────────────────────────────────────────────────────────┐
│                   UI Layer (MVVM)                       │
│  ┌───────────────────────────────────────────────────┐  │
│  │  ViewModel                                        │  │
│  │  - Manages UI state (UiState<ScreenData>)        │  │
│  │  - Calls repositories directly                    │  │
│  │  - Transforms data for UI                         │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ▼
                    Result<T> / Flow<T>
                           ▼
┌─────────────────────────────────────────────────────────┐
│                   Data Layer                            │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Repository (Interface + Implementation)          │  │
│  │  - Coordinates data sources                       │  │
│  │  - Implements business logic                      │  │
│  │  - Returns Flow<T> for reactive data             │  │
│  │  - Returns Result<T> for one-shot operations     │  │
│  └───────────────────────────────────────────────────┘  │
│                           ▼                             │
│  ┌─────────────────┐  ┌─────────────────┐              │
│  │ LocalDataSource │  │NetworkDataSource│              │
│  │  (Room)         │  │  (Retrofit)     │              │
│  └─────────────────┘  └─────────────────┘              │
└─────────────────────────────────────────────────────────┘
```

### Unidirectional Data Flow

Data flows in **one direction** through the layers:

1. **User Interaction** → UI Layer
2. **ViewModel** → Calls Repository
3. **Repository** → Coordinates Data Sources (Room/Retrofit/Firebase/DataStore)
4. **Data Sources** → External Systems (Database/Network/Storage)
5. **Data** flows back → Repository → ViewModel → UI

### Key Principles

- **Single Source of Truth**: Local database (Room) is the source of truth for observable data
- **Repositories Expose Flow**: Observable data uses `Flow<T>`, one-shot operations use `Result<T>`
- **ViewModels Call Repositories Directly**: No domain layer, repositories contain business logic
- **Offline-First**: Local data is displayed immediately, network updates happen in background
- **Error Handling**: Use `Result<T>` for error propagation, `suspendRunCatching` for repository operations

---

## Data Flow Patterns

The template supports three main data flow patterns. Choose based on your feature requirements:

| Pattern | Use Case | Data Source | Example |
|---------|----------|-------------|---------|
| **Network-Only** | Non-cacheable data, always fresh | Retrofit API | Weather data, stock prices |
| **Local-Only** | User preferences, settings | Room or DataStore | Theme preference, auth token |
| **Offline-First** | Core app data, sync required | Room + Retrofit/Firebase | User posts, profile data |

---

## Network-Only Pattern

### When to Use

- Data must always be fresh (e.g., live sports scores, stock prices)
- Caching would provide stale or incorrect information
- Data is not critical for offline access

### Architecture

```
ViewModel → Repository → NetworkDataSource (Retrofit) → API
     ↑_______________|
       Result<T>
```

### Implementation

#### 1. Define Network Model

```kotlin
// core/network/src/main/kotlin/.../model/WeatherResponse.kt
@Serializable
data class WeatherResponse(
    val temperature: Double,
    val description: String,
    val humidity: Int
)
```

#### 2. Create Retrofit API Interface

```kotlin
// core/network/src/main/kotlin/.../api/WeatherApi.kt
interface WeatherApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("city") city: String
    ): WeatherResponse
}
```

#### 3. Implement Network Data Source

```kotlin
// core/network/src/main/kotlin/.../data/WeatherNetworkDataSource.kt
interface WeatherNetworkDataSource {
    suspend fun getCurrentWeather(city: String): Result<WeatherResponse>
}

internal class WeatherNetworkDataSourceImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : WeatherNetworkDataSource {
    override suspend fun getCurrentWeather(city: String): Result<WeatherResponse> {
        return withContext(ioDispatcher) {
            suspendRunCatching {
                weatherApi.getCurrentWeather(city)
            }
        }
    }
}
```

#### 4. Create Repository

```kotlin
// data/src/main/kotlin/.../repository/WeatherRepository.kt
interface WeatherRepository {
    suspend fun getCurrentWeather(city: String): Result<Weather>
}

internal class WeatherRepositoryImpl @Inject constructor(
    private val networkDataSource: WeatherNetworkDataSource
) : WeatherRepository {
    override suspend fun getCurrentWeather(city: String): Result<Weather> {
        return suspendRunCatching {
            networkDataSource.getCurrentWeather(city)
                .map { it.toDomain() }
                .getOrThrow()
        }
    }
}

// Extension function to convert network model to domain model
private fun WeatherResponse.toDomain() = Weather(
    temperature = temperature,
    description = description,
    humidity = humidity
)
```

#### 5. Use in ViewModel

```kotlin
// feature/weather/src/main/kotlin/.../WeatherViewModel.kt
data class WeatherScreenData(
    val weather: Weather? = null
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState(WeatherScreenData()))
    val uiState = _uiState.asStateFlow()

    fun loadWeather(city: String) {
        _uiState.updateStateWith {
            weatherRepository.getCurrentWeather(city).map { weather ->
                copy(weather = weather)
            }
        }
    }
}
```

### Network-Only Flow Diagram

```
User taps "Refresh"
        ↓
ViewModel.loadWeather()
        ↓
Repository.getCurrentWeather()
        ↓
NetworkDataSource → Retrofit → API
        ↓
Result<Weather>
        ↓
ViewModel updates UiState
        ↓
UI recomposes with new data
```

---

## Local-Only Pattern

### When to Use

- User preferences and settings
- Authentication tokens and session data
- Data that doesn't require network sync
- Small, simple key-value data

### Architecture

```
ViewModel → Repository → DataStore / Room → Local Storage
     ↑_______________|
       Flow<T>
```

### Implementation

#### 1. Define DataStore Schema (for Preferences)

```kotlin
// core/preferences/src/main/kotlin/.../UserPreferences.kt
data class UserPreferences(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val language: String = "en"
)

enum class ThemePreference {
    LIGHT, DARK, SYSTEM
}
```

#### 2. Create DataStore Data Source

```kotlin
// core/preferences/src/main/kotlin/.../UserPreferencesDataSource.kt
interface UserPreferencesDataSource {
    fun observePreferences(): Flow<UserPreferences>
    suspend fun updateTheme(theme: ThemePreference): Result<Unit>
    suspend fun toggleNotifications(enabled: Boolean): Result<Unit>
}

internal class UserPreferencesDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserPreferencesDataSource {

    override fun observePreferences(): Flow<UserPreferences> {
        return dataStore.data.map { preferences ->
            UserPreferences(
                theme = preferences[THEME_KEY]?.let { ThemePreference.valueOf(it) }
                    ?: ThemePreference.SYSTEM,
                notificationsEnabled = preferences[NOTIFICATIONS_KEY] ?: true,
                language = preferences[LANGUAGE_KEY] ?: "en"
            )
        }
    }

    override suspend fun updateTheme(theme: ThemePreference): Result<Unit> {
        return withContext(ioDispatcher) {
            suspendRunCatching {
                dataStore.edit { preferences ->
                    preferences[THEME_KEY] = theme.name
                }
            }
        }
    }

    override suspend fun toggleNotifications(enabled: Boolean): Result<Unit> {
        return withContext(ioDispatcher) {
            suspendRunCatching {
                dataStore.edit { preferences ->
                    preferences[NOTIFICATIONS_KEY] = enabled
                }
            }
        }
    }

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }
}
```

#### 3. Create Repository

```kotlin
// data/src/main/kotlin/.../repository/SettingsRepository.kt
interface SettingsRepository {
    fun observeSettings(): Flow<UserPreferences>
    suspend fun updateTheme(theme: ThemePreference): Result<Unit>
    suspend fun toggleNotifications(enabled: Boolean): Result<Unit>
}

internal class SettingsRepositoryImpl @Inject constructor(
    private val preferencesDataSource: UserPreferencesDataSource
) : SettingsRepository {

    override fun observeSettings(): Flow<UserPreferences> {
        return preferencesDataSource.observePreferences()
    }

    override suspend fun updateTheme(theme: ThemePreference): Result<Unit> {
        return preferencesDataSource.updateTheme(theme)
    }

    override suspend fun toggleNotifications(enabled: Boolean): Result<Unit> {
        return preferencesDataSource.toggleNotifications(enabled)
    }
}
```

#### 4. Use in ViewModel

```kotlin
// feature/settings/src/main/kotlin/.../SettingsViewModel.kt
data class SettingsScreenData(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val language: String = "en"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState(SettingsScreenData()))
    val uiState = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.observeSettings()
                .collect { preferences ->
                    _uiState.updateState {
                        copy(
                            theme = preferences.theme,
                            notificationsEnabled = preferences.notificationsEnabled,
                            language = preferences.language
                        )
                    }
                }
        }
    }

    fun updateTheme(theme: ThemePreference) {
        _uiState.updateWith {
            settingsRepository.updateTheme(theme)
        }
    }

    fun toggleNotifications() {
        _uiState.updateWith {
            settingsRepository.toggleNotifications(!uiState.value.data.notificationsEnabled)
        }
    }
}
```

### Local-Only Flow Diagram

```
App Launch
    ↓
ViewModel.observeSettings()
    ↓
Repository.observeSettings()
    ↓
DataStore emits Flow<Preferences>
    ↓
ViewModel updates UiState
    ↓
UI renders current settings

User changes theme
    ↓
ViewModel.updateTheme(DARK)
    ↓
Repository.updateTheme(DARK)
    ↓
DataStore.edit { ... }
    ↓
Flow emits new preferences
    ↓
UI updates automatically
```

---

## Offline-First Pattern (Network + Local)

### When to Use

- Core application data (posts, messages, user profiles)
- Data needed offline
- Data that syncs with a server
- Multi-device synchronization required

### Architecture

```
                   ViewModel
                      ↓
                  Repository
                   ↙     ↘
        LocalDataSource  NetworkDataSource
             (Room)        (Retrofit/Firebase)
                ↓               ↓
         Local Database      API/Firestore
```

### Key Concepts

1. **Room is the Single Source of Truth**: UI always observes local database
2. **Network Updates Background**: Fetch from network, update local database
3. **Sync Metadata**: Track sync state (lastUpdated, lastSynced, needsSync)
4. **Soft Deletes**: Mark as deleted locally, sync deletion, then remove

### Implementation

#### 1. Define Room Entity with Sync Metadata

```kotlin
// core/room/src/main/kotlin/.../model/PostEntity.kt
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val authorId: String,

    // Sync metadata
    val userId: String = "",  // For multi-user filtering
    val lastUpdated: Long = 0,  // Local modification timestamp
    val lastSynced: Long = 0,   // Last successful sync timestamp
    val needsSync: Boolean = false,  // Has pending changes
    val deleted: Boolean = false,    // Soft delete flag
    val syncAction: SyncAction = SyncAction.NONE
)

enum class SyncAction {
    NONE,    // Already synced
    UPSERT,  // Create or update on remote
    DELETE   // Delete on remote
}
```

#### 2. Create Room DAO

```kotlin
// core/room/src/main/kotlin/.../dao/PostDao.kt
@Dao
interface PostDao {
    // Observe all non-deleted posts for a user
    @Query("SELECT * FROM posts WHERE userId = :userId AND deleted = 0 ORDER BY lastUpdated DESC")
    fun observePosts(userId: String): Flow<List<PostEntity>>

    // Get single post
    @Query("SELECT * FROM posts WHERE id = :id")
    fun observePost(id: String): Flow<PostEntity>

    // Insert or update
    @Upsert
    suspend fun upsert(post: PostEntity)

    // Get unsynced posts
    @Query("SELECT * FROM posts WHERE userId = :userId AND needsSync = 1")
    suspend fun getUnsyncedPosts(userId: String): List<PostEntity>

    // Mark as synced
    @Query("UPDATE posts SET needsSync = 0, syncAction = 'NONE', lastSynced = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: String, timestamp: Long)

    // Soft delete
    @Query("UPDATE posts SET deleted = 1, needsSync = 1, syncAction = 'DELETE', lastUpdated = :timestamp WHERE id = :id")
    suspend fun markAsDeleted(id: String, timestamp: Long)

    // Get latest sync timestamp (for incremental sync)
    @Query("SELECT MAX(lastSynced) FROM posts WHERE userId = :userId")
    suspend fun getLatestSyncTimestamp(userId: String): Long?
}
```

#### 3. Create Local Data Source

```kotlin
// core/room/src/main/kotlin/.../data/LocalDataSource.kt
interface LocalDataSource {
    fun observePosts(userId: String): Flow<List<PostEntity>>
    fun observePost(id: String): Flow<PostEntity>
    suspend fun upsertPost(post: PostEntity)
    suspend fun markPostAsDeleted(id: String)
    suspend fun getUnsyncedPosts(userId: String): List<PostEntity>
    suspend fun markAsSynced(id: String)
    suspend fun getLatestSyncTimestamp(userId: String): Long
}

internal class LocalDataSourceImpl @Inject constructor(
    private val postDao: PostDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LocalDataSource {

    override fun observePosts(userId: String): Flow<List<PostEntity>> {
        return postDao.observePosts(userId)
    }

    override fun observePost(id: String): Flow<PostEntity> {
        return postDao.observePost(id)
    }

    override suspend fun upsertPost(post: PostEntity) {
        withContext(ioDispatcher) {
            postDao.upsert(post)
        }
    }

    override suspend fun markPostAsDeleted(id: String) {
        withContext(ioDispatcher) {
            postDao.markAsDeleted(id, System.currentTimeMillis())
        }
    }

    override suspend fun getUnsyncedPosts(userId: String): List<PostEntity> {
        return withContext(ioDispatcher) {
            postDao.getUnsyncedPosts(userId)
        }
    }

    override suspend fun markAsSynced(id: String) {
        withContext(ioDispatcher) {
            postDao.markAsSynced(id, System.currentTimeMillis())
        }
    }

    override suspend fun getLatestSyncTimestamp(userId: String): Long {
        return withContext(ioDispatcher) {
            postDao.getLatestSyncTimestamp(userId) ?: 0L
        }
    }
}
```

#### 4. Create Network Data Source

```kotlin
// core/network/src/main/kotlin/.../data/PostsNetworkDataSource.kt
interface PostsNetworkDataSource {
    suspend fun getPosts(userId: String, since: Long): Result<List<PostResponse>>
    suspend fun createOrUpdatePost(post: PostRequest): Result<Unit>
    suspend fun deletePost(postId: String): Result<Unit>
}

internal class PostsNetworkDataSourceImpl @Inject constructor(
    private val postsApi: PostsApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PostsNetworkDataSource {

    override suspend fun getPosts(userId: String, since: Long): Result<List<PostResponse>> {
        return withContext(ioDispatcher) {
            suspendRunCatching {
                postsApi.getPosts(userId, since)
            }
        }
    }

    override suspend fun createOrUpdatePost(post: PostRequest): Result<Unit> {
        return withContext(ioDispatcher) {
            suspendRunCatching {
                postsApi.upsertPost(post)
            }
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return withContext(ioDispatcher) {
            suspendRunCatching {
                postsApi.deletePost(postId)
            }
        }
    }
}
```

#### 5. Create Repository (Offline-First)

```kotlin
// data/src/main/kotlin/.../repository/PostsRepository.kt
interface PostsRepository {
    fun observePosts(): Flow<List<Post>>
    fun observePost(id: String): Flow<Post>
    suspend fun createOrUpdatePost(post: Post): Result<Unit>
    suspend fun deletePost(post: Post): Result<Unit>
    suspend fun syncPosts(): Result<Unit>
}

internal class PostsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val networkDataSource: PostsNetworkDataSource,
    private val preferencesDataSource: UserPreferencesDataSource,
    private val syncManager: SyncManager
) : PostsRepository {

    // Observe local database (single source of truth)
    override fun observePosts(): Flow<List<Post>> {
        return flow {
            val userId = preferencesDataSource.getUserIdOrThrow()

            // Trigger background sync
            syncManager.requestSync()

            // Emit local data immediately
            emitAll(
                localDataSource.observePosts(userId)
                    .map { entities -> entities.map { it.toDomain() } }
            )
        }
    }

    override fun observePost(id: String): Flow<Post> {
        return localDataSource.observePost(id)
            .map { it.toDomain() }
    }

    // Create or update locally, mark for sync
    override suspend fun createOrUpdatePost(post: Post): Result<Unit> {
        return suspendRunCatching {
            val userId = preferencesDataSource.getUserIdOrThrow()

            localDataSource.upsertPost(
                post.toEntity().copy(
                    userId = userId,
                    lastUpdated = System.currentTimeMillis(),
                    needsSync = true,
                    syncAction = SyncAction.UPSERT
                )
            )

            // Request background sync
            syncManager.requestSync()
        }
    }

    // Soft delete locally, mark for sync
    override suspend fun deletePost(post: Post): Result<Unit> {
        return suspendRunCatching {
            localDataSource.markPostAsDeleted(post.id)
            syncManager.requestSync()
        }
    }

    // Bidirectional sync: Push local changes, pull remote changes
    override suspend fun syncPosts(): Result<Unit> {
        return suspendRunCatching {
            val userId = preferencesDataSource.getUserIdOrThrow()

            // Step 1: Push local changes to remote
            val unsyncedPosts = localDataSource.getUnsyncedPosts(userId)
            unsyncedPosts.forEach { unsyncedPost ->
                when (unsyncedPost.syncAction) {
                    SyncAction.UPSERT -> {
                        networkDataSource.createOrUpdatePost(
                            unsyncedPost.toNetworkRequest()
                        ).getOrThrow()
                    }
                    SyncAction.DELETE -> {
                        networkDataSource.deletePost(unsyncedPost.id).getOrThrow()
                    }
                    SyncAction.NONE -> {
                        // Skip
                    }
                }
                localDataSource.markAsSynced(unsyncedPost.id)
            }

            // Step 2: Pull remote changes (incremental sync)
            val lastSyncTimestamp = localDataSource.getLatestSyncTimestamp(userId)
            val remotePosts = networkDataSource.getPosts(userId, lastSyncTimestamp)
                .getOrThrow()

            // Update local database with remote changes
            remotePosts.forEach { remotePost ->
                localDataSource.upsertPost(
                    remotePost.toEntity().copy(
                        userId = userId,
                        lastSynced = System.currentTimeMillis(),
                        needsSync = false,
                        syncAction = SyncAction.NONE
                    )
                )
            }
        }
    }
}
```

#### 6. Use in ViewModel

```kotlin
// feature/posts/src/main/kotlin/.../PostsViewModel.kt
data class PostsScreenData(
    val posts: List<Post> = emptyList()
)

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postsRepository: PostsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState(PostsScreenData()))
    val uiState = _uiState.asStateFlow()

    init {
        observePosts()
    }

    private fun observePosts() {
        viewModelScope.launch {
            postsRepository.observePosts()
                .collect { posts ->
                    _uiState.updateState {
                        copy(posts = posts)
                    }
                }
        }
    }

    fun createPost(title: String, content: String) {
        val newPost = Post(
            title = title,
            content = content,
            authorId = "current-user-id"
        )
        _uiState.updateWith {
            postsRepository.createOrUpdatePost(newPost)
        }
    }

    fun deletePost(post: Post) {
        _uiState.updateWith {
            postsRepository.deletePost(post)
        }
    }
}
```

### Offline-First Flow Diagram

```
App Launch / User navigates to screen
              ↓
  ViewModel.observePosts()
              ↓
  Repository.observePosts()
              ↓
    ┌─────────────────────┐
    │ Trigger Sync        │ (Background)
    └─────────────────────┘
              ↓
  LocalDataSource.observePosts()
              ↓
    Room Database emits Flow<List<PostEntity>>
              ↓
    Map to domain models (Flow<List<Post>>)
              ↓
    ViewModel updates UiState
              ↓
    UI displays posts immediately (offline-first!)

    Background Sync (triggered by SyncManager):
              ↓
    Repository.syncPosts()
              ↓
    ┌──────────────────────────────┐
    │ Step 1: Push Local Changes   │
    │ - Get unsynced posts          │
    │ - For each: UPSERT or DELETE  │
    │ - Mark as synced              │
    └──────────────────────────────┘
              ↓
    ┌──────────────────────────────┐
    │ Step 2: Pull Remote Changes  │
    │ - Get lastSyncTimestamp       │
    │ - Fetch posts since timestamp │
    │ - Upsert to local database    │
    └──────────────────────────────┘
              ↓
    Room emits updated data
              ↓
    UI updates automatically (reactive!)
```

---

## Real-Time Data Updates

For real-time updates (e.g., Firebase Firestore snapshots, WebSocket), use Firebase's snapshot listeners or similar mechanisms.

### Firebase Firestore Real-Time Example

```kotlin
// firebase/firestore/src/main/kotlin/.../FirebaseDataSource.kt
interface FirebaseDataSource {
    fun observePosts(userId: String): Flow<List<FirebasePost>>
    suspend fun createOrUpdatePost(post: FirebasePost): Result<Unit>
    suspend fun deletePost(postId: String): Result<Unit>
}

internal class FirebaseDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FirebaseDataSource {

    override fun observePosts(userId: String): Flow<List<FirebasePost>> = callbackFlow {
        val listener = firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebasePost::class.java)
                } ?: emptyList()

                trySend(posts)
            }

        awaitClose { listener.remove() }
    }.flowOn(ioDispatcher)

    override suspend fun createOrUpdatePost(post: FirebasePost): Result<Unit> {
        return withContext(ioDispatcher) {
            suspendRunCatching {
                firestore.collection("posts")
                    .document(post.id)
                    .set(post)
                    .await()
            }
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return withContext(ioDispatcher) {
            suspendRunCatching {
                firestore.collection("posts")
                    .document(postId)
                    .delete()
                    .await()
            }
        }
    }
}
```

### Using Real-Time Data in Repository

```kotlin
// Combine Firestore real-time updates with local database
override fun observePosts(): Flow<List<Post>> {
    return flow {
        val userId = preferencesDataSource.getUserIdOrThrow()

        // Start listening to Firestore real-time updates
        viewModelScope.launch {
            firebaseDataSource.observePosts(userId)
                .collect { firestorePosts ->
                    // Update local database with Firestore changes
                    firestorePosts.forEach { firebasePost ->
                        localDataSource.upsertPost(
                            firebasePost.toEntity().copy(
                                userId = userId,
                                lastSynced = System.currentTimeMillis(),
                                needsSync = false,
                                syncAction = SyncAction.NONE
                            )
                        )
                    }
                }
        }

        // Emit from local database (single source of truth)
        emitAll(
            localDataSource.observePosts(userId)
                .map { entities -> entities.map { it.toDomain() } }
        )
    }
}
```

---

## Caching Strategies

### 1. Time-Based Cache Invalidation

Fetch fresh data from network if cache is older than a threshold:

```kotlin
class PostsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val networkDataSource: PostsNetworkDataSource
) : PostsRepository {

    companion object {
        private const val CACHE_EXPIRATION_MS = 5 * 60 * 1000 // 5 minutes
    }

    override fun observePosts(): Flow<List<Post>> {
        return flow {
            val userId = preferencesDataSource.getUserIdOrThrow()

            // Check if cache is stale
            val lastSyncTimestamp = localDataSource.getLatestSyncTimestamp(userId)
            val isCacheStale = System.currentTimeMillis() - lastSyncTimestamp > CACHE_EXPIRATION_MS

            if (isCacheStale) {
                // Fetch fresh data in background
                viewModelScope.launch {
                    syncPosts()
                }
            }

            // Emit local data immediately
            emitAll(
                localDataSource.observePosts(userId)
                    .map { entities -> entities.map { it.toDomain() } }
            )
        }
    }
}
```

### 2. Manual Refresh (Pull-to-Refresh)

Allow user to manually trigger sync:

```kotlin
// In ViewModel
fun refreshPosts() {
    _uiState.updateWith {
        postsRepository.syncPosts()
    }
}

// In UI
@Composable
fun PostsScreen(
    posts: List<Post>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    Box(Modifier.pullRefresh(pullRefreshState)) {
        LazyColumn {
            items(posts) { post ->
                PostCard(post = post)
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
```

### 3. Network-Bound Resource Pattern

Utility for coordinating network + local data with automatic caching:

```kotlin
// core/android/src/main/kotlin/.../utils/Resource.kt

/**
 * Fetch data from network, save to local database, and observe local database.
 *
 * @param query Function to query local database
 * @param fetch Function to fetch from network
 * @param saveFetchResult Function to save network result to local database
 * @param shouldFetch Predicate to determine if network fetch is needed
 */
inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType?) -> Boolean = { true }
): Flow<Resource<ResultType>> = flow {
    // Emit loading state
    emit(Resource.Loading())

    // Query local database first
    val localData = query().first()

    // Check if we should fetch from network
    if (shouldFetch(localData)) {
        // Emit local data as loading state
        emit(Resource.Loading(localData))

        try {
            // Fetch from network
            val networkData = fetch()

            // Save to local database
            saveFetchResult(networkData)

            // Emit success with fresh data from local database
            query().collect { freshData ->
                emit(Resource.Success(freshData))
            }
        } catch (throwable: Throwable) {
            // Emit error with stale local data
            query().collect { staleData ->
                emit(Resource.Error(throwable, staleData))
            }
        }
    } else {
        // Use cached data
        query().collect { data ->
            emit(Resource.Success(data))
        }
    }
}

// Usage in Repository
override fun observePosts(): Flow<Resource<List<Post>>> {
    return networkBoundResource(
        query = {
            localDataSource.observePosts(userId)
                .map { entities -> entities.map { it.toDomain() } }
        },
        fetch = {
            networkDataSource.getPosts(userId, 0).getOrThrow()
        },
        saveFetchResult = { networkPosts ->
            networkPosts.forEach { networkPost ->
                localDataSource.upsertPost(networkPost.toEntity())
            }
        },
        shouldFetch = { cachedPosts ->
            // Fetch if cache is empty or stale
            cachedPosts.isNullOrEmpty() || isCacheStale()
        }
    )
}
```

---

## Error Handling

All data layer operations use a **layered error handling approach** with `Result<T>` for error propagation:

- **Repository Layer**: Uses `suspendRunCatching` to wrap all operations
- **ViewModel Layer**: Uses `updateStateWith`/`updateWith` for automatic error capture
- **UI Layer**: Uses `StatefulComposable` for automatic error display via snackbar

For comprehensive error handling patterns including network-specific errors, HTTP error codes, and error flow diagrams, see:

> [!NOTE]
> Complete error handling documentation is available in the [Data Module README](../data/README.md#error-handling).

---

## Summary

This guide covered three main data flow patterns:

- **Network-Only**: For real-time data that doesn't need offline access (weather, stock prices)
- **Local-Only**: For preferences and settings using DataStore (theme, notifications)
- **Offline-First**: For user-generated content with Room as single source of truth (posts, profiles)

**Key Takeaways:**

1. **Choose the Right Pattern** based on feature requirements
2. **Room is the Single Source of Truth** for offline-first - UI observes local database, network updates happen in background
3. **Use Proper Threading** - Inject `@IoDispatcher` and use `withContext(ioDispatcher)` for blocking calls
4. **Error Handling is Centralized** - Repository uses `suspendRunCatching`, ViewModel uses `updateStateWith`/`updateWith`, UI uses `StatefulComposable`

All patterns use **Repositories** as the interface to ViewModels, **Data Sources** for external system interaction, **Result** type for error handling, and **Flow** for reactive data streams.

## Further Reading

- [Data Module README](../data/README.md) - Repository patterns and error handling reference
- [State Management](state-management.md) - Learn about ViewModel state patterns
- [Architecture Overview](architecture.md) - Understand the two-layer architecture
- [Adding Features](guide.md) - Step-by-step implementation guide
- [Quick Reference](quick-reference.md) - Common data flow patterns cheat sheet
