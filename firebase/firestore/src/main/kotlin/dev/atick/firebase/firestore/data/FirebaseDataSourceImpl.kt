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

import com.google.firebase.firestore.FirebaseFirestore
import dev.atick.core.di.IoDispatcher
import dev.atick.firebase.firestore.model.FirebaseJetpack
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of [FirebaseDataSource].
 *
 * @param firestore [FirebaseFirestore].
 * @param ioDispatcher [CoroutineDispatcher].
 */
internal class FirebaseDataSourceImpl @Inject constructor(
    firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FirebaseDataSource {

    /**
     * Firestore database reference.
     */
    private val database = firestore.collection(FirebaseDataSource.DATABASE_NAME)

    /**
     * Pulls a list of [FirebaseJetpack] objects that have been updated since the last sync.
     *
     * @param userId The unique identifier of the user.
     * @param lastSynced The timestamp of the last sync.
     * @return A list of [FirebaseJetpack] objects.
     */
    override suspend fun pullJetpacks(userId: String, lastSynced: Long): List<FirebaseJetpack> {
        return withContext(ioDispatcher) {
            database
                .document(checkAuthentication(userId))
                .collection(FirebaseDataSource.JETPACK_COLLECTION_NAME)
                .whereGreaterThan("lastUpdated", lastSynced)
                .get()
                .await()
                .toObjects(FirebaseJetpack::class.java)
        }
    }

    /**
     * Creates a new [FirebaseJetpack] object in the database.
     *
     * @param firebaseJetpack The [FirebaseJetpack] object to create.
     */
    override suspend fun createJetpack(firebaseJetpack: FirebaseJetpack) {
        withContext(ioDispatcher) {
            database
                .document(checkAuthentication(firebaseJetpack.userId))
                .collection(FirebaseDataSource.JETPACK_COLLECTION_NAME)
                .add(firebaseJetpack)
                .await()
        }
    }

    /**
     * Creates or updates a [FirebaseJetpack] object in the database.
     *
     * @param firebaseJetpack The [FirebaseJetpack] object to create or update.
     */
    override suspend fun createOrUpdateJetpack(firebaseJetpack: FirebaseJetpack) {
        withContext(ioDispatcher) {
            database
                .document(checkAuthentication(firebaseJetpack.userId))
                .collection(FirebaseDataSource.JETPACK_COLLECTION_NAME)
                .document(firebaseJetpack.id)
                .set(firebaseJetpack)
                .await()
        }
    }

    /**
     * Deletes a [FirebaseJetpack] object from the database.
     *
     * @param firebaseJetpack The [FirebaseJetpack] object to delete.
     */
    override suspend fun deleteJetpack(firebaseJetpack: FirebaseJetpack) {
        withContext(ioDispatcher) {
            database
                .document(checkAuthentication(firebaseJetpack.userId))
                .collection(FirebaseDataSource.JETPACK_COLLECTION_NAME)
                .document(firebaseJetpack.id)
                .delete()
                .await()
        }
    }

    /**
     * Checks if the user is authenticated.
     *
     * @param userId The unique identifier of the user.
     * @return The user ID.
     */
    private fun checkAuthentication(userId: String?): String {
        return userId ?: throw IllegalStateException("User not authenticated")
    }
}
