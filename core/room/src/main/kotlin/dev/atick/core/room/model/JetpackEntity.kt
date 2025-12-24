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

package dev.atick.core.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room database entity representing a Jetpack item with offline-first sync capabilities.
 *
 * This entity implements an **offline-first** pattern with automatic sync tracking.
 * All local modifications are tracked via sync metadata fields, enabling bidirectional
 * synchronization with a remote data source (e.g., Firestore, REST API).
 *
 * ## Sync Metadata Pattern
 *
 * The entity uses several fields to track synchronization state:
 * - **lastUpdated**: Timestamp of the last local modification (set on every update)
 * - **lastSynced**: Timestamp of the last successful sync with remote
 * - **needsSync**: Flag indicating this entity has pending changes to sync
 * - **deleted**: Soft delete flag (entity is hidden from UI but kept for sync)
 * - **syncAction**: The type of sync operation to perform ([SyncAction])
 *
 * ## Lifecycle States
 *
 * 1. **Created Locally**:
 *    - needsSync = true, syncAction = UPSERT, lastUpdated > lastSynced
 * 2. **Updated Locally**:
 *    - needsSync = true, syncAction = UPSERT, lastUpdated > lastSynced
 * 3. **Deleted Locally** (Soft Delete):
 *    - deleted = true, needsSync = true, syncAction = DELETE
 * 4. **Synced**:
 *    - needsSync = false, syncAction = NONE, lastSynced = current timestamp
 * 5. **Updated from Remote**:
 *    - Upserted with remote data, lastUpdated and lastSynced updated
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Create new entity (needs sync)
 * val newJetpack = JetpackEntity(
 *     name = "Jetpack MK-1",
 *     price = 499.99,
 *     userId = currentUserId,
 *     lastUpdated = System.currentTimeMillis(),
 *     needsSync = true,
 *     syncAction = SyncAction.UPSERT
 * )
 *
 * // Update existing entity
 * val updated = existingJetpack.copy(
 *     price = 399.99,
 *     lastUpdated = System.currentTimeMillis(),
 *     needsSync = true,
 *     syncAction = SyncAction.UPSERT
 * )
 *
 * // Soft delete entity
 * val softDeleted = existingJetpack.copy(
 *     deleted = true,
 *     needsSync = true,
 *     syncAction = SyncAction.DELETE,
 *     lastUpdated = System.currentTimeMillis()
 * )
 *
 * // Mark as synced after successful remote operation
 * val synced = existingJetpack.copy(
 *     needsSync = false,
 *     syncAction = SyncAction.NONE,
 *     lastSynced = System.currentTimeMillis()
 * )
 * ```
 *
 * @property id Unique identifier (UUID). Auto-generated if not provided.
 * @property name Display name of the jetpack. Required field for domain logic.
 * @property price Price of the jetpack in the app's currency. Must be non-negative.
 * @property userId Identifier of the user who owns this jetpack. Used for multi-user filtering.
 * @property lastUpdated Timestamp (milliseconds since epoch) when this entity was last modified locally.
 *                       Updated on every create/update operation. Used to determine if entity needs sync.
 * @property lastSynced Timestamp (milliseconds since epoch) when this entity was last successfully synced
 *                      with the remote data source. Updated after successful sync operations.
 * @property needsSync Flag indicating this entity has pending local changes that need to be synced to remote.
 *                     Set to `true` on create/update/delete, reset to `false` after successful sync.
 * @property deleted Soft delete flag. When `true`, the entity is hidden from normal queries but kept in
 *                   the database for synchronization. Allows deletion to be synced with remote before
 *                   permanent removal.
 * @property syncAction The type of synchronization action to perform during the next sync operation.
 *                      See [SyncAction] for possible values.
 *
 * @see JetpackDao for database operations
 * @see LocalDataSource for data access abstraction
 * @see SyncAction for sync operation types
 */
@Entity(tableName = "jetpacks")
data class JetpackEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double,

    // User metadata
    val userId: String = String(),

    // Sync metadata
    val lastUpdated: Long = 0,
    val lastSynced: Long = 0,
    val needsSync: Boolean = false,
    val deleted: Boolean = false,
    val syncAction: SyncAction = SyncAction.NONE,
)

/**
 * Defines the type of synchronization action to perform on an entity.
 *
 * This enum is used to track what operation needs to be performed during the next
 * sync operation with a remote data source. It enables fine-grained control over
 * sync behavior and supports conflict resolution strategies.
 *
 * ## Usage
 *
 * Set the appropriate action when modifying entities:
 * ```kotlin
 * // After creating or updating
 * entity.copy(syncAction = SyncAction.UPSERT, needsSync = true)
 *
 * // After soft deleting
 * entity.copy(syncAction = SyncAction.DELETE, needsSync = true, deleted = true)
 *
 * // After successful sync
 * entity.copy(syncAction = SyncAction.NONE, needsSync = false)
 * ```
 *
 * ## Sync Worker Implementation
 *
 * ```kotlin
 * when (entity.syncAction) {
 *     SyncAction.UPSERT -> remoteDataSource.upsert(entity.toRemote())
 *     SyncAction.DELETE -> remoteDataSource.delete(entity.id)
 *     SyncAction.NONE -> {} // Already synced, skip
 * }
 * ```
 */
enum class SyncAction {
    /**
     * No synchronization action needed.
     *
     * This entity is already in sync with the remote data source,
     * or synchronization is not required.
     */
    NONE,

    /**
     * Insert or update this entity on the remote data source.
     *
     * Used when:
     * - Entity was created locally and needs to be pushed to remote
     * - Entity was updated locally and changes need to be synced
     * - Remote should be updated with the latest local version
     */
    UPSERT,

    /**
     * Delete this entity from the remote data source.
     *
     * Used when:
     * - Entity was soft-deleted locally (deleted = true)
     * - Remote copy should be removed
     * - After successful remote deletion, entity can be permanently removed locally
     */
    DELETE,
}
