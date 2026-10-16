# Module :sync

**Purpose:** Implements `SyncManager` from `:data` and schedules a `WorkManager` job.
`SyncWorker` collects the `Syncable` repository implementation in `:data`, which pushes then pulls.

## Key APIs

| API | What it does |
|---|---|
| `SyncManagerImpl` | Implements `SyncManager`; `isSyncing` reads WorkManager's work-info flow, `requestSync()` calls `Sync.initialize()` |
| `Sync.initialize()` | `enqueueUniqueWork(SYNC_WORK_NAME, ExistingWorkPolicy.KEEP, ...)` |
| `SyncWorker` | `@HiltWorker` + `@AssistedInject` (WorkManager supplies `Context`/`WorkerParameters`, not the graph); runs on `@IoDispatcher`, promoted to a foreground service; allows three retries after the initial attempt via `Result.retry()` |
| `DelegatingWorker` | Lets Hilt construct `SyncWorker` at all — WorkManager instantiates workers itself, not through injection |

```kotlin
// sync/src/main/kotlin/dev/atick/sync/worker/SyncWorker.kt
val SyncConstraints
    get() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
```

## Gotchas

- `ExistingWorkPolicy.KEEP` ignores a request while matching unfinished work exists. It does not
  schedule another pass for writes made after the repository's pending snapshot.
- No custom backoff is set: the three retry delays nominally start at 30, 60, and 120 seconds.
  Constraints/system scheduling can delay execution. `isSyncing` is true only for RUNNING work.
- The worker catches `Exception`, including cancellation; it does not explicitly rethrow
  cancellation. See [progress, retries, and errors](../docs/data.md#progress-retries-and-errors).
- The only real constraint is `NetworkType.CONNECTED`. There is no battery/storage/idle constraint.
- The actual push-then-pull sync algorithm lives in `HomeRepositoryImpl.sync()` (`:data`), not here —
  this module only schedules and retries it.

## Related Documentation

- [Data Layer](../docs/data.md) — the sync algorithm this worker calls, with a sequence diagram
- [Troubleshooting](../docs/troubleshooting.md#sync) — sync-not-running, retry failures, notification permissions
- [Firebase Setup](../docs/firebase.md) — Firestore setup this sync target depends on
