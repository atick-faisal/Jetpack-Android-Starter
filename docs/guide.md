# Adding a Feature

This walks through adding a feature end to end, using the `home` feature (`Jetpack` library
cards — a list screen plus a create/edit screen) as the running example, because it's the only
feature in the repo that touches every layer: a Room-backed local store, an offline-first
repository with two-way Firebase sync, two ViewModels, and two Navigation 3 destinations.

Use the excerpts to understand the existing wiring, then follow the **For your feature** steps to
add your own model and screens. Existing Home code is already registered: do not paste a second copy
of its bindings, entities, or entries into this checkout. Code marked “excerpt” omits imports or
members and is not a complete file; the worked test below is a complete file.

Start with [Getting Started](getting-started.md) and a working debug build. This walkthrough uses
existing dependencies and plugins. Adding a new module or dependency is a separate approval decision
in this repository; documenting its setup does not authorize changing the production module graph.

## Overview

- [Module setup](#module-setup) — existing screen or approved new module
- [Data models](#1-data-models) — domain model + persistence model
- [Data source](#2-data-source) — Room-backed local access
- [Repository](#3-repository) — the offline-first contract the UI depends on
- [UI layer](#4-ui-layer) — ViewModel + screen
- [Navigation](#5-navigation) — `NavKey`s and entries
- [Dependency injection](#6-dependency-injection) — wiring it all together

## Module setup

If the screen belongs to an existing feature, add its screen/ViewModel under
`feature/<feature>/src/main/kotlin/dev/atick/feature/<feature>/ui/<screen>/` and register its key in
that feature's `navigation/` file. No new Gradle module or app dependency is needed.

For an approved new Compose feature module, create `feature/<feature>/build.gradle.kts`. The following
is the complete build script for **existing** `:feature:home`; substitute your module's namespace:

```kotlin
plugins {
    alias(libs.plugins.jetpack.ui.library)
    alias(libs.plugins.jetpack.dagger.hilt)
    alias(libs.plugins.jetpack.dokka)
}

android {
    namespace = "dev.atick.feature.home"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.data)
    testImplementation(projects.core.testing)
}
```

1. Add `include(":feature:<feature>")` to root `settings.gradle.kts` beside the other features.
2. Add `implementation(projects.feature.<feature>)` to `app/build.gradle.kts`. Gradle generates
   this accessor from the included module path; use a simple lower-case module name.
3. Create `src/main/AndroidManifest.xml` containing `<manifest />`. Place Kotlin files under
   `src/main/kotlin/`, resources under `src/main/res/`, and unit tests under `src/test/kotlin/`.
4. Prefix every library resource with the module path: `:feature:catalog` uses `feature_catalog_`.
   Update `R` imports and resource references when adapting another feature's screens. App resources
   use the app's own `R`, not the feature's generated `R`.

`core:ui` exports Compose, Navigation 3, `Navigator`, and Hilt's Compose ViewModel API. The existing
plugins configure serialization and Hilt/KSP; do not add versions or duplicate compiler plugins.
Features access persistence through `:data`, without a direct dependency on Room, Firebase, or
another feature. See [Build & Tooling](build-and-tooling.md#apply-or-extend-the-build-configuration)
for plugin responsibilities.

Source locations below abbreviate the package root: `data/model/` means
`data/src/main/kotlin/dev/atick/data/model/`, and `core/room/data/` means
`core/room/src/main/kotlin/dev/atick/core/room/data/`. Feature Kotlin files similarly live below
`feature/<feature>/src/main/kotlin/dev/atick/feature/<feature>/`.

## 1. Data models

Two models, not one: a Room `@Entity` for storage, and a plain domain model the rest of the app
depends on. Extension functions map between them (and, for `home`, a third — `FirebaseJetpack` —
for the Firestore side of the sync).

```kotlin
// Existing entity excerpt; imports and comments omitted.
@Entity(tableName = "jetpacks")
data class JetpackEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double,
    val userId: String = String(),
    val lastUpdated: Long = 0,
    val lastSynced: Long = 0,
    val serverUpdatedAtNanos: Long = 0,
    val needsSync: Boolean = false,
    val deleted: Boolean = false,
    val syncAction: SyncAction = SyncAction.NONE,
)
```

```kotlin
// Existing model/mapping excerpt; imports and remaining mapping arguments omitted.
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
> `lastUpdated` orders Home's list and participates in pending-row selection; `lastSynced`,
> `serverUpdatedAtNanos`, `needsSync`, `deleted`, and `syncAction` support its sync/delete behavior.
> A local-only feature can omit remote bookkeeping and implement its own deletion policy.

**For your feature:** put the public model in `data/model/<feature>/` and the Room entity in
`core/room/model/`. Add explicit mapping functions in the data model package; carry every field your
repository needs. For a synced feature, also define its remote DTO in `firebase/firestore/model/`
and mappings in `:data`. Home's complete mappings are in `data/model/home/Jetpack.kt`; do not use
an abbreviated constructor above as a complete sync mapping. The
[two-clock and mapping reference](data.md#mapping-stored-and-remote-models) explains the timestamps.

## 2. Data source

`LocalDataSource` wraps the Room DAO and moves every call onto `IoDispatcher`:

```kotlin
// Existing implementation excerpt; imports and other members omitted.
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
> [`firebase/firestore`](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fix/docs-v3/firebase/firestore/README.md). `core:network` also ships a
> `NetworkDataSource`/`JetpackRestApi` pair, but `HomeRepositoryImpl` doesn't use it; it's there
> for a feature that talks to a plain REST backend instead of Firestore. Use whichever remote your
> feature actually needs — not both.

### Register persistence before injecting it

**For your feature**, extend `:core:room` in this order:

1. Add a `@Dao` in `core/room/data/` with the observed read queries and suspend writes your feature
   needs. Home's `JetpackDao.getJetpacks(userId)` filters owner and soft-deleted rows; its
   `getJetpack(id)` filters only ID. Choose and test ownership/missing-row behavior explicitly rather
   than assuming every Home query is owner-scoped.
2. Add the entity to `JetpackDatabase`'s `@Database(entities = [...])` and an abstract DAO accessor.
   This database lives in `core/room/data/JetpackDatabase.kt`.
3. Add a provider to `core/room/di/DaoModule.kt`. The existing provider is shown below; for a new
   DAO, change both the return type and database accessor.
4. Add your read/write methods to `LocalDataSource` and implement them in `LocalDataSourceImpl`.
   Inject the new DAO into that implementation. Use `flowOn(ioDispatcher)` for upstream Flow work
   and `withContext(ioDispatcher)` for suspend writes, following the existing methods.
5. Keep the existing `LocalDataSource` binding; adding methods does not require another binding.
   If introducing a separate data-source interface, add its constructor-injected implementation and
   a binding in `DataSourceModule`, following [Dependency injection](#6-dependency-injection).

```kotlin
// Existing DaoModule excerpt; imports and other providers omitted.
@Module(includes = [DatabaseModule::class])
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Singleton
    @Provides
    fun provideJetpackDao(jetpackDatabase: JetpackDatabase): JetpackDao =
        jetpackDatabase.getJetpackDao()
}
```

Changing the entity list changes the database schema. For existing installations, increment the
version and register/test a data-preserving migration before release. The current database is
version 2 with destructive fallback and no migration path; copying that fallback can erase pending
local changes. Follow [Room persistence and schema changes](data.md#room-persistence-and-schema-changes)
for schema export and migration testing. An in-memory test of the new schema does not test an upgrade.

## 3. Repository

Home ViewModels depend on a repository interface for data access. It returns `Flow` for observable reads
and `Result<Unit>` for writes, via `suspendRunCatching`:

```kotlin
// Existing interface excerpt; imports omitted.
interface HomeRepository : Syncable {
    fun getJetpacks(): Flow<List<Jetpack>>
    fun getJetpack(id: String): Flow<Jetpack>
    suspend fun createOrUpdateJetpack(jetpack: Jetpack): Result<Unit>
    suspend fun markJetpackAsDeleted(jetpack: Jetpack): Result<Unit>
}
```

```kotlin
// Existing method excerpts; class, constructor, imports and other members omitted.
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

The UI observes Room; a successful write Result covers local persistence and the sync request,
not confirmed remote delivery. `HomeRepositoryImpl` additionally implements `sync()`,
which pushes unsynced entities to Firestore and pulls remote changes back — the full two-way
algorithm is covered in [Data Layer](data.md#two-way-sync). A feature with no remote doesn't
implement `Syncable` at all.

**For your feature:** add its interface and `internal` implementation under
`data/repository/<feature>/`. Inject data sources with `@Inject constructor`, map entities to public
models, and use `suspendRunCatching` for one-time operations. Bind the interface in
`data/di/RepositoryModule.kt`. Keep I/O dispatch in data sources. For a local-only repository, omit
`Syncable`, sync requests, and remote dependencies. For a REST-backed feature, follow the
[remote-source options](data.md#data-sources); the existing Home repository does not use REST.

### Connect a synced feature to the worker

`Syncable` is a contract, not automatic discovery. `sync/worker/SyncWorker.kt` currently injects only
`HomeRepository` and collects `homeRepository.sync()`. To add another synced repository after agreeing
its sync behavior, inject it there and explicitly collect its sync Flow inside the worker's existing
I/O/error-handling path. Define ordering and progress reporting across repositories; returning a Flow
without collecting it performs no synchronization. Reuse the current `SyncManager` request path and
worker delegation rather than creating a second scheduler incidentally.

Read [Two-way sync](data.md#two-way-sync) before adapting the algorithm. Home takes one pending
snapshot, has no guaranteed drain after writes during a pass, and can overwrite concurrent edits.
It is not a conflict-resolution or reliable cross-device deletion protocol. New collections also
need their own remote model, query, and reviewed security-rule setup.

## 4. UI layer

Home uses one ViewModel per screen, each exposing a `StateFlow<UiState<ScreenData>>`.
Use an immutable screen-data class co-located with its ViewModel; Home's `HomeScreenData` has
`jetpacks: List<Jetpack> = emptyList()`. The following existing class excerpt omits imports and the
screen-data declaration. Its observation method maps the repository Flow, stores updates and errors
in `_homeUiState`, then launches collection in `viewModelScope`:

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

    private fun getJetpacks() {
        homeRepository.getJetpacks()
            .map(::HomeScreenData)
            .onEach { data -> _homeUiState.update { UiState(data) } }
            .catch { e -> _homeUiState.update { it.copy(error = e.asOneTimeEvent()) } }
            .launchIn(viewModelScope)
    }

    fun deleteJetpack(jetpack: Jetpack) {
        _homeUiState.updateWith { homeRepository.markJetpackAsDeleted(jetpack) }
    }
}
```

**Observation limitation:** Home and Item start ViewModel-owned repository jobs inside `onStart`.
Resubscribing after the sharing timeout starts another job while the old one remains alive. Treat
these excerpts as current wiring, not a subscription-lifetime solution; see
[Subscription lifetimes](state-management.md#subscription-lifetimes) and the bounded Observation
lifetime follow-up before adapting them.

The detail ViewModel handles both create and edit, taking the optional existing ID by assisted
injection from its `NavKey`. This existing class excerpt omits imports, state declarations, and
load/save methods; the complete factory-to-Screen chain is in [Navigation](navigation.md#passing-arguments-to-viewmodels):

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

    // State declarations, updatePrice, loading and saving are omitted here.
}
```

Keep `jetpackPrice` as raw text in `ItemScreenData`; `updatePrice` stores it without parsing.
Current save trims the name and parses the price once, with invalid/empty input becoming `0.0`;
this is not range validation. Define validation for the adopter's product separately.

**Save/load limitation:** current Item ignores the repository save Result and requests navigation
even on failure. Its load error handler constructs a state without storing it. Do not copy those
methods as successful persistence handling. The
[proposed save replacement](state-management.md#updatestatewith) maps the returned Result before
requesting navigation; it remains a proposed change and does not fix load errors, concurrent edits,
or event delivery. These runtime fixes remain separate from this walkthrough.

**For your feature:** co-locate `<Name>Screen.kt` and `<Name>ViewModel.kt` under `ui/<screen>/`.
Expose immutable screen data through `UiState`, update inputs with `updateState`, and propagate
returned Results through `updateWith`/`updateStateWith`. Provide explicit screen callbacks for
item clicks, save, delete, and back; keep navigation in the composable/entry callback.

The screen collects that state and hands it to `StatefulComposable`, which renders loading/error
around your content:

```kotlin
// Existing Screen excerpt; imports and the private content overload omitted.
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

For the complete import list, use the existing screen in the source tree: collection is
`androidx.lifecycle.compose.collectAsStateWithLifecycle`, Hilt creation is
`androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel`, and delegated Compose state uses
`androidx.compose.runtime.getValue`. `StatefulComposable` and `SnackbarAction` are in
`dev.atick.core.ui.utils`. Keep the injected destination screen `internal`; its public entry function
is the boundary the app calls. A private content overload can support previews without introducing
a separate Route destination.

Item's screen supplies `itemId` through the assisted factory described in
[Passing arguments to ViewModels](navigation.md#passing-arguments-to-viewmodels). Inside its
`StatefulComposable` content, it wires raw name/price state to `updateName`/`updatePrice`, save to
`createOrUpdateJetpack`, and back to `onBackClick`. It consumes `navigateBack` in
`LaunchedEffect(screenData.navigateBack, onBackClick)` and calls back only when consumed content is
true. Consumption is not a guarantee of completed navigation; the existing snackbar event path also
has [delivery limitations](state-management.md#onetimeeventt).

`UiState`, the update helpers, and `StatefulComposable` contracts are covered in
[State Management](state-management.md).

## 5. Navigation

Each screen gets a `@Serializable NavKey`, a `Navigator` extension to reach it, and an entry
registration:

```kotlin
// Existing navigation excerpt; package/imports and navigateToHome omitted.
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

**For your feature:** create keys and a public `EntryProviderScope<NavKey>` registration function
in `feature/<feature>/navigation/`. Use `kotlinx.serialization.Serializable`,
`androidx.navigation3.runtime.NavKey` and `EntryProviderScope`,
`androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy`, and
`dev.atick.core.navigation.Navigator`. Import your module's internal screens there. Register each
key type once in the app's active entry provider and import the public registration function in
`JetpackApp.kt`.

For a new tab, add an enum entry to `app/.../navigation/TopLevelDestination.kt` with selected/unselected
icons, app string labels/title, and the new root key. Its `keys` property automatically includes enum
entries. Keep `START` unchanged unless intentionally changing the exit/start destination. Registering
an entry alone does not add a tab. For create, add an app callback using `navigateToItem(null)` (Home's
FAB is in `JetpackApp.kt`); the list click passes the existing ID. Adapt the app's Home-specific FAB
condition when adding another feature's create action. A nested-only screen needs neither a tab nor
a new top-level key.

Preserve the existing entry decorators, scene strategy, and explicit back handler. See
[Navigation](navigation.md) for tab restoration, identity constraints, and account-boundary behavior.

## 6. Dependency injection

The existing repository binding, including its containing module, is:

```kotlin
// Existing RepositoryModule excerpt; imports and unrelated bindings omitted.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    internal abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl,
    ): HomeRepository
}
```

Use `dagger.Binds`, `dagger.Module`, `dagger.hilt.InstallIn`,
`dagger.hilt.components.SingletonComponent`, and `javax.inject.Singleton`. The implementation's
constructor uses `javax.inject.Inject`. Add your binding to this existing module; do not create a
second `RepositoryModule` with the same package/name.

`core/room/di/DataSourceModule.kt` has the same `@Module`/`@InstallIn(SingletonComponent::class)`
container and binds `LocalDataSourceImpl` to `LocalDataSource`. The database and DAO use `@Provides`
because they need construction/accessor calls. Data sources inject the dispatcher with
`dev.atick.core.di.IoDispatcher`; [Data](data.md#dependency-injection) covers qualifiers and scopes.

The app already has `@HiltAndroidApp` on `App` and `@AndroidEntryPoint` on `MainActivity`.
Do not create another application class when adding a feature. Annotate normal injected ViewModels
with `@HiltViewModel`; an assisted ViewModel also needs its factory and the Screen's
`creationCallback`. Adding `@HiltViewModel` alone does not supply runtime arguments.

## Testing

This complete test file exercises the **existing Home ViewModel**, whose delete method propagates
returned Results correctly. Place `HomeFeatureExampleTest.kt` in
`feature/home/src/test/kotlin/dev/atick/feature/home/ui/home/` when trying the example. It starts active state collection, observes real fake-repository data, and suspends the
write so loading is observable. It checks a successful operation and a returned failure without
recommending Item's current failed-save behavior. No Hilt instrumentation graph is required.

```kotlin
package dev.atick.feature.home.ui.home

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.atick.core.testing.rule.MainDispatcherRule
import dev.atick.data.model.home.Jetpack
import dev.atick.data.repository.home.HomeRepository
import dev.atick.data.utils.SyncProgress
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HomeFeatureExampleTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `successful delete clears loading and updates the observed list`() = runTest {
        checkDelete(Result.success(Unit))
    }

    @Test
    fun `failed delete clears loading and preserves the item with an error`() = runTest {
        checkDelete(Result.failure(IllegalStateException("delete failed")))
    }

    private suspend fun checkDelete(result: Result<Unit>) {
        val item = Jetpack(id = "compose", name = "Compose")
        val repository = ExampleHomeRepository(item)
        val viewModel = HomeViewModel(repository)
        try {
            viewModel.homeUiState.test {
                var initial = awaitItem()
                while (initial.data.jetpacks.isEmpty()) initial = awaitItem()
                assertThat(initial.data.jetpacks).containsExactly(item)

                viewModel.deleteJetpack(item)
                assertThat(awaitItem().loading).isTrue()
                assertThat(repository.deletedId).isEqualTo(item.id)

                repository.completion.complete(result)
                val finished = expectMostRecentItem()
                assertThat(finished.loading).isFalse()
                assertThat(finished.error.peekContent()).isSameInstanceAs(result.exceptionOrNull())
                if (result.isSuccess) {
                    assertThat(finished.data.jetpacks).isEmpty()
                } else {
                    assertThat(finished.data.jetpacks).containsExactly(item)
                }
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }
}

private class ExampleHomeRepository(item: Jetpack) : HomeRepository {
    private val items = MutableStateFlow(listOf(item))
    val completion = CompletableDeferred<Result<Unit>>()
    var deletedId: String? = null
        private set

    override fun getJetpacks(): Flow<List<Jetpack>> = items
    override fun getJetpack(id: String): Flow<Jetpack> =
        items.map { rows -> rows.first { it.id == id } }

    override suspend fun createOrUpdateJetpack(jetpack: Jetpack): Result<Unit> =
        Result.success(Unit)

    override suspend fun markJetpackAsDeleted(jetpack: Jetpack): Result<Unit> {
        deletedId = jetpack.id
        return completion.await().also { result ->
            if (result.isSuccess) items.value = items.value.filterNot { it.id == jetpack.id }
        }
    }

    override suspend fun sync(): Flow<SyncProgress> = emptyFlow()
}
```

`MainDispatcherRule` runs eagerly by default, which makes the completion assertions deterministic
here. This is not an overlap/concurrency test. Adapt the fake to your own repository and assert its
observable behavior, returned failures, and the exact fields persisted. The
[Item input example](state-management.md#testing) separately covers raw price text.

### Validate the integrated feature

Run from the repository root, substituting the module name for an approved new feature:

```bash
./gradlew :feature:home:testDebugUnitTest :core:navigation:testDebugUnitTest
./gradlew :core:room:testDebugUnitTest :data:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache
```

Add an in-memory Room DAO test for the new entity/query, following `JetpackDaoTest`, and a repository
test with fake data sources covering mappings, local writes, and returned failures. Registering an
entity without exercising its DAO is insufficient. A database upgrade additionally needs migration
tests. Keep tests in their owning modules; constructor fakes need no `@HiltAndroidTest` setup.

On an emulator/device with Firebase configured, exercise list → create → save → edit → back, a
failed write, and tab switching. Rotate/recreate while a detail is open; verify the right tab and
arguments restore. Check narrow/wide layouts and back at both detail and top-level roots. Sign out
and confirm back cannot expose the old graph; test account-specific data separately. These are
manual acceptance checks, not claims that the JVM tests cover Firebase, remote sync, or UI rendering.
For synced features, also test worker invocation, offline writes and later sync, and the concurrency
cases in the [sync limitations](data.md#delivery-and-conflict-limitations).

## Further reading

- [Architecture](architecture.md) — where each of these layers sits in the module graph
- [State Management](state-management.md) — how `UiState` and its update functions work
- [Navigation](navigation.md) — the full Navigation 3 model, including assisted-injected ViewModels
- [Components](components.md) — the `core:ui` building blocks used in `HomeScreen`/`ItemScreen`
- [Data Layer](data.md) — the full sync algorithm and the `@Binds`/`@IoDispatcher` wiring behind it
- [Firebase Setup](firebase.md) — configuring the Firestore backend this feature syncs with
