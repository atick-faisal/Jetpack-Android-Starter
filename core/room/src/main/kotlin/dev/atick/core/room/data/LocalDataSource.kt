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
 * Room operations used by the Home repository for content and pending-sync metadata.
 *
 * User-scoped lists exclude soft-deleted rows. Pending queries include rows with needsSync set
 * or lastUpdated greater than lastSynced. The stored server timestamp supplies the pull cursor.
 * Writes do not schedule sync; the repository requests it separately.
 *
 * @see JetpackEntity
 * @see JetpackDao
 */
interface LocalDataSource {
    /**
     * Observes a user's non-deleted jetpacks, newest edit first.
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
     * Clears pending flags by ID and sets lastSynced, without checking the uploaded version.
     *
     * @param id The ID of the jetpack to mark as synced.
     * @param timestamp The timestamp to set as the last synced time. Defaults to the current system time.
     */
    suspend fun markAsSynced(id: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Returns the largest stored server write time, including soft-deleted rows.
     * This timestamp alone does not establish a complete pull checkpoint.
     *
     * @param userId The ID of the user whose jetpacks to check.
     * @return The newest server timestamp in nanoseconds, or 0 if no timestamp is stored for the user.
     */
    suspend fun getSyncCursor(userId: String): Long
}
