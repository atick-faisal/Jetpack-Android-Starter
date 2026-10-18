# Module :data

**Purpose:** Repository implementations that sit between ViewModels and storage/auth sources:
Room, JSON DataStore, Firebase Auth and Firestore, with an optional REST source available for
extensions. Observable data uses `Flow`; one-time operations return `Result<T>`.

## Key APIs

| API | What it does |
|---|---|
| `HomeRepository` / `HomeRepositoryImpl` | Offline-first repo for `Jetpack` — Room is the only thing read by the UI, Firestore is a sync target only, implements `Syncable` |
| `AuthRepository` / `AuthRepositoryImpl` | Saved-credential, email/password and Google authentication; successful auth updates local profile preferences |
| `ProfileRepositoryImpl`, `SettingsRepositoryImpl` | Local-only repos — DataStore (+ Firebase Auth for sign-out), no Room, no sync |
| `SyncManager`, `Syncable`, `SyncProgress` (`utils/SyncUtils.kt`) | Scheduler interface (`SyncManager`), repository contract (`Syncable`), and progress model; `:sync` implements the manager, `HomeRepositoryImpl` implements `Syncable` |
| `RepositoryModule` | One `@Binds` per repository, `@Singleton` |

Interface excerpt; the inherited `sync()` contract is defined by `Syncable`.

```kotlin
// data/src/main/kotlin/dev/atick/data/repository/home/HomeRepository.kt
interface HomeRepository : Syncable {
    fun getJetpacks(): Flow<List<Jetpack>>
    fun getJetpack(id: String): Flow<Jetpack>
    suspend fun createOrUpdateJetpack(jetpack: Jetpack): Result<Unit>
    suspend fun markJetpackAsDeleted(jetpack: Jetpack): Result<Unit>
}
```

## Gotchas

- `HomeRepositoryImpl.sync()` returns a cold progress flow; collection executes the push/pull.
  `SyncWorker` in `:sync` collects it. Calling the suspend method alone does not execute the phases.
- `SyncAction` (`core/room`) is `NONE, UPSERT, DELETE`. There is no `CREATE`/`UPDATE`.
- Home writes update Room before requesting work. Local persistence/requesting work can fail;
  a successful Result does not acknowledge remote delivery. A failed enqueue can leave a local
  change committed. See [repository behavior](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md#repository-pattern).
- Sync takes one pending snapshot, then pushes and pulls. It does not protect concurrent edits or
  guarantee a follow-up pass after a dropped request. See [delivery limitations](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md#delivery-and-conflict-limitations).

## Related Documentation

- [Data Layer](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md) — the full repository pattern, `suspendRunCatching` → `Result`, and the two-way sync algorithm
- [Guide](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/guide.md) — the `home` feature walkthrough built on this module
- [Sync algorithm](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md#two-way-sync), [Room persistence](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md#room-persistence-and-schema-changes), [Data source choices](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md#data-sources)
