# Module :sync

This module handles background data synchronization using WorkManager. It ensures data consistency
between local and remote data sources by performing periodic and on-demand sync operations.

## Features

- Background Synchronization
- Periodic Sync Scheduling
- Work Constraints Management
- Progress Tracking
- Error Handling
- Hilt Worker Integration

## Dependencies Graph

```mermaid
graph TD
    A[sync] --> B[core:android]
    A --> C[data]
    A --> D[androidx.work]
    D --> E[work.runtime.ktx]
    D --> F[hilt.work]

    subgraph "Work Manager"
        D
        E
        F
    end
```

## Usage

```kotlin
dependencies {
    implementation(project(":sync"))
}
```

### Setting Up Sync

1. Make your repository syncable:

	```kotlin
	interface YourRepository : Syncable {
	    override suspend fun sync(): Flow<SyncProgress>
	}
	```

2. Create sync worker:

	```kotlin
	@HiltWorker
	class SyncWorker @AssistedInject constructor(
	    @Assisted context: Context,
	    @Assisted params: WorkerParameters,
	    private val repository: YourRepository
	) : CoroutineWorker(context, params) {
	    override suspend fun doWork(): Result {
	        repository.sync()
	            .collect { progress ->
	                setProgress(progress.toWorkData())
	            }
	        return Result.success()
	    }
	}
	```

3. Request sync operation:

	```kotlin
	class YourRepositoryImpl @Inject constructor(
	    private val syncManager: SyncManager
	) : YourRepository {
	    fun requestSync() {
	        syncManager.requestSync()
	    }
	}
	```

### Work Constraints

The sync operation respects the following constraints:

- Network availability
- Battery not low
- Storage not low
- Device idle (for periodic sync)

### Progress Tracking

```kotlin
data class SyncProgress(
    val total: Int = 0,
    val current: Int = 0,
    val message: String? = null
)
```

The sync progress can be observed from the WorkManager's progress updates.

## Troubleshooting

See [Troubleshooting Guide](../docs/troubleshooting.md#sync) for sync-specific symptom → fix entries (sync not running, failing retries, missing notifications, constraints, conflict resolution, ADB debugging).

### Related Documentation

- **[Troubleshooting Guide](../docs/troubleshooting.md)** - General troubleshooting patterns
- **[Firebase Setup](../docs/firebase.md)** - Firestore setup and security rules
- **[Data Repository Patterns](../data/README.md)** - Repository implementation patterns
- **[WorkManager Documentation](https://developer.android.com/topic/libraries/architecture/workmanager)** - Official Android WorkManager guide

### Implementation Reference

- **SyncWorker**: `sync/src/main/kotlin/dev/atick/sync/worker/SyncWorker.kt`
- **SyncManager**: `sync/src/main/kotlin/dev/atick/sync/SyncManager.kt`