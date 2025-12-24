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
 * Interface for Firebase Firestore data source operations.
 *
 * This interface defines CRUD operations for syncing data between the local Room database
 * and Firebase Firestore. It implements an offline-first sync pattern where:
 * - Local Room database is the source of truth for the UI
 * - Firestore serves as the remote backup and sync hub
 * - Changes are synchronized bidirectionally between local and remote
 *
 * ## Sync Strategy
 * **Pull (Download from Firestore)**:
 * - Query items where `lastUpdated > lastSyncTimestamp`
 * - Merge remote changes into local database
 * - Handle conflicts (typically last-write-wins)
 *
 * **Push (Upload to Firestore)**:
 * - Query local items with pending sync actions (CREATE, UPDATE, DELETE)
 * - Upload changes to Firestore
 * - Mark items as synced after successful upload
 *
 * ## Firestore Structure
 * ```
 * /dev.atick.jetpack/
 *   /jetpacks/
 *     /{userId}/
 *       /{jetpackId}/
 *         - id: String
 *         - name: String
 *         - price: Double
 *         - userId: String
 *         - lastUpdated: Long
 *         - lastSynced: Long
 *         - deleted: Boolean
 * ```
 *
 * ## Usage in Repository
 * ```kotlin
 * class HomeRepository @Inject constructor(
 *     private val localDataSource: LocalDataSource,
 *     private val firebaseDataSource: FirebaseDataSource,
 *     private val authDataSource: AuthDataSource
 * ) {
 *     // Pull remote changes
 *     suspend fun pullData(): Result<Unit> = suspendRunCatching {
 *         val userId = authDataSource.getCurrentUser()?.id ?: return@suspendRunCatching
 *         val lastSync = localDataSource.getLastSyncTimestamp()
 *         val remoteItems = firebaseDataSource.pullJetpacks(userId, lastSync)
 *
 *         // Merge into local database
 *         remoteItems.forEach { remote ->
 *             localDataSource.upsertJetpack(remote.toEntity())
 *         }
 *     }
 *
 *     // Push local changes
 *     suspend fun pushData(): Result<Unit> = suspendRunCatching {
 *         val pendingItems = localDataSource.getPendingSyncItems()
 *
 *         pendingItems.forEach { local ->
 *             when (local.syncAction) {
 *                 SyncAction.CREATE, SyncAction.UPDATE ->
 *                     firebaseDataSource.createOrUpdateJetpack(local.toFirebase())
 *                 SyncAction.DELETE ->
 *                     firebaseDataSource.deleteJetpack(local.toFirebase())
 *                 SyncAction.SYNCED -> { /* Skip */ }
 *             }
 *             localDataSource.markAsSynced(local.id)
 *         }
 *     }
 * }
 * ```
 *
 * ## Thread Safety
 * All operations are suspend functions and should be called from appropriate coroutine contexts.
 * The implementation uses Firebase's built-in thread handling which is safe for concurrent access.
 *
 * ## Error Handling
 * Operations can throw:
 * - `FirebaseFirestoreException` - Firestore-specific errors
 * - `FirebaseNetworkException` - Network connectivity issues
 * - `SecurityException` - Firestore security rules violations
 *
 * @see FirebaseJetpack
 * @see dev.atick.core.room.data.LocalDataSource
 */
interface FirebaseDataSource {
    companion object {
        /**
         * The name of the Firestore database.
         *
         * This is used as the root collection name for all app data in Firestore.
         */
        const val DATABASE_NAME = "dev.atick.jetpack"

        /**
         * The name of the jetpacks collection within the database.
         *
         * Full Firestore path: `/dev.atick.jetpack/jetpacks/{userId}/{jetpackId}`
         */
        const val JETPACK_COLLECTION_NAME = "jetpacks"
    }

    /**
     * Pulls (downloads) a list of Jetpack items that have been updated since the last sync.
     *
     * This method implements incremental sync by querying only items that changed after
     * [lastSynced]. This minimizes network usage and improves sync performance.
     *
     * ## Query Logic
     * ```
     * Firestore.collection("dev.atick.jetpack/jetpacks/{userId}")
     *     .where("lastUpdated", ">", lastSynced)
     *     .get()
     * ```
     *
     * ## Soft Deletes
     * Deleted items (where `deleted = true`) are included in the results. This allows
     * the local database to properly handle deletions that occurred on other devices.
     *
     * ## Performance
     * - Returns only changed items (not the entire dataset)
     * - Firestore indexes should be configured for the `lastUpdated` field
     * - Results are ordered by `lastUpdated` ascending
     *
     * ## Example
     * ```kotlin
     * suspend fun syncPull() {
     *     val userId = getCurrentUserId()
     *     val lastSync = preferences.getLastSyncTimestamp()
     *     val updates = firebaseDataSource.pullJetpacks(userId, lastSync)
     *
     *     updates.forEach { remote ->
     *         localDataSource.upsertJetpack(remote.toEntity())
     *     }
     *
     *     preferences.setLastSyncTimestamp(System.currentTimeMillis())
     * }
     * ```
     *
     * @param userId The unique identifier of the user (Firebase Auth UID). Only items
     *               belonging to this user are returned.
     * @param lastSynced The timestamp (milliseconds) of the last successful sync. Items
     *                   with `lastUpdated > lastSynced` are returned. Use 0L for the
     *                   initial sync to fetch all items.
     * @return A list of [FirebaseJetpack] objects that have been updated since [lastSynced].
     *         Returns an empty list if no updates are available.
     * @throws FirebaseFirestoreException if the query fails
     * @throws FirebaseNetworkException if network is unavailable
     */
    suspend fun pullJetpacks(userId: String, lastSynced: Long): List<FirebaseJetpack>

    /**
     * Creates a new Jetpack item in Firestore.
     *
     * This method adds a new document to the Firestore collection. If a document with
     * the same ID already exists, this operation will fail with a conflict error.
     *
     * ## Document Path
     * `/dev.atick.jetpack/jetpacks/{userId}/{jetpackId}`
     *
     * ## When to Use
     * Use this when you're certain the item doesn't exist in Firestore. For most sync
     * operations, prefer [createOrUpdateJetpack] which handles both cases.
     *
     * ## Example
     * ```kotlin
     * suspend fun uploadNewItem(item: JetpackEntity) {
     *     val firebaseItem = item.toFirebase().copy(
     *         userId = getCurrentUserId(),
     *         lastUpdated = System.currentTimeMillis()
     *     )
     *     firebaseDataSource.createJetpack(firebaseItem)
     * }
     * ```
     *
     * @param firebaseJetpack The [FirebaseJetpack] object to create. Must have a unique
     *                        ID and all required fields populated.
     * @throws FirebaseFirestoreException if creation fails or document already exists
     * @throws FirebaseNetworkException if network is unavailable
     */
    suspend fun createJetpack(firebaseJetpack: FirebaseJetpack)

    /**
     * Creates or updates a Jetpack item in Firestore (upsert operation).
     *
     * This method uses Firestore's `set()` operation to either create a new document
     * or replace an existing one. This is the preferred method for sync operations as
     * it handles both new items and updates uniformly.
     *
     * ## Behavior
     * - If document doesn't exist: Creates new document
     * - If document exists: Completely replaces the document with new data
     * - **Warning**: This performs a full replace, not a merge. All fields are overwritten.
     *
     * ## Document Path
     * `/dev.atick.jetpack/jetpacks/{userId}/{jetpackId}`
     *
     * ## Usage in Sync
     * ```kotlin
     * suspend fun syncPush() {
     *     val pendingItems = localDataSource.getItemsWithSyncAction(
     *         SyncAction.CREATE, SyncAction.UPDATE
     *     )
     *
     *     pendingItems.forEach { local ->
     *         val firebaseItem = local.toFirebase().copy(
     *             lastUpdated = System.currentTimeMillis()
     *         )
     *         firebaseDataSource.createOrUpdateJetpack(firebaseItem)
     *         localDataSource.markAsSynced(local.id)
     *     }
     * }
     * ```
     *
     * @param firebaseJetpack The [FirebaseJetpack] object to create or update. Should have
     *                        `lastUpdated` set to the current timestamp.
     * @throws FirebaseFirestoreException if the operation fails
     * @throws FirebaseNetworkException if network is unavailable
     * @throws SecurityException if Firestore security rules deny the operation
     */
    suspend fun createOrUpdateJetpack(firebaseJetpack: FirebaseJetpack)

    /**
     * Deletes a Jetpack item from Firestore (hard delete).
     *
     * This method permanently removes the document from Firestore. **Warning**: This is
     * a destructive operation and cannot be undone.
     *
     * ## Soft Delete vs Hard Delete
     * In practice, this template uses **soft deletes** by setting `deleted = true` via
     * [createOrUpdateJetpack]. Hard deletion using this method should only be used for:
     * - Final cleanup after all devices have synchronized the soft delete
     * - Administrative operations
     * - GDPR/data deletion requests
     *
     * ## Recommended Pattern (Soft Delete)
     * ```kotlin
     * // Instead of calling deleteJetpack():
     * val softDeleted = item.copy(
     *     deleted = true,
     *     lastUpdated = System.currentTimeMillis()
     * )
     * firebaseDataSource.createOrUpdateJetpack(softDeleted)
     * ```
     *
     * ## Hard Delete Usage
     * ```kotlin
     * suspend fun permanentlyDelete(item: JetpackEntity) {
     *     // First, ensure all devices have synced the soft delete
     *     if (allDevicesSynced(item.id)) {
     *         firebaseDataSource.deleteJetpack(item.toFirebase())
     *         localDataSource.hardDelete(item.id)
     *     }
     * }
     * ```
     *
     * ## Document Path
     * `/dev.atick.jetpack/jetpacks/{userId}/{jetpackId}`
     *
     * @param firebaseJetpack The [FirebaseJetpack] object to delete. Only the ID field
     *                        is used to identify the document to delete.
     * @throws FirebaseFirestoreException if deletion fails
     * @throws FirebaseNetworkException if network is unavailable
     * @throws SecurityException if Firestore security rules deny deletion
     */
    suspend fun deleteJetpack(firebaseJetpack: FirebaseJetpack)
}
