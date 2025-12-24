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

package dev.atick.data.repository.home

import dev.atick.data.model.home.Jetpack
import dev.atick.data.utils.Syncable
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining operations for interacting with the home repository.
 *
 * This repository follows the offline-first pattern where the local database serves as the
 * single source of truth. Network operations update the local database, and UI observes
 * local data via Flow.
 *
 * ## Synchronization
 * This repository implements [Syncable] to support background sync via WorkManager.
 * See [sync] for details on the sync implementation.
 *
 * @see HomeRepositoryImpl Implementation class with network and local data sources
 * @see Syncable For background sync interface
 * @see SyncProgress For sync progress tracking
 */
interface HomeRepository : Syncable {
    /**
     * Retrieves a list of all jetpacks.
     *
     * @return A Flow emitting a list of Jetpack objects.
     * @sample dev.atick.data.repository.home.HomeRepositoryImpl.getJetpacks Offline-first implementation
     */
    fun getJetpacks(): Flow<List<Jetpack>>

    /**
     * Retrieves a specific jetpack by its ID.
     *
     * @param id The unique identifier of the jetpack.
     * @return A Flow emitting the Jetpack object.
     * @sample dev.atick.data.repository.home.HomeRepositoryImpl.getJetpack Single item retrieval from local database
     */
    fun getJetpack(id: String): Flow<Jetpack>

    /**
     * Creates or updates a jetpack in the repository.
     *
     * @param jetpack The Jetpack object to create or update.
     * @return A Result indicating the success or failure of the operation.
     * @sample dev.atick.data.repository.home.HomeRepositoryImpl.createOrUpdateJetpack Local-first write with sync trigger
     */
    suspend fun createOrUpdateJetpack(jetpack: Jetpack): Result<Unit>

    /**
     * Marks a jetpack as deleted in the repository.
     *
     * @param jetpack The Jetpack object to mark as deleted.
     * @return A Result indicating the success or failure of the operation.
     * @sample dev.atick.data.repository.home.HomeRepositoryImpl.markJetpackAsDeleted Soft delete with sync trigger
     */
    suspend fun markJetpackAsDeleted(jetpack: Jetpack): Result<Unit>
}
