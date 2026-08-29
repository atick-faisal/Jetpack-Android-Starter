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
 * Two clocks appear on this document and they are not interchangeable. [lastUpdated] and
 * [lastSynced] are stamped by whichever device wrote the row, so they are only meaningful for
 * display. [serverUpdatedAt] is assigned by Firestore itself and is the only field ordered
 * consistently across devices, which is why it — and nothing else — drives the pull cursor.
 *
 * @property id Unique identifier, shared with the Room entity and used as the document ID.
 * @property name Display name.
 * @property price Price in the user's currency.
 * @property userId Firebase Auth UID of the owner. Also the parent document in the path above.
 * @property lastUpdated Writing device's clock, in milliseconds, when the row was last edited.
 * @property lastSynced Writing device's clock, in milliseconds, when it last pushed the row.
 * @property deleted Soft delete flag.
 * @see dev.atick.core.room.model.JetpackEntity
 */
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
) {
    // The server's own write time, and deliberately not a constructor parameter. :data builds
    // FirebaseJetpack with default arguments, and a Timestamp in the constructor would put
    // com.google.firebase.Timestamp on :data's compile classpath -- which it does not have, because
    // this module declares the SDK with implementation rather than api. Keeping the property in the
    // body lets :data stay blind to the type and read the value through serverUpdatedAtNanos().
    //
    // Left null on every write: CustomClassMapper substitutes FieldValue.serverTimestamp() for a
    // null @ServerTimestamp property, so the value below is only ever what the server assigned. The
    // @get: use-site is the one the SDK documents for Kotlin; the annotation is read on write only
    // and ignored when reading documents back.
    @get:ServerTimestamp
    var serverUpdatedAt: Timestamp? = null
}

/**
 * [FirebaseJetpack.serverUpdatedAt] as nanoseconds since the epoch, or `0` for a document the server
 * has not stamped yet.
 *
 * 💡 Nanoseconds rather than milliseconds because this value is stored and replayed as a sync
 * cursor. Firestore timestamps carry nanosecond precision, so a cursor truncated to milliseconds
 * sits *below* the timestamp of the newest document it just ingested, and that document is pulled
 * again on every later sync, forever. A [Long] holds nanoseconds since the epoch until the year
 * 2262.
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
