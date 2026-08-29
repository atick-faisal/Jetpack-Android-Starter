# Adding a Feature

This walks through adding a feature end to end, using the `home` feature (`Jetpack` library
cards — a list screen plus a create/edit screen) as the running example, because it's the only
feature in the repo that touches every layer: a Room-backed local store, an offline-first
repository with two-way Firebase sync, two ViewModels, and two Navigation 3 destinations. Building
a new feature means repeating this shape with your own model and screen names.

## Overview

1. [Data models](#1-data-models) — domain model + persistence model
2. [Data source](#2-data-source) — Room-backed local access
3. [Repository](#3-repository) — the offline-first contract the UI depends on
4. [UI layer](#4-ui-layer) — ViewModel + screen
5. [Navigation](#5-navigation) — `NavKey`s and entries
6. [Dependency injection](#6-dependency-injection) — wiring it all together

## 1. Data models

Two models, not one: a Room `@Entity` for storage, and a plain domain model the rest of the app
depends on. Extension functions map between them (and, for `home`, a third — `FirebaseJetpack` —
for the Firestore side of the sync).

```kotlin
// core/room/src/main/kotlin/dev/atick/core/room/model/JetpackEntity.kt
@Entity(tableName = "jetpacks")
data class JetpackEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double,
    val userId: String = String(),
    val lastUpdated: Long = 0,
    val lastSynced: Long = 0,
    val needsSync: Boolean = false,
    val deleted: Boolean = false,
    val syncAction: SyncAction = SyncAction.NONE,
)
```

```kotlin
// data/src/main/kotlin/dev/atick/data/model/home/Jetpack.kt
data class Jetpack(
    val id: String = UUID.randomUUID().toString(),
    val name: String = String(),
    val price: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val lastSynced: Long = 0L,
    val needsSync: Boolean = true,
    val formattedDate: String = lastUpdated.asFormattedDateTime(),
)

fun JetpackEntity.toJetpack(): Jetpack = Jetpack(id = id, name = name, price = price, /* ... */)
fun Jetpack.toJetpackEntity(): JetpackEntity = JetpackEntity(id = id, name = name, price = price, /* ... */)
```

> [!NOTE]
> `lastUpdated`, `lastSynced`, `needsSync`, `syncAction` are sync bookkeeping, not part of the
> feature's actual data. A feature with no remote counterpart doesn't need them.

## 2. Data source

`LocalDataSource` wraps the Room DAO and moves every call onto `IoDispatcher`:

```kotlin
// core/room/src/main/kotlin/dev/atick/core/room/data/LocalDataSourceImpl.kt
internal class LocalDataSourceImpl @Inject constructor(
    private val jetpackDao: JetpackDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LocalDataSource {
    override fun getJetpacks(userId: String): Flow<List<JetpackEntity>> =
        jetpackDao.getJetpacks(userId).flowOn(ioDispatcher)

    override suspend fun upsertJetpack(jetpackEntity: JetpackEntity) = withContext(ioDispatcher) {
        jetpackDao.upsertJetpack(jetpackEntity)
    }
    // ...
}
```

> [!NOTE]
> `home`'s remote counterpart is Firestore, reached through `FirebaseDataSource` — see
> [`firebase/firestore`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/firebase/firestore/README.md). `core:network` also ships a
> `NetworkDataSource`/`JetpackRestApi` pair, but `HomeRepositoryImpl` doesn't use it; it's there
> for a feature that talks to a plain REST backend instead of Firestore. Use whichever remote your
> feature actually needs — not both.

## 3. Repository

The repository is the only thing a ViewModel depends on. It returns `Flow` for observable reads
and `Result<Unit>` for writes, via `suspendRunCatching`:

```kotlin
// data/src/main/kotlin/dev/atick/data/repository/home/HomeRepository.kt
interface HomeRepository : Syncable {
    fun getJetpacks(): Flow<List<Jetpack>>
    fun getJetpack(id: String): Flow<Jetpack>
    suspend fun createOrUpdateJetpack(jetpack: Jetpack): Result<Unit>
    suspend fun markJetpackAsDeleted(jetpack: Jetpack): Result<Unit>
}
```

```kotlin
// data/src/main/kotlin/dev/atick/data/repository/home/HomeRepositoryImpl.kt
override fun getJetpacks(): Flow<List<Jetpack>> {
    syncManager.requestSync()
    return flow {
        val userId = preferencesDataSource.getUserIdOrThrow()
        emitAll(localDataSource.getJetpacks(userId).map { it.mapToJetpacks() })
    }
}

override suspend fun createOrUpdateJetpack(jetpack: Jetpack): Result<Unit> = suspendRunCatching {
    val userId = preferencesDataSource.getUserIdOrThrow()
    localDataSource.upsertJetpack(
        jetpack.toJetpackEntity().copy(
            userId = userId,
            lastUpdated = System.currentTimeMillis(),
            needsSync = true,
            syncAction = SyncAction.UPSERT,
        ),
    )
    syncManager.requestSync()
}
```

Local reads/writes are all this needs. `HomeRepositoryImpl` additionally implements `sync()`,
which pushes unsynced entities to Firestore and pulls remote changes back — the full two-way
algorithm is covered in [Data Layer](data.md#two-way-sync). A feature with no remote doesn't
implement `Syncable` at all.

## 4. UI layer

One ViewModel per screen, each exposing a single `StateFlow<UiState<ScreenData>>`. The list
ViewModel just observes and forwards:

```kotlin
// feature/home/src/main/kotlin/dev/atick/feature/home/ui/home/HomeViewModel.kt
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
) : ViewModel() {
    private val _homeUiState = MutableStateFlow(UiState(HomeScreenData()))
    val homeUiState = _homeUiState
        .onStart { getJetpacks() }
        .stateInDelayed(UiState(HomeScreenData()), viewModelScope)

    fun deleteJetpack(jetpack: Jetpack) {
        _homeUiState.updateWith { homeRepository.markJetpackAsDeleted(jetpack) }
    }
}
```

The detail ViewModel handles both create and edit, taking the optional existing ID by assisted
injection from its `NavKey` rather than a `SavedStateHandle`:

```kotlin
// feature/home/src/main/kotlin/dev/atick/feature/home/ui/item/ItemViewModel.kt
@HiltViewModel(assistedFactory = ItemViewModel.Factory::class)
class ItemViewModel @AssistedInject constructor(
    private val homeRepository: HomeRepository,
    @Assisted private val existingJetpackId: String?,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(existingJetpackId: String?): ItemViewModel
    }

    fun updateName(name: String) {
        _itemUiState.updateState { copy(jetpackName = name) }
    }

    fun createOrUpdateJetpack() {
        _itemUiState.updateStateWith {
            val jetpack = Jetpack(id = jetpackId, name = jetpackName.trim(), price = jetpackPrice)
            homeRepository.createOrUpdateJetpack(jetpack)
            Result.success(copy(navigateBack = OneTimeEvent(true)))
        }
    }
}
```

The screen collects that state and hands it to `StatefulComposable`, which renders loading/error
around your content:

```kotlin
// feature/home/src/main/kotlin/dev/atick/feature/home/ui/home/HomeScreen.kt
@Composable
internal fun HomeScreen(
    onJetpackClick: (String) -> Unit,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val homeState by homeViewModel.homeUiState.collectAsStateWithLifecycle()

    StatefulComposable(state = homeState, onShowSnackbar = onShowSnackbar) { homeScreenData ->
        HomeScreen(
            jetpacks = homeScreenData.jetpacks,
            onJetpackCLick = onJetpackClick,
            onDeleteJetpack = homeViewModel::deleteJetpack,
        )
    }
}
```

`UiState`, `updateState`, `updateStateWith`, `updateWith`, and `StatefulComposable` are covered in
full in [State Management](state-management.md) — this is what wiring them into a real screen
looks like, not a restatement of how they work.

## 5. Navigation

Each screen gets a `@Serializable NavKey`, a `Navigator` extension to reach it, and an entry
registration:

```kotlin
// feature/home/src/main/kotlin/dev/atick/feature/home/navigation/HomeNavigation.kt
@Serializable
data object HomeNavKey : NavKey

@Serializable
data class ItemNavKey(val itemId: String?) : NavKey

fun Navigator.navigateToItem(itemId: String?) = navigate(ItemNavKey(itemId))

fun EntryProviderScope<NavKey>.homeEntries(
    navigator: Navigator,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
) {
    entry<HomeNavKey>(metadata = ListDetailSceneStrategy.listPane()) {
        HomeScreen(onJetpackClick = navigator::navigateToItem, onShowSnackbar = onShowSnackbar)
    }
    entry<ItemNavKey>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
        ItemScreen(itemId = key.itemId, onBackClick = navigator::goBack, onShowSnackbar = onShowSnackbar)
    }
}
```

`homeEntries(navigator, onShowSnackbar)` is then called inside the app-level `entryProvider { }` in
`app/src/main/kotlin/dev/atick/compose/ui/JetpackApp.kt`. How `NavKey`s carry arguments into a
ViewModel by assisted injection, and how the scene strategies decide list/detail layout, are
covered in [Navigation](navigation.md).

## 6. Dependency injection

Bind the repository interface to its implementation:

```kotlin
// data/src/main/kotlin/dev/atick/data/di/RepositoryModule.kt
@Binds
@Singleton
internal abstract fun bindHomeRepository(
    homeRepositoryImpl: HomeRepositoryImpl,
): HomeRepository
```

Data sources are bound the same way, one `@Binds` per interface, in `core/room/.../di/DataSourceModule.kt`
and `core/network/.../di/DataSourceModule.kt`. ViewModels need no manual wiring — `@HiltViewModel`
(or `@HiltViewModel(assistedFactory = ...)` for `ItemViewModel`) is enough.

## Testing

- `core/room/src/test/kotlin/dev/atick/core/room/data/JetpackDaoTest.kt` — a Robolectric test
  against a real in-memory database, pinning down the sync-metadata semantics (`deleted`,
  `needsSync`) for this exact entity.
- `core/testing/src/main/kotlin/dev/atick/core/testing/rule/MainDispatcherRule.kt` — swaps
  `Dispatchers.Main` for a test dispatcher; required by any ViewModel test that touches
  `updateStateWith`/`updateWith`.
- `core/testing/src/main/kotlin/dev/atick/core/testing/data/` — fake data sources
  (`FakeAuthDataSource`, `FakeUserPreferencesDataSource`) to inject in place of the real ones.

## Further reading

- [Architecture](architecture.md) — where each of these layers sits in the module graph
- [State Management](state-management.md) — how `UiState` and its update functions work
- [Navigation](navigation.md) — the full Navigation 3 model, including assisted-injected ViewModels
- [Components](components.md) — the `core:ui` building blocks used in `HomeScreen`/`ItemScreen`
- [Data Layer](data.md) — the full sync algorithm and the `@Binds`/`@IoDispatcher` wiring behind it
- [Firebase Setup](firebase.md) — configuring the Firestore backend this feature syncs with
