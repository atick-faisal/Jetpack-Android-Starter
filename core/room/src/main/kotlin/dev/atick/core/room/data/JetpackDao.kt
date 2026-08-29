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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import dev.atick.core.room.model.JetpackEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for handling [JetpackEntity] operations.
 */
@Dao
interface JetpackDao {
    /**
     * Retrieves a list of jetpacks for a specific user that are not marked as deleted,
     * ordered by the last updated timestamp in descending order.
     *
     * @param userId The ID of the user whose jetpacks to retrieve.
     * @return A Flow emitting a list of [JetpackEntity] objects.
     */
    @Query("SELECT * FROM jetpacks WHERE userId = :userId AND deleted = 0 ORDER BY lastUpdated DESC")
    fun getJetpacks(userId: String): Flow<List<JetpackEntity>>

    /**
     * Retrieves a specific jetpack by its ID.
     *
     * @param id The ID of the jetpack to retrieve.
     * @return A Flow emitting the [JetpackEntity] object.
     */
    @Query("SELECT * FROM jetpacks WHERE id = :id")
    fun getJetpack(id: String): Flow<JetpackEntity>

    /**
     * Retrieves a list of jetpacks for a specific user that need to be synced.
     * A jetpack needs to be synced if it was updated after the last sync or if it is marked as needing sync.
     *
     * @param userId The ID of the user whose unsynced jetpacks to retrieve.
     * @return A list of [JetpackEntity] objects that need to be synced.
     */
    @Query("SELECT * FROM jetpacks WHERE userId = :userId AND (lastUpdated > lastSynced OR needsSync = 1)")
    suspend fun getUnsyncedJetpacks(userId: String): List<JetpackEntity>

    /**
     * Inserts a new jetpack into the database. If a jetpack with the same ID already exists, it will be replaced.
     *
     * @param jetpackEntity The [JetpackEntity] object to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJetpack(jetpackEntity: JetpackEntity)

    /**
     * Inserts or updates a jetpack in the database.
     * If a jetpack with the same ID already exists, it will be updated; otherwise, a new entry will be created.
     *
     * @param jetpackEntity The [JetpackEntity] object to upsert.
     */
    @Upsert
    suspend fun upsertJetpack(jetpackEntity: JetpackEntity)

    /**
     * Upserts (insert or update) jetpacks from a remote source.
     * If a jetpack already exists locally (matching ID), it will be updated with the remote version.
     * If it doesn't exist locally, a new entry will be created.
     *
     * @param remoteJetpacks List of jetpack entities from the remote source to be upserted.
     * @return List of row IDs for the inserted rows.
     */
    @Transaction
    @Upsert
    suspend fun upsertJetpacks(remoteJetpacks: List<JetpackEntity>)

    /**
     * Updates an existing jetpack in the database.
     *
     * @param jetpackEntity The [JetpackEntity] object to update.
     */
    @Update
    suspend fun updateJetpack(jetpackEntity: JetpackEntity)

    /**
     * Marks a jetpack as deleted by setting the deleted flag to 1 and the needsSync flag to 1.
     * Also sets the syncAction to 'DELETE'.
     *
     * @param id The ID of the jetpack to mark as deleted.
     */
    @Query("UPDATE jetpacks SET deleted = 1, needsSync = 1, syncAction = 'DELETE' WHERE id = :id")
    suspend fun markJetpackAsDeleted(id: String)

    /**
     * Permanently deletes a jetpack from the database.
     *
     * @param id The ID of the jetpack to delete.
     */
    @Query("DELETE FROM jetpacks WHERE id = :id")
    suspend fun deleteJetpackPermanently(id: String)

    /**
     * Marks a jetpack as synced by setting the needsSync flag to 0, the syncAction to 'NONE',
     * and updating the lastSynced timestamp.
     *
     * @param id The ID of the jetpack to mark as synced.
     * @param timestamp The timestamp to set as the last synced time. Defaults to the current system time.
     */
    @Query("UPDATE jetpacks SET needsSync = 0, syncAction = 'NONE', lastSynced = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Gets the newest remote server timestamp this device has already stored, which is the cursor
     * the next pull resumes from.
     *
     * ⚠️ This reads `serverUpdatedAtNanos` and never `lastUpdated`. `lastUpdated` is stamped by
     * whichever device edited the row, so using it here makes the cursor jump to the reading of the
     * furthest-ahead clock in the fleet, and every record written by a device behind that clock is
     * then skipped forever with no error. Soft-deleted rows count too: their server timestamp has
     * still been consumed, and excluding them would rewind the cursor.
     *
     * @param userId The ID of the user whose jetpacks to check.
     * @return The newest server timestamp in nanoseconds, or null if the user has no jetpacks.
     */
    @Query("SELECT MAX(serverUpdatedAtNanos) FROM jetpacks WHERE userId = :userId")
    suspend fun getSyncCursor(userId: String): Long?
}
