# Module :sync

**Purpose:** Implements `SyncManager` from `:data` and schedules a `WorkManager` job.
`SyncWorker` collects the `Syncable` repository implementation in `:data`, which pushes then pulls.

## Key APIs

| API | What it does |
|---|---|
| `SyncManagerImpl` | Implements `SyncManager`; `isSyncing` reads WorkManager's work-info flow, `requestSync()` calls `Sync.initialize()` |
| `Sync.initialize()` | `enqueueUniqueWork(SYNC_WORK_NAME, ExistingWorkPolicy.KEEP, ...)` |
| `SyncWorker` | `@HiltWorker` + `@AssistedInject` (WorkManager supplies `Context`/`WorkerParameters`, not the graph); runs on `@IoDispatcher`, requests foreground execution; allows three retries after the initial attempt via `Result.retry()` |
| `DelegatingWorker` | WorkManager-created proxy that obtains `HiltWorkerFactory` through an entry point and forwards work/foreground calls to its injected delegate |

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
  cancellation. See [progress, retries, and errors](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/data.md#progress-retries-and-errors).
- Foreground configuration is incomplete: runtime `ForegroundInfo` has no service type, and the
  reviewed merged manifests lack the data-sync service type/permission. A separately reviewed fix
  and device verification are needed; see [foreground diagnostics](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/troubleshooting.md#sync).
- The only configured work constraint is `NetworkType.CONNECTED`. There is no battery/storage/idle constraint.
- The actual push-then-pull sync algorithm lives in `HomeRepositoryImpl.sync()` (`:data`), not here —
  this module schedules work, collects progress and manages worker retries.

## Related Documentation

- [Data Layer](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/data.md) — the sync algorithm this worker calls, with a sequence diagram
- [Troubleshooting](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/troubleshooting.md#sync) — sync-not-running, retry failures, notification permissions
- [Firebase Setup](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/firebase.md) — Firestore setup this sync target depends on
