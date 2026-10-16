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

package dev.atick.sync.manager

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dev.atick.sync.worker.SyncWorker
import timber.log.Timber

/**
 * Enqueues unique one-time background sync work through WorkManager.
 *
 * [SyncWorker] collects the repository's push-then-pull Flow. Requests use
 * [ExistingWorkPolicy.KEEP]; a request ignored while unfinished work exists does not schedule
 * another pass. No periodic work is configured.
 *
 * The request requires a connected network and asks for expedited execution, with fallback
 * to ordinary work when quota is unavailable. Execution timing is controlled by WorkManager.
 */
object Sync {
    /**
     * Requests sync without awaiting enqueue completion or remote persistence.
     *
     * Matching unfinished work is kept; otherwise a new request is enqueued. New writes made
     * after the active pass's pending snapshot need a later trigger, which is not guaranteed here.
     *
     * @param context Context used to obtain WorkManager.
     */
    fun initialize(context: Context) {
        WorkManager.getInstance(context).apply {
            // KEEP does not queue a follow-up pass behind unfinished work.
            Timber.d("Sync: initialize")
            enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                SyncWorker.startUpSyncWork(),
            )
        }
    }
}

/**
 * The unique name identifier for the sync worker.
 *
 * This constant is used to:
 * - Uniquely identify the sync worker in WorkManager's queue
 * - Ensure only one sync worker runs at a time (via `enqueueUniqueWork`)
 * - Query the sync worker's status in [SyncManager]
 *
 * ## Usage
 * ```kotlin
 * // Enqueue sync work
 * WorkManager.getInstance(context).enqueueUniqueWork(
 *     SYNC_WORK_NAME,
 *     ExistingWorkPolicy.KEEP,
 *     syncWorkRequest
 * )
 *
 * // Query sync status
 * val workInfo = WorkManager.getInstance(context)
 *     .getWorkInfosForUniqueWork(SYNC_WORK_NAME)
 * ```
 */
internal const val SYNC_WORK_NAME = "dev.atick.jetpack.sync.worker"
