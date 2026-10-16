# Module :data

**Purpose:** Repository implementations that sit between ViewModels and the four data sources
(Room, DataStore, Retrofit, Firestore), returning `Flow` for reads and `Result<T>` for writes.

## Key APIs

| API | What it does |
|---|---|
| `HomeRepository` / `HomeRepositoryImpl` | Offline-first repo for `Jetpack` — Room is the only thing read by the UI, Firestore is a sync target only, implements `Syncable` |
| `ProfileRepositoryImpl`, `SettingsRepositoryImpl` | Local-only repos — DataStore (+ Firebase Auth for sign-out), no Room, no sync |
| `SyncManager`, `Syncable`, `SyncProgress` (`utils/SyncUtils.kt`) | Sync contracts declared here; implemented in `:sync` (`SyncManagerImpl`) |
| `RepositoryModule` | One `@Binds` per repository, `@Singleton` |

```kotlin
// data/src/main/kotlin/dev/atick/data/repository/home/HomeRepository.kt
interface HomeRepository : Syncable {
    fun getJetpacks(): Flow<List<Jetpack>>
    suspend fun createOrUpdateJetpack(jetpack: Jetpack): Result<Unit>
    suspend fun markJetpackAsDeleted(jetpack: Jetpack): Result<Unit>
}
```

## Gotchas

- `SyncManager`/`Syncable` are declared here but implemented in `:sync` — reading this module's
  source alone won't show you how a sync request actually runs.
- `SyncAction` (`core/room`) is `NONE, UPSERT, DELETE`. There is no `CREATE`/`UPDATE`.
- Home writes update Room before requesting work. Local persistence/requesting work can fail;
  a successful Result does not acknowledge remote delivery. A failed enqueue can leave a local
  change committed. See [repository behavior](../docs/data.md#repository-pattern).
- Sync takes one pending snapshot, then pushes and pulls. It does not protect concurrent edits or
  guarantee a follow-up pass after a dropped request. See [delivery limitations](../docs/data.md#delivery-and-conflict-limitations).

## Related Documentation

- [Data Layer](../docs/data.md) — the full repository pattern, `suspendRunCatching` → `Result`, and the two-way sync algorithm
- [Guide](../docs/guide.md) — the `home` feature walkthrough built on this module
- [Sync module](../sync/README.md), [Core Room](../core/room/README.md), [Core Network](../core/network/README.md)
