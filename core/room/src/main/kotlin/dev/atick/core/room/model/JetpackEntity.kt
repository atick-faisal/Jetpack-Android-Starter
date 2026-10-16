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
 * Room row containing Home content and local sync metadata.
 *
 * The repository sets pending metadata on local writes; entity construction alone does not
 * schedule work. Pulled rows carry remote content/timestamps and default to no pending action.
 * Acknowledgement clears pending flags by ID, without a version check. Deleted rows remain
 * locally after remote acknowledgement; no automatic cleanup is implemented.
 *
 * @property id Primary key shared with the remote document.
 * @property name Display name.
 * @property price Numeric price; the entity itself does not enforce a range.
 * @property userId Owner used by user-scoped list, pending, and cursor queries.
 * @property lastUpdated Edit time in epoch milliseconds from the writing device. Used for list
 *     ordering and comparison with [lastSynced] when selecting pending rows.
 * @property lastSynced Epoch milliseconds set during upload and local acknowledgement, and also
 *     carried from remote mappings. Not a guarantee that the current content was uploaded.
 * @property serverUpdatedAtNanos Server write time in epoch nanoseconds copied from a pull, or zero
 *     before such a timestamp is stored. Used by the timestamp-only pull cursor.
 * @property needsSync Explicit pending-change flag used alongside the timestamp comparison.
 * @property deleted Soft-delete flag excluding the row from the normal list query.
 * @property syncAction Remote operation selected for a pending row.
 *
 * @see dev.atick.core.room.data.JetpackDao
 * @see dev.atick.core.room.data.LocalDataSource
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
    val serverUpdatedAtNanos: Long = 0,
    val needsSync: Boolean = false,
    val deleted: Boolean = false,
    val syncAction: SyncAction = SyncAction.NONE,
)

/**
 * Remote operation selected by the repository when pushing a pending row.
 *
 * This value does not implement conflict resolution or schedule a pass.
 */
enum class SyncAction {
    /**
     * No remote operation in the push branch. The repository still acknowledges the row.
     */
    NONE,

    /**
     * Create or replace the remote document with the pending row's mapped content.
     */
    UPSERT,

    /**
     * Hard-delete the remote document. The local soft-deleted row is retained.
     */
    DELETE,
}
