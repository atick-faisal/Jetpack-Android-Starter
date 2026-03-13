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
 * Manager interface for controlling application-wide data synchronization.
 *
 * This is typically implemented by the sync module and injected into the app layer
 * to provide access to sync status and manual sync triggering.
 *
 * @see SyncManagerImpl Implementation in the sync module
 * @see Syncable Interface implemented by repositories that support sync
 */
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
 *
 * Repositories that implement this interface can be synchronized in the background
 * by SyncWorker. The [sync] method should handle fetching remote data and updating
 * the local database while emitting progress updates.
 *
 * ## Implementation Pattern
 * ```kotlin
 * override suspend fun sync(): Flow<SyncProgress> = flow {
 *     emit(SyncProgress(total = 100, current = 0, message = "Starting sync"))
 *     // Fetch from network
 *     val data = networkDataSource.getData()
 *     // Update local database
 *     localDataSource.saveData(data)
 *     emit(SyncProgress(total = 100, current = 100, message = "Sync complete"))
 * }
 * ```
 *
 * @sample dev.atick.data.repository.home.HomeRepositoryImpl.sync Complete sync implementation with push/pull
 *
 * @see SyncProgress For progress reporting
 * @see SyncManager For triggering sync operations
 */
interface Syncable {
    /**
     * Synchronizes data and returns a Flow emitting the progress of the sync operation.
     *
     * @return A Flow emitting SyncProgress objects representing the progress of the sync operation.
     * @sample dev.atick.data.repository.home.HomeRepositoryImpl.sync Complete sync implementation
     */
    suspend fun sync(): Flow<SyncProgress>
}

/**
 * Data class that represents the progress of a sync operation.
 *
 * This is emitted by [Syncable.sync] implementations to report progress to SyncWorker,
 * which displays it in a foreground service notification.
 *
 * @param total The total number of items to sync.
 * @param current The current number of items synced.
 * @param message The message to display during the sync operation.
 *
 * @see Syncable Interface implemented by syncable repositories
 */
data class SyncProgress(
    val total: Int = 0,
    val current: Int = 0,
    val message: String? = null,
)
