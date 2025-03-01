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

package dev.atick.data.repository.home

import dev.atick.core.preferences.data.UserPreferencesDataSource
import dev.atick.core.room.data.LocalDataSource
import dev.atick.core.room.model.SyncAction
import dev.atick.core.utils.suspendRunCatching
import dev.atick.data.model.home.Jetpack
import dev.atick.data.model.home.mapToJetpacks
import dev.atick.data.model.home.toFirebaseJetpack
import dev.atick.data.model.home.toJetpack
import dev.atick.data.model.home.toJetpackEntity
import dev.atick.data.utils.SyncManager
import dev.atick.data.utils.SyncProgress
import dev.atick.firebase.firestore.data.FirebaseDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of [HomeRepository].
 *
 * @param localDataSource [LocalDataSource].
 * @param preferencesDataSource [UserPreferencesDataSource].
 * @param firebaseDataSource [FirebaseDataSource].
 * @param syncManager [SyncManager].
 */
internal class HomeRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val preferencesDataSource: UserPreferencesDataSource,
    private val firebaseDataSource: FirebaseDataSource,
    private val syncManager: SyncManager,
) : HomeRepository {

    // TODO: Implement userId caching that respects sign out

    /**
     * Get all jetpacks.
     *
     * @return A [Flow] of a list of [Jetpack].
     */
    override fun getJetpacks(): Flow<List<Jetpack>> {
        // Request a sync when fetching jetpacks
        // TODO: This should be done in a more efficient way
        syncManager.requestSync()

        return flow {
            val userId = preferencesDataSource.getUserIdOrThrow()
            val jetpacks = localDataSource.getJetpacks(userId).map { it.mapToJetpacks() }
            emitAll(jetpacks)
        }
    }

    /**
     * Get a jetpack by its ID.
     *
     * @param id The ID of the jetpack.
     * @return A [Flow] of a [Jetpack].
     */
    override fun getJetpack(id: String): Flow<Jetpack> {
        return localDataSource.getJetpack(id).map { it.toJetpack() }
    }

    /**
     * Create or update a jetpack.
     *
     * @param jetpack The jetpack to create or update.
     * @return A [Result] indicating the success or failure of the operation.
     */
    override suspend fun createOrUpdateJetpack(jetpack: Jetpack): Result<Unit> {
        return suspendRunCatching {
            val userId = preferencesDataSource.getUserIdOrThrow()
            localDataSource.upsertJetpack(
                jetpack
                    .toJetpackEntity()
                    .copy(
                        userId = userId,
                        lastUpdated = System.currentTimeMillis(),
                        needsSync = true,
                        syncAction = SyncAction.UPSERT,
                    ),
            )
            syncManager.requestSync()
        }
    }

    /**
     * Mark a jetpack as deleted.
     *
     * @param jetpack The jetpack to mark as deleted.
     * @return A [Result] indicating the success or failure of the operation.
     */
    override suspend fun markJetpackAsDeleted(jetpack: Jetpack): Result<Unit> {
        return suspendRunCatching {
            localDataSource.markJetpackAsDeleted(jetpack.id)
            syncManager.requestSync()
        }
    }

    /**
     * Sync jetpacks with the remote data source.
     *
     * @return A [Flow] of [SyncProgress].
     */
    override suspend fun sync(): Flow<SyncProgress> {
        return flow {
            val userId = preferencesDataSource.getUserIdOrThrow()

            // Unsynced jetpacks
            val unsyncedJetpacks = localDataSource.getUnsyncedJetpacks(userId)
            val totalUnsyncedJetpacks = unsyncedJetpacks.size
            Timber.d("Syncing $totalUnsyncedJetpacks unsynced jetpacks")

            // Push updates to remote
            unsyncedJetpacks.forEachIndexed { index, unsyncedJetpack ->
                when (unsyncedJetpack.syncAction) {
                    SyncAction.UPSERT -> {
                        Timber.d("Syncing create/update jetpack: ${unsyncedJetpack.id}")
                        firebaseDataSource.createOrUpdateJetpack(
                            unsyncedJetpack
                                .toFirebaseJetpack()
                                .copy(
                                    lastSynced = System.currentTimeMillis(),
                                ),
                        )
                    }

                    SyncAction.DELETE -> {
                        firebaseDataSource.deleteJetpack(
                            unsyncedJetpack
                                .toFirebaseJetpack()
                                .copy(
                                    lastSynced = System.currentTimeMillis(),
                                ),
                        )
                    }

                    SyncAction.NONE -> {
                        // Do nothing
                    }
                }

                localDataSource.markAsSynced(unsyncedJetpack.id)
                emit(
                    SyncProgress(
                        total = totalUnsyncedJetpacks,
                        current = index + 1,
                        message = "Syncing jetpacks with the cloud",
                    ),
                )
            }

            // Remote jetpacks
            val lastSynced = localDataSource.getLatestUpdateTimestamp(userId)
            val remoteJetpacks = firebaseDataSource.pullJetpacks(userId, lastSynced)
            val totalRemoteJetpacks = remoteJetpacks.size
            Timber.d("Syncing $totalRemoteJetpacks remote jetpacks")

            // Pull updates from remote
            // We pull after pushing local changes to save the local changes to the cloud first
            // and avoid accidentally overwriting them with stale data
            remoteJetpacks.forEachIndexed { index, remoteJetpack ->
                localDataSource.upsertJetpack(remoteJetpack.toJetpackEntity())
                emit(
                    SyncProgress(
                        total = totalRemoteJetpacks,
                        current = index + 1,
                        message = "Fetching jetpacks from the cloud",
                    ),
                )
            }
        }
    }
}
