# Module :data

**Purpose:** Provides repository implementations that coordinate between local and remote data
sources, implementing the single source of truth pattern.

## Overview

The `data` module is the central data management layer that implements the **Repository Pattern**.
It coordinates between local (Room, DataStore) and remote (Network, Firebase) data sources to
provide a unified, reactive API for the UI layer.

## Key Concepts

### 1. Repository Pattern

- **Single source of truth**: Local database is always the source of truth
- **Offline-first**: UI always reads from local database
- **Background sync**: Network data updates local database
- **Reactive**: Exposes data as Flow for automatic UI updates

### 2. Two-Layer Architecture

This template intentionally uses a **two-layer architecture** (UI + Data):

- **NO domain layer** by design
- ViewModels call repositories directly
- Reduces complexity and boilerplate
- Sufficient for most applications

### 3. Data Flow

```
Network/Firebase → Repository → Local Database → Flow → ViewModel → UI
                       ↓
                  Sync Logic
```

## When to Use This Module

**Use `data` module for:**

- Implementing repository interfaces
- Coordinating local and remote data sources
- Offline-first data management
- Data transformation (DTO ↔ Entity ↔ Domain Model)
- Caching strategies

**Don't use `data` module for:**

- UI logic (use feature modules)
- Direct database access (use repositories)
- Business logic without data access (consider if you need a domain layer)

## Common Patterns

### Repository Interface

```kotlin
interface UserRepository {
    // Observe data (reactive)
    fun observeUsers(): Flow<List<User>>
    fun observeUserById(id: String): Flow<User?>

    // One-shot operations
    suspend fun syncUsers(): Result<Unit>
    suspend fun createUser(user: User): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(id: String): Result<Unit>
}
```

### Repository Implementation (Offline-First)

```kotlin
class UserRepositoryImpl @Inject constructor(
    private val localDataSource: UserLocalDataSource,
    private val networkDataSource: UserNetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    // UI observes local database (single source of truth)
    override fun observeUsers(): Flow<List<User>> =
        localDataSource.observeUsers()
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeUserById(id: String): Flow<User?> =
        localDataSource.observeUserById(id)
            .map { it?.toDomain() }

    // Sync from network to local database
    override suspend fun syncUsers(): Result<Unit> = suspendRunCatching {
        val networkUsers = networkDataSource.getUsers()
        localDataSource.saveUsers(
            networkUsers.map { dto ->
                dto.toEntity().copy(lastSynced = System.currentTimeMillis())
            }
        )
    }

    // Create local, then sync
    override suspend fun createUser(user: User): Result<Unit> = suspendRunCatching {
        val entity = user.toEntity().copy(
            syncAction = SyncAction.CREATE,
            lastUpdated = System.currentTimeMillis()
        )
        localDataSource.saveUser(entity)
        // SyncWorker will push to server
    }

    override suspend fun updateUser(user: User): Result<Unit> = suspendRunCatching {
        val entity = user.toEntity().copy(
            syncAction = SyncAction.UPDATE,
            lastUpdated = System.currentTimeMillis()
        )
        localDataSource.saveUser(entity)
    }

    override suspend fun deleteUser(id: String): Result<Unit> = suspendRunCatching {
        localDataSource.markAsDeleted(id)
    }
}
```

### Using `networkBoundResource` Helper

```kotlin
class UserRepositoryImpl @Inject constructor(
    private val localDataSource: UserLocalDataSource,
    private val networkDataSource: UserNetworkDataSource
) : UserRepository {

    override fun observeUsers(): Flow<Resource<List<User>>> =
        networkBoundResource(
            query = {
                localDataSource.observeUsers()
                    .map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                networkDataSource.getUsers()
            },
            saveFetchResult = { dtos ->
                localDataSource.saveUsers(dtos.map { it.toEntity() })
            },
            shouldFetch = { users ->
                // Fetch if data is stale
                users.isEmpty() || isDataStale()
            }
        )
}
```

### Data Transformation (Mappers)

```kotlin
// DTO (from network) → Entity (Room)
fun UserDto.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    lastUpdated = System.currentTimeMillis(),
    syncAction = SyncAction.NONE
)

// Entity → Domain Model
fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email
)

// Domain Model → Entity
fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email
)

// Domain Model → DTO
fun User.toDto(): UserDto = UserDto(
    id = id,
    name = name,
    email = email
)
```

## Dependencies Graph

```mermaid
graph TD
    A[data] --> B[core:android]
    A --> C[core:network]
    A --> D[core:preferences]
    A --> E[core:room]
    A --> F[firebase:auth]
    A --> G[firebase:firestore]

    subgraph "Core"
        B
        C
        D
        E
    end

    subgraph "Firebase"
        F
        G
    end

    style A fill: #4CAF50, stroke: #333, stroke-width: 2px
    style B fill: #64B5F6, stroke: #333, stroke-width: 2px
    style C fill: #64B5F6, stroke: #333, stroke-width: 2px
    style D fill: #64B5F6, stroke: #333, stroke-width: 2px
    style E fill: #64B5F6, stroke: #333, stroke-width: 2px
    style F fill: #FFA726, stroke: #333, stroke-width: 2px
    style G fill: #FFA726, stroke: #333, stroke-width: 2px
```

## Hilt Module Setup

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}
```

## API Documentation

For detailed API documentation, see the [Dokka-generated API reference](../docs/api/).

## Related Documentation

- [Quick Reference Guide](../docs/quick-reference.md) - Repository patterns
- [Architecture Overview](../docs/architecture.md) - Two-layer architecture explained
- [Core Room Module](../core/room/README.md) - Local data source patterns
- [Core Network Module](../core/network/README.md) - Remote data source patterns

## Repository Patterns

### 1. Network-Only Repository

```kotlin
class RemoteOnlyRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource
) : RemoteOnlyRepository {
    override suspend fun getData(): Result<Data> = suspendRunCatching {
        networkDataSource.getData()
    }
}
```

### 2. Local-Only Repository

```kotlin
class LocalOnlyRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : LocalOnlyRepository {
    override fun observeData(): Flow<List<Data>> =
        localDataSource.observeData()
            .map { entities -> entities.map { it.toDomain() } }
}
```

### 3. Offline-First with Manual Sync

```kotlin
class OfflineFirstRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val networkDataSource: NetworkDataSource
) : OfflineFirstRepository {
    // Always observe local
    override fun observeData(): Flow<List<Data>> =
        localDataSource.observeData()
            .map { entities -> entities.map { it.toDomain() } }

    // Manual refresh
    override suspend fun refresh(): Result<Unit> = suspendRunCatching {
        val networkData = networkDataSource.getData()
        localDataSource.saveData(networkData.map { it.toEntity() })
    }
}
```

### 4. Offline-First with Automatic Sync (using `networkBoundResource`)

See example above using `networkBoundResource` helper.

## Best Practices

1. **Always use `suspendRunCatching`** for error handling in repositories
2. **Return Flow for observable data**, Result<T> for one-shot operations
3. **Keep repositories focused** on data coordination (no business logic)
4. **Use injected dispatchers** from core:android
5. **Implement mapper functions** for clean data transformation
6. **Prefer local database as source of truth** for offline-first
7. **Update sync metadata** when modifying local data
8. **Return domain models** from repositories (hide DTOs and Entities)

## Philosophy

### Why No Domain Layer?

This template follows a **pragmatic simplicity** approach:

**Two-layer architecture (UI + Data):**

- ViewModels call repositories directly
- Repositories return domain models (simple data classes)
- Reduces boilerplate and indirection
- Easier to understand and maintain

**When to add a domain layer:**

- Complex business logic that doesn't fit in repositories
- Multiple UI representations of the same data
- Shared business rules across features
- Heavy data transformation logic

For most applications, **two layers are sufficient**.

## Usage

This module is used by all feature modules that need data access:

```kotlin
dependencies {
    implementation(project(":data"))
}
```

## Testing Repositories

```kotlin
class UserRepositoryImplTest {
    private lateinit var localDataSource: FakeUserLocalDataSource
    private lateinit var networkDataSource: FakeUserNetworkDataSource
    private lateinit var repository: UserRepositoryImpl

    @Test
    fun `observeUsers returns local data`() = runTest {
        // Given
        val entity = UserEntity(id = "1", name = "Test")
        localDataSource.emit(listOf(entity))

        // When
        val users = repository.observeUsers().first()

        // Then
        assertEquals(1, users.size)
        assertEquals("Test", users[0].name)
    }
}
```