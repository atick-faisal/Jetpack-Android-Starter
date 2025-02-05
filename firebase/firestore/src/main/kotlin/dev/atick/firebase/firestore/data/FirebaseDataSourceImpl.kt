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
import dev.atick.firebase.firestore.models.FirebaseJetpack
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseDataSourceImpl @Inject constructor(
    firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FirebaseDataSource {

    private val database = firestore.collection(FirebaseDataSource.DATABASE_NAME)

    override suspend fun pull(userId: String, lastSynced: Long): List<FirebaseJetpack> {
        return withContext(ioDispatcher) {
            database
                .document(checkAuthentication(userId))
                .collection(FirebaseDataSource.COLLECTION_NAME)
                .whereGreaterThan("lastUpdated", lastSynced)
                .get()
                .await()
                .toObjects(FirebaseJetpack::class.java)
        }
    }

    override suspend fun create(firebaseJetpack: FirebaseJetpack) {
        withContext(ioDispatcher) {
            database
                .document(checkAuthentication(firebaseJetpack.userId))
                .collection(FirebaseDataSource.COLLECTION_NAME)
                .add(firebaseJetpack)
                .await()
        }
    }

    override suspend fun createOrUpdate(firebaseJetpack: FirebaseJetpack) {
        withContext(ioDispatcher) {
            database
                .document(checkAuthentication(firebaseJetpack.userId))
                .collection(FirebaseDataSource.COLLECTION_NAME)
                .document(firebaseJetpack.id)
                .set(firebaseJetpack)
                .await()
        }
    }

    override suspend fun delete(firebaseJetpack: FirebaseJetpack) {
        withContext(ioDispatcher) {
            database
                .document(checkAuthentication(firebaseJetpack.userId))
                .collection(FirebaseDataSource.COLLECTION_NAME)
                .document(firebaseJetpack.id)
                .delete()
                .await()
        }
    }

    private fun checkAuthentication(userId: String?): String {
        return userId ?: throw IllegalStateException("User not authenticated")
    }
}
