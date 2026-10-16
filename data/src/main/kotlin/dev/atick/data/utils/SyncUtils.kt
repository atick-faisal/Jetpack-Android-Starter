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

package dev.atick.data.utils

import kotlinx.coroutines.flow.Flow

/**
 * Contract for requesting background sync and observing running state.
 *
 * The implementation is supplied by the sync module.
 */
interface SyncManager {
    /**
     * Emits whether matching work is running; waiting or failed work is not running.
     */
    val isSyncing: Flow<Boolean>

    /**
     * Requests background work without awaiting completion. The scheduler may keep existing work.
     */
    fun requestSync()
}

/**
 * Repository contract for synchronization with progress reporting.
 *
 * The Home implementation returns a cold Flow: collect it to execute its push and pull phases.
 * Calling the suspend method alone does not execute those phases.
 *
 * @see SyncProgress
 * @see SyncManager
 */
interface Syncable {
    /**
     * Returns synchronization progress. Execution and error behavior depend on the implementation.
     *
     * @return A [Flow] of [SyncProgress]; Home performs I/O during collection and may throw.
     */
    suspend fun sync(): Flow<SyncProgress>
}

/**
 * Data class that represents the progress of a sync operation.
 *
 * This is emitted by [Syncable.sync] implementations to report progress to SyncWorker,
 * which displays it in a foreground service notification.
 *
 * @param total Number of items in the current phase, not an overall operation total.
 * @param current Number of items processed in the current phase.
 * @param message The message to display during the sync operation.
 *
 * @see Syncable Interface implemented by syncable repositories
 */
data class SyncProgress(
    val total: Int = 0,
    val current: Int = 0,
    val message: String? = null,
)
