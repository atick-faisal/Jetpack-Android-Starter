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
 */
interface HomeRepository : Syncable {
    /**
     * Retrieves a list of all jetpacks.
     *
     * @return A Flow emitting a list of Jetpack objects.
     */
    fun getJetpacks(): Flow<List<Jetpack>>

    /**
     * Retrieves a specific jetpack by its ID.
     *
     * @param id The unique identifier of the jetpack.
     * @return A Flow emitting the Jetpack object.
     */
    fun getJetpack(id: String): Flow<Jetpack>

    /**
     * Creates or updates a jetpack in the repository.
     *
     * @param jetpack The Jetpack object to create or update.
     * @return A Result indicating the success or failure of the operation.
     */
    suspend fun createOrUpdateJetpack(jetpack: Jetpack): Result<Unit>

    /**
     * Marks a jetpack as deleted in the repository.
     *
     * @param jetpack The Jetpack object to mark as deleted.
     * @return A Result indicating the success or failure of the operation.
     */
    suspend fun markJetpackAsDeleted(jetpack: Jetpack): Result<Unit>
}
