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

package dev.atick.firebase.firestore.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a Jetpack item stored in Firebase Firestore with sync metadata.
 *
 * This model demonstrates the offline-first sync pattern used in this template. It includes
 * metadata fields ([lastUpdated], [lastSynced], [deleted]) that enable bidirectional
 * synchronization between local Room database and remote Firestore.
 *
 * ## Sync Pattern
 * - **Pull**: Query Firestore for items where `lastUpdated > lastSyncTimestamp`
 * - **Push**: Upload local items to Firestore based on their [SyncAction]
 * - **Soft Delete**: Use [deleted] flag instead of hard deletion for sync safety
 *
 * ## Field Initialization
 * All properties must have default values to work with Firestore's automatic serialization.
 * This is a Firestore requirement for data classes used as document models.
 *
 * ## Usage Example
 * ```kotlin
 * // Create new item
 * val jetpack = FirebaseJetpack(
 *     name = "Lightning Bolt",
 *     price = 99.99,
 *     userId = currentUser.id,
 *     lastUpdated = System.currentTimeMillis()
 * )
 *
 * // Mark for deletion (soft delete)
 * val deletedJetpack = jetpack.copy(
 *     deleted = true,
 *     lastUpdated = System.currentTimeMillis()
 * )
 * ```
 *
 * ## Conversion to/from Room Entity
 * ```kotlin
 * // Extension in repository layer
 * fun FirebaseJetpack.toEntity() = JetpackEntity(
 *     id = id,
 *     name = name,
 *     price = price,
 *     userId = userId,
 *     lastUpdated = lastUpdated,
 *     lastSynced = lastSynced,
 *     deleted = deleted,
 *     syncAction = SyncAction.SYNCED
 * )
 *
 * fun JetpackEntity.toFirebase() = FirebaseJetpack(
 *     id = id,
 *     name = name,
 *     price = price,
 *     userId = userId,
 *     lastUpdated = lastUpdated,
 *     lastSynced = lastSynced,
 *     deleted = deleted
 * )
 * ```
 *
 * @property id Unique identifier of the Jetpack (UUID). This is the primary key used across
 *              both Firestore and Room databases.
 * @property name Jetpack's display name.
 * @property price Jetpack's price in the user's currency.
 * @property userId User's unique identifier (Firebase Auth UID). Used for user-specific queries
 *                  and Firestore security rules.
 * @property lastUpdated Timestamp in milliseconds (epoch time) of the last modification to this item.
 *                       Updated whenever the item is created, modified, or soft-deleted. Used for
 *                       incremental sync to pull only changed items.
 * @property lastSynced Timestamp in milliseconds (epoch time) of the last successful sync with Firestore.
 *                      Updated after successful push/pull operations.
 * @property deleted Soft delete flag. When true, the item is considered deleted but remains in
 *                   both Firestore and Room for sync purposes. Hard deletion happens after
 *                   confirming all clients have synchronized the deletion.
 * @see dev.atick.core.room.model.JetpackEntity
 * @see dev.atick.core.room.model.SyncAction
 */
@Serializable
data class FirebaseJetpack(
    // Every property has to be initialized for Firestore serialization.
    // https://stackoverflow.com/a/67298049/12737399
    val id: String = UUID.randomUUID().toString(),
    val name: String = String(),
    val price: Double = 0.0,
    val userId: String = String(),
    val lastUpdated: Long = 0L,
    val lastSynced: Long = 0L,
    val deleted: Boolean = false,
)
