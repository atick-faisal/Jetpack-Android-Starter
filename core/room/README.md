# Module :core:room

**Purpose:** Local Room storage observed by the Home UI, with metadata used by
`HomeRepositoryImpl` in `:data` to select pending changes for Firestore sync.

## Key APIs

| API | What it does |
|---|---|
| `JetpackEntity` | `id`, `name`, `price`, `userId`, `lastUpdated`, `lastSynced`, `serverUpdatedAtNanos`, `needsSync`, `deleted`, `syncAction` |
| `SyncAction` | `NONE`, `UPSERT`, `DELETE` |
| `JetpackDao` | `getJetpacks(userId)`, `getUnsyncedJetpacks(userId)`, `upsertJetpack`, `markJetpackAsDeleted` (soft delete), `markAsSynced`, `getSyncCursor` |
| `LocalDataSource` / `LocalDataSourceImpl` | Wraps `JetpackDao` behind an interface consumed by `:data` |

```kotlin
// core/room/src/main/kotlin/dev/atick/core/room/data/JetpackDao.kt
@Query("SELECT * FROM jetpacks WHERE userId = :userId AND (lastUpdated > lastSynced OR needsSync = 1)")
suspend fun getUnsyncedJetpacks(userId: String): List<JetpackEntity>

@Query("UPDATE jetpacks SET deleted = 1, needsSync = 1, syncAction = 'DELETE' WHERE id = :id")
suspend fun markJetpackAsDeleted(id: String)
```

## Gotchas

- `deleted` is a soft delete: hidden from the list, retained locally after remote deletion is
  acknowledged. No automatic cleanup is implemented; remote hard deletes do not remove other
  devices' existing local rows through the incremental pull.
- `lastUpdated` and `lastSynced` are device-clock milliseconds used in pending-change detection;
  both are transmitted to/from Firestore. `serverUpdatedAtNanos` is the server write timestamp
  used by the pull cursor. See [metadata and algorithm](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md#algorithm-and-metadata).
- `getSyncCursor` includes soft-deleted rows because their server timestamps are already stored.
  It is a timestamp-only cursor, with interruption/tie limitations described in the guide.
- Version 2 has no registered migration from version 1 and uses destructive fallback. Upgrades can
  erase unsent local changes. Same-version schema-integrity errors are a different failure path;
  see [schema changes](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md#room-persistence-and-schema-changes).

## Related Documentation

- [Data Layer](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md) — the repository that reads/writes through this DAO, and the sync algorithm that drives `SyncAction`
- [Sync algorithm](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md#two-way-sync) — the `WorkManager` job that consumes `getUnsyncedJetpacks`
