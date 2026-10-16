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
 * Collects the repository's push-then-pull sync Flow and updates foreground notification counts.
 *
 * The repository owns persistence and conflict behavior. Completion does not establish that
 * concurrent edits or every remote record were delivered. Exceptions request retry while
 * [runAttemptCount] is below [TOTAL_SYNC_ATTEMPTS]; the broad catch includes cancellation.
 *
 * WorkManager supplies assisted parameters; Hilt supplies the dispatcher and repository.
 *
 * @param context Application context provided by WorkManager.
 * @param workerParameters Runtime parameters provided by WorkManager.
 * @param ioDispatcher Dispatcher for sync work.
 * @param homeRepository Repository implementing the sync algorithm.
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
     * Collects sync progress on [ioDispatcher] and updates foreground counts.
     *
     * Earlier acknowledgements and upserts remain committed if a later operation fails.
     * The catch includes cancellation and does not explicitly rethrow it.
     *
     * @return Success on completed collection; retry below the retry limit, otherwise failure.
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
         * Retry limit compared with zero-based [runAttemptCount].
         *
         * A failure at counts 0, 1, or 2 requests retry; a failure at 3 returns failure, allowing
         * four attempts in total. No custom backoff is set: WorkManager defaults to exponential
         * backoff starting at 30 seconds. Constraints and scheduling can delay execution further.
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
         * Creates connected-network, one-time work targeting [SyncWorker] through [DelegatingWorker].
         *
         * Requests expedited execution, falling back to ordinary work when quota is unavailable.
         * [dev.atick.sync.manager.Sync.initialize] supplies the unique-work name and KEEP policy.
         *
         * @return The request; constructing it does not enqueue it.
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
