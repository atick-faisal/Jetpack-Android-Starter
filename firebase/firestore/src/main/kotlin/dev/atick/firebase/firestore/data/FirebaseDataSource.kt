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

import dev.atick.firebase.firestore.model.FirebaseJetpack

/**
 * Firestore operations under `/dev.atick.jetpack/{userId}/jetpacks/{documentId}`.
 *
 * The repository owns Room persistence, pending metadata, and push/pull ordering. These calls
 * propagate SDK failures; they do not resolve conflicts or acknowledge local rows.
 *
 * @see FirebaseJetpack
 */
interface FirebaseDataSource {
    companion object {
        /**
         * The root collection name; this does not select a named Firestore database.
         */
        const val DATABASE_NAME = "dev.atick.jetpack"

        /**
         * The subcollection under each user document: `/dev.atick.jetpack/{userId}/jetpacks`.
         */
        const val JETPACK_COLLECTION_NAME = "jetpacks"
    }

    /**
     * Queries documents whose [FirebaseJetpack.serverUpdatedAt] is greater than [syncedAfterNanos].
     *
     * The default SDK source may return cached results. Documents missing serverUpdatedAt are excluded,
     * even with a zero cursor. Hard-deleted documents are absent; this is not a deletion feed.
     *
     * @param userId Owner document ID, expected to be the Firebase Auth UID.
     * @param syncedAfterNanos Stored server write time in epoch nanoseconds; zero starts at the epoch.
     * @return Matching documents, without snapshot cache/pending-write metadata.
     * @throws com.google.firebase.firestore.FirebaseFirestoreException if the SDK query fails
     */
    suspend fun pullJetpacks(userId: String, syncedAfterNanos: Long): List<FirebaseJetpack>

    /**
     * Adds a document with a Firestore-generated ID, independent of [FirebaseJetpack.id].
     *
     * Repository sync uses [createOrUpdateJetpack] to address documents by the model ID instead.
     *
     * @param firebaseJetpack Content to add under its userId.
     * @throws com.google.firebase.firestore.FirebaseFirestoreException if the SDK write fails
     */
    suspend fun createJetpack(firebaseJetpack: FirebaseJetpack)

    /**
     * Creates or replaces the document identified by the model's userId and id using set(), without merge.
     *
     * Fresh upload mappings leave serverUpdatedAt null so Firestore assigns a server timestamp.
     *
     * @param firebaseJetpack Full document content; fields omitted from an existing document are removed.
     * @throws com.google.firebase.firestore.FirebaseFirestoreException if the SDK write fails
     */
    suspend fun createOrUpdateJetpack(firebaseJetpack: FirebaseJetpack)

    /**
     * Hard-deletes the document identified by the model's userId and id.
     *
     * The repository uses this for pending local soft deletes, then acknowledges the retained local row.
     * No remote tombstone is written, so incremental pulls cannot remove another device's existing row.
     *
     * @param firebaseJetpack Identifies the owner and document; other fields are not used.
     * @throws com.google.firebase.firestore.FirebaseFirestoreException if the SDK delete fails
     */
    suspend fun deleteJetpack(firebaseJetpack: FirebaseJetpack)
}
