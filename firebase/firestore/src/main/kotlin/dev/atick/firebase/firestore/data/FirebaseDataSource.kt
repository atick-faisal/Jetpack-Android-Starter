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

package dev.atick.firebase.firestore.data

import dev.atick.firebase.firestore.models.FirebaseJetpack

/**
 * Interface for Firebase data source operations.
 */
interface FirebaseDataSource {
    companion object {
        /**
         * The name of the database.
         */
        const val DATABASE_NAME = "dev.atick.jetpack"

        /**
         * The name of the collection.
         */
        const val COLLECTION_NAME = "jetpacks"
    }

    /**
     * Pulls a list of FirebaseJetpack objects that have been updated since the last sync.
     *
     * @param lastSynced The timestamp of the last sync.
     * @return A list of FirebaseJetpack objects.
     */
    suspend fun pull(lastSynced: Long): List<FirebaseJetpack>

    /**
     * Creates a new FirebaseJetpack object in the database.
     *
     * @param firebaseJetpack The FirebaseJetpack object to create.
     */
    suspend fun create(firebaseJetpack: FirebaseJetpack)

    /**
     * Updates an existing FirebaseJetpack object in the database.
     *
     * @param firebaseJetpack The FirebaseJetpack object to update.
     */
    suspend fun update(firebaseJetpack: FirebaseJetpack)

    /**
     * Deletes a FirebaseJetpack object from the database.
     *
     * @param firebaseJetpack The FirebaseJetpack object to delete.
     */
    suspend fun delete(firebaseJetpack: FirebaseJetpack)
}
