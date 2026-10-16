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
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.atick.data.utils.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * Requests unique sync work and observes whether any matching work is running.
 *
 * @param context Application context used to obtain WorkManager.
 */
internal class SyncManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SyncManager {
    /**
     * Emits true when any matching work is RUNNING, false otherwise.
     *
     * Waiting on constraints or retry backoff is not running. Slow collectors may skip
     * intermediate values because this Flow is conflated.
     */
    override val isSyncing: Flow<Boolean> =
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            .map(List<WorkInfo>::anyRunning)
            .conflate()

    /**
     * Requests work through [Sync.initialize] without waiting for remote completion.
     *
     * An unfinished request is kept, so this call does not guarantee a new pass.
     */
    override fun requestSync() {
        Timber.d("Requesting sync")
        Sync.initialize(context)
    }
}

/**
 * Extension function to check if any WorkInfo in the list is currently running.
 *
 * This is a convenience function used to determine sync status from WorkManager's
 * work info list. A sync is considered "running" only if its state is [State.RUNNING].
 *
 * ## WorkInfo States
 * - **ENQUEUED**: Work is scheduled but not yet running
 * - **RUNNING**: Work is actively executing (this function returns `true`)
 * - **SUCCEEDED**: Work completed successfully
 * - **FAILED**: Work failed after all retries
 * - **BLOCKED**: Work is waiting for prerequisites
 * - **CANCELLED**: Work was cancelled
 *
 * @receiver List of [WorkInfo] from WorkManager
 * @return `true` if any work is in RUNNING state, `false` otherwise
 */
private fun List<WorkInfo>.anyRunning() = any { it.state == State.RUNNING }
