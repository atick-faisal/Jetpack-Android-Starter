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

package dev.atick.data.model.home

import dev.atick.core.extensions.asFormattedDateTime
import dev.atick.core.room.model.JetpackEntity
import dev.atick.firebase.firestore.model.FirebaseJetpack
import dev.atick.firebase.firestore.model.serverUpdatedAtNanos
import java.util.UUID

/**
 * Home item exposed by the repository after mapping Room data.
 *
 * Room supplies observed content; this model is not a separate persistence store.
 *
 * @param id Item ID shared with local and remote storage.
 * @param name Display name.
 * @param price Numeric price.
 * @param lastUpdated Edit time in epoch milliseconds, carried from the writing device.
 * @param lastSynced Epoch milliseconds carried from local acknowledgement or remote mapping.
 *     It is not an acknowledgement of the current form's persistence.
 * @param needsSync Local pending flag; the DAO also compares edit and acknowledgement times.
 * @param formattedDate Display text derived from [lastUpdated].
 *
 * @see JetpackEntity
 * @see FirebaseJetpack
 */
data class Jetpack(
    val id: String = UUID.randomUUID().toString(),
    val name: String = String(),
    val price: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val lastSynced: Long = 0L,
    val needsSync: Boolean = true,
    val formattedDate: String = lastUpdated.asFormattedDateTime(),
)

/**
 * Extension function to map a [JetpackEntity] to a [Jetpack].
 *
 * @return The mapped [Jetpack].
 */
fun JetpackEntity.toJetpack(): Jetpack {
    return Jetpack(
        id = id,
        name = name,
        price = price,
        lastUpdated = lastUpdated,
        lastSynced = lastSynced,
        needsSync = needsSync,
        formattedDate = lastUpdated.asFormattedDateTime(),
    )
}

/**
 * Extension function to map a list of [JetpackEntity] to a list of [Jetpack].
 *
 * @return The mapped list of [Jetpack].
 */
fun List<JetpackEntity>.mapToJetpacks(): List<Jetpack> {
    return map(JetpackEntity::toJetpack)
}

/**
 * Extension function to map a [Jetpack] to a [JetpackEntity].
 *
 * @return The mapped [JetpackEntity].
 */
fun Jetpack.toJetpackEntity(): JetpackEntity {
    return JetpackEntity(
        id = id,
        name = name,
        price = price,
        lastUpdated = lastUpdated,
        lastSynced = lastSynced,
    )
}

/**
 * Extension function to map a [Jetpack] to a [FirebaseJetpack].
 *
 * @return The mapped [FirebaseJetpack].
 */
fun Jetpack.toFirebaseJetpack(): FirebaseJetpack {
    return FirebaseJetpack(
        id = id,
        name = name,
        price = price,
        lastUpdated = lastUpdated,
        lastSynced = lastSynced,
    )
}

/**
 * Extension function to map a [JetpackEntity] to a [FirebaseJetpack].
 *
 * Leaves serverUpdatedAt null so Firestore assigns a server timestamp when writing.
 *
 * @return The mapped [FirebaseJetpack].
 */
fun JetpackEntity.toFirebaseJetpack(): FirebaseJetpack {
    return FirebaseJetpack(
        id = id,
        name = name,
        price = price,
        userId = userId,
        lastUpdated = lastUpdated,
        lastSynced = lastSynced,
        deleted = deleted,
    )
}

/**
 * Extension function to map a [FirebaseJetpack] to a [JetpackEntity].
 *
 * Copies the server write timestamp into Room for [dev.atick.core.room.data.JetpackDao.getSyncCursor].
 * Pending flags use entity defaults. Pull upserts can overwrite pending local content; an upload
 * echo is not guaranteed to leave that content unchanged.
 *
 * @return The mapped [JetpackEntity].
 */
fun FirebaseJetpack.toJetpackEntity(): JetpackEntity {
    return JetpackEntity(
        id = id,
        name = name,
        price = price,
        userId = userId,
        lastUpdated = lastUpdated,
        lastSynced = lastSynced,
        serverUpdatedAtNanos = serverUpdatedAtNanos(),
        deleted = deleted,
    )
}
