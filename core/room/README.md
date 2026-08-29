# Module :core:room

**Purpose:** Local Room database — the single source of truth the UI reads from, with sync-tracking
metadata on every entity so `:sync` knows what still needs to reach Firestore.

## Key APIs

| API | What it does |
|---|---|
| `JetpackEntity` | `id`, `name`, `price`, `userId`, `lastUpdated`, `lastSynced`, `needsSync`, `deleted`, `syncAction` |
| `SyncAction` | `NONE`, `UPSERT`, `DELETE` |
| `JetpackDao` | `getJetpacks(userId)`, `getUnsyncedJetpacks(userId)`, `upsertJetpack`, `markJetpackAsDeleted` (soft delete), `markAsSynced`, `getLatestUpdateTimestamp` |
| `LocalDataSource` / `LocalDataSourceImpl` | Wraps `JetpackDao` behind an interface consumed by `:data` |

```kotlin
// core/room/src/main/kotlin/dev/atick/core/room/data/JetpackDao.kt
@Query("SELECT * FROM jetpacks WHERE userId = :userId AND (lastUpdated > lastSynced OR needsSync = 1)")
suspend fun getUnsyncedJetpacks(userId: String): List<JetpackEntity>

@Query("UPDATE jetpacks SET deleted = 1, needsSync = 1, syncAction = 'DELETE' WHERE id = :id")
suspend fun markJetpackAsDeleted(id: String)
```

## Gotchas

- `deleted` is a **soft delete** — a row marked deleted is hidden from `getJetpacks()` but kept until
  it's pushed to Firestore, so a deletion made offline isn't lost.
- `getLatestUpdateTimestamp` (max `lastUpdated`) is used as the pull cursor into Firestore's
  `whereGreaterThan("lastUpdated", ...)` — it depends on device clock, not a server timestamp.

## Related Documentation

- [Data Layer](../../docs/data.md) — the repository that reads/writes through this DAO, and the sync algorithm that drives `SyncAction`
- [Sync module](../../sync/README.md) — the `WorkManager` job that consumes `getUnsyncedJetpacks`
