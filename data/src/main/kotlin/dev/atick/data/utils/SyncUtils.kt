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

interface SyncManager {
    /**
     * Flow that emits a boolean value indicating whether the sync operation is in progress.
     */
    val isSyncing: Flow<Boolean>

    /**
     * Starts the sync operation.
     */
    fun requestSync()
}

/**
 * Interface representing a syncable entity.
 */
interface Syncable {
    /**
     * Synchronizes data and returns a Flow emitting the progress of the sync operation.
     *
     * @return A Flow emitting SyncProgress objects representing the progress of the sync operation.
     */
    suspend fun sync(): Flow<SyncProgress>
}

/**
 * Data class that represents the progress of a sync operation.
 *
 * @param total The total number of items to sync.
 * @param current The current number of items synced.
 * @param message The message to display during the sync operation.
 */
data class SyncProgress(
    val total: Int = 0,
    val current: Int = 0,
    val message: String? = null,
)
