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

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

/**
 * A Jetpack item as it is stored in Firestore, at
 * `/dev.atick.jetpack/{userId}/jetpacks/{jetpackId}`.
 *
 * [FirebaseJetpack.lastUpdated] and [FirebaseJetpack.lastSynced] carry device-clock milliseconds into Room, where they affect
 * list ordering and pending-row selection. [serverUpdatedAt] carries the server write timestamp
 * used by the pull cursor; it is independent of those device clocks.
 *
 * @property id Unique identifier, shared with the Room entity and used as the document ID.
 * @property name Display name.
 * @property price Price in the user's currency.
 * @property userId Firebase Auth UID of the owner. Also the parent document in the path above.
 * @property lastUpdated Writing device's clock, in milliseconds, when the row was last edited.
 * @property lastSynced Device-clock milliseconds set on upload; also mapped back into Room.
 * @property deleted Soft delete flag.
 */
data class FirebaseJetpack(
    // Constructor defaults supply the no-argument constructor used when mapping Firestore documents.
    val id: String = UUID.randomUUID().toString(),
    val name: String = String(),
    val price: Double = 0.0,
    val userId: String = String(),
    val lastUpdated: Long = 0L,
    val lastSynced: Long = 0L,
    val deleted: Boolean = false,
) {
    // Keep Timestamp out of the constructor: :data calls it without the SDK on its compile classpath.
    // Fresh upload mappings leave this null; @ServerTimestamp then requests a server-assigned value.
    // :data reads pulled values through serverUpdatedAtNanos(), without naming Timestamp.
    @get:ServerTimestamp
    var serverUpdatedAt: Timestamp? = null
}

/**
 * [FirebaseJetpack.serverUpdatedAt] as nanoseconds since the epoch, or `0` for a document the server
 * has not stamped yet.
 *
 * Preserves sub-millisecond precision when storing and rebuilding the pull cursor. A signed Long
 * can represent epoch nanoseconds only within its range (approximately years 1677–2262).
 *
 * @return Nanoseconds since the epoch.
 */
fun FirebaseJetpack.serverUpdatedAtNanos(): Long {
    val timestamp = serverUpdatedAt ?: return 0L
    return timestamp.seconds * NANOS_PER_SECOND + timestamp.nanoseconds
}

/**
 * Converts nanoseconds since the epoch back into the [Timestamp] a Firestore query expects.
 *
 * @return The equivalent [Timestamp].
 */
internal fun Long.asFirestoreTimestamp(): Timestamp {
    return Timestamp(
        floorDiv(NANOS_PER_SECOND),
        mod(NANOS_PER_SECOND).toInt(),
    )
}

private const val NANOS_PER_SECOND = 1_000_000_000L
