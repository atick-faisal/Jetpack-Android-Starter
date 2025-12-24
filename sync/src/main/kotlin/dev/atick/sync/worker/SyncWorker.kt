/*
 * Copyright 2025 Atick Faisal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.atick.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.atick.core.di.IoDispatcher
import dev.atick.data.repository.home.HomeRepository
import dev.atick.sync.extensions.syncForegroundInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * WorkManager worker that synchronizes local data with Firebase Firestore in the background.
 *
 * This worker handles bidirectional data synchronization between the local Room database
 * and remote Firestore. It runs as a foreground service with a notification, showing
 * sync progress to the user.
 *
 * ## Sync Process
 * 1. **Pull Phase**: Download changes from Firestore since last sync
 * 2. **Push Phase**: Upload local changes (CREATE, UPDATE, DELETE) to Firestore
 * 3. **Progress Updates**: Update foreground notification with current progress
 * 4. **Result**: Mark sync as complete or retry on failure
 *
 * ## Foreground Service
 * The worker runs as an expedited foreground service to:
 * - Show sync progress in a notification
 * - Prevent the system from killing the process during sync
 * - Comply with Android 12+ background execution limits
 *
 * ## Retry Logic
 * - Automatic retry up to [TOTAL_SYNC_ATTEMPTS] (3) times
 * - Exponential backoff between retries (handled by WorkManager)
 * - Logs retry attempts for debugging
 * - Returns `Result.failure()` after exhausting all retries
 *
 * ## Dependency Injection
 * This class uses Hilt's `@AssistedInject` to inject dependencies into the Worker.
 * WorkManager parameters (`Context`, `WorkerParameters`) are provided by the `@Assisted`
 * annotation, while app dependencies are injected normally.
 *
 * ## Thread Safety
 * All work is executed on the [ioDispatcher] to ensure proper threading and avoid
 * blocking the main thread. The repository's sync method returns a Flow that emits
 * progress updates.
 *
 * @param context The application context provided by WorkManager
 * @param workerParameters Worker parameters provided by WorkManager
 * @param ioDispatcher The IO coroutine dispatcher for performing sync operations
 * @param homeRepository The repository that performs the actual sync logic
 *
 * @see Sync
 * @see SyncManager
 * @see dev.atick.data.repository.home.HomeRepository.sync
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val homeRepository: HomeRepository,
) : CoroutineWorker(context, workerParameters) {

    /**
     * Provides foreground information for the sync worker with default progress.
     *
     * This method is called by WorkManager when the worker needs to be promoted to
     * a foreground service. It returns notification information with zero progress.
     *
     * @return [ForegroundInfo] containing notification details for the foreground service
     */
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return getForegroundInfo(0, 0)
    }

    /**
     * Provides foreground information with specific sync progress.
     *
     * Creates a notification showing the current sync progress (e.g., "Syncing 5/10 items").
     * This notification is displayed while the worker runs as a foreground service.
     *
     * @param total The total number of items to sync
     * @param current The number of items synced so far
     * @return [ForegroundInfo] containing notification with progress details
     */
    private fun getForegroundInfo(total: Int, current: Int): ForegroundInfo {
        return context.syncForegroundInfo(total, current)
    }

    /**
     * Performs the background data synchronization work.
     *
     * This method:
     * 1. Promotes the worker to a foreground service with a progress notification
     * 2. Calls the repository's sync method which returns a Flow of progress updates
     * 3. Updates the notification as sync progresses
     * 4. Returns success/retry/failure based on the outcome
     *
     * ## Sync Flow
     * ```kotlin
     * homeRepository.sync() // Returns Flow<SyncProgress>
     *     .collect { progress ->
     *         // Update notification with current progress
     *         setForeground(getForegroundInfo(progress.total, progress.current))
     *     }
     * ```
     *
     * ## Error Handling
     * - **Retry**: If [runAttemptCount] < [TOTAL_SYNC_ATTEMPTS], returns `Result.retry()`
     * - **Failure**: After exhausting retries, returns `Result.failure()`
     * - **Success**: Returns `Result.success()` when sync completes without errors
     *
     * ## Threading
     * All operations run on [ioDispatcher] to avoid blocking the main thread.
     *
     * @return [Result.success] if sync completed successfully,
     *         [Result.retry] if sync failed but retries remain,
     *         [Result.failure] if sync failed after all retries
     */
    override suspend fun doWork(): Result {
        return withContext(ioDispatcher) {
            try {
                setForeground(getForegroundInfo())
                homeRepository.sync()
                    .flowOn(ioDispatcher)
                    .collect { progress ->
                        Timber.d("SyncWorker: Progress: $progress")
                        setForeground(
                            foregroundInfo = getForegroundInfo(
                                total = progress.total,
                                current = progress.current,
                            ),
                        )
                    }
                Result.success()
            } catch (e: Exception) {
                if (runAttemptCount < TOTAL_SYNC_ATTEMPTS) {
                    Timber.d(e, "Error syncing, retrying ($runAttemptCount/$TOTAL_SYNC_ATTEMPTS)")
                    Result.retry()
                } else {
                    Timber.e(e, "Error syncing")
                    Result.failure()
                }
            }
        }
    }

    companion object {
        /**
         * Maximum number of retry attempts for failed sync operations.
         *
         * If sync fails (due to network issues, server errors, etc.), WorkManager
         * will retry up to this many times before giving up. Retries use exponential
         * backoff (default: 10s, 20s, 40s).
         */
        const val TOTAL_SYNC_ATTEMPTS = 3

        /**
         * WorkManager constraints that must be met for sync to run.
         *
         * Current constraints:
         * - **Network**: CONNECTED (any network type - WiFi or cellular)
         *
         * Additional constraints can be added:
         * ```kotlin
         * val SyncConstraints = Constraints.Builder()
         *     .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi only
         *     .setRequiresBatteryNotLow(true) // Battery not low
         *     .setRequiresStorageNotLow(true) // Storage not low
         *     .build()
         * ```
         *
         * @see Constraints
         */
        val SyncConstraints
            get() = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        /**
         * Creates a WorkRequest for initial sync on app startup.
         *
         * This method creates a one-time expedited work request with:
         * - **Expedited**: Runs as soon as possible, promoted to foreground service
         * - **Unique**: Only one sync runs at a time (enforced by `enqueueUniqueWork`)
         * - **Network Constraint**: Only runs when network is available
         * - **Delegated**: Uses [DelegatingWorker] to enable Hilt injection
         *
         * ## Expedited Work
         * Expedited work is prioritized by WorkManager and runs in the foreground.
         * If the system quota for expedited work is exhausted, it falls back to
         * normal work (via [OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST]).
         *
         * ## Delegation Pattern
         * Uses [DelegatingWorker] to enable Hilt dependency injection in Workers.
         * The actual worker class ([SyncWorker]) is passed via `delegatedData()`.
         *
         * @return A [OneTimeWorkRequest] configured for startup sync
         * @see DelegatingWorker
         * @see Sync.initialize
         */
        fun startUpSyncWork(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<DelegatingWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(SyncConstraints)
                .setInputData(SyncWorker::class.delegatedData())
                .build()
        }
    }
}
