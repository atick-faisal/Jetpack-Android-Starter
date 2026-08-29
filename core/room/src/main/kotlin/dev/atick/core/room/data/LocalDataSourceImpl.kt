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

import dev.atick.core.di.IoDispatcher
import dev.atick.core.room.model.JetpackEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of [LocalDataSource] that interacts with the local storage using [JetpackDao].
 *
 * @param jetpackDao The data access object for performing database operations.
 * @param ioDispatcher The coroutine dispatcher for performing IO-bound tasks.
 */
internal class LocalDataSourceImpl @Inject constructor(
    private val jetpackDao: JetpackDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LocalDataSource {

    /**
     * Retrieves a list of jetpacks for a specific user.
     *
     * @param userId The ID of the user whose jetpacks to retrieve.
     * @return A Flow emitting a list of [JetpackEntity] objects.
     */
    override fun getJetpacks(userId: String): Flow<List<JetpackEntity>> =
        jetpackDao.getJetpacks(userId).flowOn(ioDispatcher)

    /**
     * Retrieves a specific jetpack by its ID.
     *
     * @param id The ID of the jetpack to retrieve.
     * @return A Flow emitting the [JetpackEntity] object.
     */
    override fun getJetpack(id: String): Flow<JetpackEntity> =
        jetpackDao.getJetpack(id).flowOn(ioDispatcher)

    /**
     * Retrieves a list of jetpacks for a specific user that need to be synced.
     *
     * @param userId The ID of the user whose unsynced jetpacks to retrieve.
     * @return A list of [JetpackEntity] objects that need to be synced.
     */
    override suspend fun getUnsyncedJetpacks(userId: String): List<JetpackEntity> =
        withContext(ioDispatcher) {
            jetpackDao.getUnsyncedJetpacks(userId)
        }

    /**
     * Inserts a new jetpack into the database.
     *
     * @param jetpackEntity The [JetpackEntity] object to insert.
     */
    override suspend fun insertJetpack(jetpackEntity: JetpackEntity) = withContext(ioDispatcher) {
        jetpackDao.insertJetpack(jetpackEntity)
    }

    /**
     * Inserts or updates a jetpack in the database.
     *
     * @param jetpackEntity The [JetpackEntity] object to upsert.
     */
    override suspend fun upsertJetpack(jetpackEntity: JetpackEntity) = withContext(ioDispatcher) {
        jetpackDao.upsertJetpack(jetpackEntity)
    }

    /**
     * Upserts (insert or update) jetpacks from a remote source.
     *
     * @param remoteJetpacks List of jetpack entities from the remote source to be upserted.
     */
    override suspend fun upsertJetpacks(remoteJetpacks: List<JetpackEntity>) =
        withContext(ioDispatcher) {
            jetpackDao.upsertJetpacks(remoteJetpacks)
        }

    /**
     * Updates an existing jetpack in the database.
     *
     * @param jetpackEntity The [JetpackEntity] object to update.
     */
    override suspend fun updateJetpack(jetpackEntity: JetpackEntity) = withContext(ioDispatcher) {
        jetpackDao.updateJetpack(jetpackEntity)
    }

    /**
     * Marks a jetpack as deleted.
     *
     * @param id The ID of the jetpack to mark as deleted.
     */
    override suspend fun markJetpackAsDeleted(id: String) = withContext(ioDispatcher) {
        jetpackDao.markJetpackAsDeleted(id)
    }

    /**
     * Permanently deletes a jetpack from the database.
     *
     * @param id The ID of the jetpack to delete.
     */
    override suspend fun deleteJetpackPermanently(id: String) = withContext(ioDispatcher) {
        jetpackDao.deleteJetpackPermanently(id)
    }

    /**
     * Marks a jetpack as synced.
     *
     * @param id The ID of the jetpack to mark as synced.
     * @param timestamp The timestamp to set as the last synced time. Defaults to the current system time.
     */
    override suspend fun markAsSynced(id: String, timestamp: Long) = withContext(ioDispatcher) {
        jetpackDao.markAsSynced(id, timestamp)
    }

    /**
     * Gets the newest remote server timestamp this device has already stored, to resume a pull from.
     *
     * @param userId The ID of the user whose jetpacks to check.
     * @return The newest server timestamp in nanoseconds, or 0 if the user has no jetpacks.
     */
    override suspend fun getSyncCursor(userId: String): Long =
        withContext(ioDispatcher) {
            jetpackDao.getSyncCursor(userId) ?: 0
        }
}
