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
import java.util.UUID

/**
 * Domain model representing a Jetpack library item in the application's data layer.
 *
 * This is the core domain model used throughout the app for representing Jetpack library entries.
 * It sits between the database layer ([JetpackEntity]) and the remote layer ([FirebaseJetpack]),
 * serving as the single source of truth that the UI layer observes.
 *
 * The model supports offline-first architecture with sync tracking:
 * - [lastUpdated]: Timestamp when the item was modified locally
 * - [lastSynced]: Timestamp of the last successful sync with Firebase
 * - [needsSync]: Flag indicating if local changes need to be pushed to Firebase
 *
 * Mapping extensions are provided to convert between layers:
 * - [toJetpackEntity]: Convert to Room database entity for local storage
 * - [toFirebaseJetpack]: Convert to Firebase document for remote storage
 * - [JetpackEntity.toJetpack]: Convert from Room entity to domain model
 * - [FirebaseJetpack.toJetpackEntity]: Convert from Firebase to Room entity
 *
 * Usage context:
 * - UI layer displays lists and details of Jetpack items via [dev.atick.feature.home.ui.home.HomeViewModel]
 * - Repository returns Flow<List<Jetpack>> as the single source of truth
 * - ViewModel wraps Jetpack data in [dev.atick.feature.home.ui.home.HomeScreenData]
 * - Background sync uses [needsSync] flag to identify items requiring upload
 *
 * @param id Unique identifier (UUID) for the Jetpack item, consistent across all layers.
 * @param name Display name of the Jetpack library (e.g., "Compose", "Room", "Hilt").
 * @param price Numeric value representing the item's price (demo field for CRUD operations).
 * @param lastUpdated Unix timestamp (milliseconds) of the last local modification.
 * @param lastSynced Unix timestamp (milliseconds) of the last successful Firebase sync.
 * @param needsSync Boolean flag indicating if local changes haven't been synced to Firebase.
 * @param formattedDate Human-readable date string derived from [lastUpdated] for UI display.
 *
 * @see JetpackEntity Room database entity representing local storage
 * @see FirebaseJetpack Firestore document representing remote storage
 * @see dev.atick.data.repository.home.HomeRepository Repository providing Jetpack operations
 * @see dev.atick.feature.home.ui.home.HomeViewModel ViewModel consuming Jetpack data
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
        deleted = deleted,
    )
}
