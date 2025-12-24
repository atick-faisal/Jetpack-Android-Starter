/*
 * Copyright 2023 Atick Faisal
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

package dev.atick.core.room.data

import dev.atick.core.room.model.JetpackEntity
import kotlinx.coroutines.flow.Flow

/**
 * Local data source for managing offline-first data persistence using Room database.
 *
 * This interface defines the contract for local database operations, enabling:
 * - **Offline-first architecture**: Data is always available locally
 * - **Sync management**: Tracks which entities need synchronization with remote
 * - **Soft deletes**: Marks items as deleted instead of removing immediately
 * - **Reactive queries**: Returns [Flow] for real-time UI updates
 *
 * ## Design Pattern
 *
 * This follows the **Repository Pattern with Local Data Source**:
 * - Abstracts Room DAO implementation details
 * - Provides domain-agnostic data access
 * - Handles sync metadata automatically
 * - Executes queries on IO dispatcher (handled by Room)
 *
 * ## Sync Strategy
 *
 * The data source tracks sync state using:
 * - `lastUpdated`: When the entity was last modified locally
 * - `lastSynced`: When the entity was last synced with remote
 * - `needsSync`: Flag indicating sync is required
 * - `syncAction`: Action to perform during sync (UPSERT, DELETE, NONE)
 *
 * ## Usage in Repositories
 *
 * ```kotlin
 * class JetpackRepository @Inject constructor(
 *     private val localDataSource: LocalDataSource,
 *     private val networkDataSource: NetworkDataSource
 * ) {
 *     // Reactive query for UI
 *     fun observeJetpacks(userId: String): Flow<List<Jetpack>> =
 *         localDataSource.getJetpacks(userId)
 *             .map { entities -> entities.map { it.toDomain() } }
 *
 *     // Sync local changes to remote
 *     suspend fun syncLocalChanges(userId: String): Result<Unit> = suspendRunCatching {
 *         val unsyncedItems = localDataSource.getUnsyncedJetpacks(userId)
 *         unsyncedItems.forEach { entity ->
 *             when (entity.syncAction) {
 *                 SyncAction.UPSERT -> networkDataSource.upsert(entity.toNetwork())
 *                 SyncAction.DELETE -> networkDataSource.delete(entity.id)
 *                 SyncAction.NONE -> {}
 *             }
 *             localDataSource.markAsSynced(entity.id)
 *         }
 *     }
 *
 *     // Sync remote changes to local
 *     suspend fun syncRemoteChanges(userId: String): Result<Unit> = suspendRunCatching {
 *         val lastUpdate = localDataSource.getLatestUpdateTimestamp(userId)
 *         val remoteChanges = networkDataSource.getChanges(since = lastUpdate)
 *         localDataSource.upsertJetpacks(remoteChanges.map { it.toEntity() })
 *     }
 * }
 * ```
 *
 * @see JetpackEntity
 * @see JetpackDao
 * @see SyncAction
 */
interface LocalDataSource {
    /**
     * Retrieves a list of jetpacks for a specific user.
     *
     * @param userId The ID of the user whose jetpacks to retrieve.
     * @return A Flow emitting a list of [JetpackEntity] objects.
     */
    fun getJetpacks(userId: String): Flow<List<JetpackEntity>>

    /**
     * Retrieves a specific jetpack by its ID.
     *
     * @param id The ID of the jetpack to retrieve.
     * @return A Flow emitting the [JetpackEntity] object.
     */
    fun getJetpack(id: String): Flow<JetpackEntity>

    /**
     * Retrieves a list of jetpacks for a specific user that need to be synced.
     *
     * @param userId The ID of the user whose unsynced jetpacks to retrieve.
     * @return A list of [JetpackEntity] objects that need to be synced.
     */
    suspend fun getUnsyncedJetpacks(userId: String): List<JetpackEntity>

    /**
     * Inserts a new jetpack into the database.
     *
     * @param jetpackEntity The [JetpackEntity] object to insert.
     */
    suspend fun insertJetpack(jetpackEntity: JetpackEntity)

    /**
     * Inserts or updates a jetpack in the database.
     *
     * @param jetpackEntity The [JetpackEntity] object to upsert.
     */
    suspend fun upsertJetpack(jetpackEntity: JetpackEntity)

    /**
     * Upserts (insert or update) jetpacks from a remote source.
     *
     * @param remoteJetpacks List of jetpack entities from the remote source to be upserted.
     */
    suspend fun upsertJetpacks(remoteJetpacks: List<JetpackEntity>)

    /**
     * Updates an existing jetpack in the database.
     *
     * @param jetpackEntity The [JetpackEntity] object to update.
     */
    suspend fun updateJetpack(jetpackEntity: JetpackEntity)

    /**
     * Marks a jetpack as deleted.
     *
     * @param id The ID of the jetpack to mark as deleted.
     */
    suspend fun markJetpackAsDeleted(id: String)

    /**
     * Permanently deletes a jetpack from the database.
     *
     * @param id The ID of the jetpack to delete.
     */
    suspend fun deleteJetpackPermanently(id: String)

    /**
     * Marks a jetpack as synced.
     *
     * @param id The ID of the jetpack to mark as synced.
     * @param timestamp The timestamp to set as the last synced time. Defaults to the current system time.
     */
    suspend fun markAsSynced(id: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Gets the most recent lastUpdated timestamp for a specific user's jetpacks.
     *
     * @param userId The ID of the user whose jetpacks to check.
     * @return The most recent lastUpdated timestamp for that user's jetpacks.
     */
    suspend fun getLatestUpdateTimestamp(userId: String): Long
}
