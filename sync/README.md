# Module :sync

**Purpose:** Implements the `SyncManager`/`Syncable` contracts declared in `:data` — background sync
via a `WorkManager` job, `SyncWorker`, that pushes local changes then pulls remote ones.

## Key APIs

| API | What it does |
|---|---|
| `SyncManagerImpl` | Implements `SyncManager`; `isSyncing` reads WorkManager's work-info flow, `requestSync()` calls `Sync.initialize()` |
| `Sync.initialize()` | `enqueueUniqueWork(SYNC_WORK_NAME, ExistingWorkPolicy.KEEP, ...)` |
| `SyncWorker` | `@HiltWorker` + `@AssistedInject` (WorkManager supplies `Context`/`WorkerParameters`, not the graph); runs on `@IoDispatcher`, promoted to a foreground service; retries up to `TOTAL_SYNC_ATTEMPTS = 3` via `Result.retry()` |
| `DelegatingWorker` | Lets Hilt construct `SyncWorker` at all — WorkManager instantiates workers itself, not through injection |

```kotlin
// sync/src/main/kotlin/dev/atick/sync/worker/SyncWorker.kt
val SyncConstraints
    get() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
```

## Gotchas

- `ExistingWorkPolicy.KEEP` silently drops a new request if a sync is already enqueued or running —
  see [Data Layer](../docs/data.md#two-way-sync) for why that's safe rather than lossy.
- The only real constraint is `NetworkType.CONNECTED`. There is no battery/storage/idle constraint.
- The actual push-then-pull sync algorithm lives in `HomeRepositoryImpl.sync()` (`:data`), not here —
  this module only schedules and retries it.

## Related Documentation

- [Data Layer](../docs/data.md) — the sync algorithm this worker calls, with a sequence diagram
- [Troubleshooting](../docs/troubleshooting.md#sync) — sync-not-running, retry failures, notification permissions
- [Firebase Setup](../docs/firebase.md) — Firestore setup this sync target depends on
