/*
 * Copyright 2026 Atick Faisal
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

import com.google.common.truth.Truth.assertThat
import com.google.firebase.Timestamp
import dev.atick.core.preferences.model.UserDataPreferences
import dev.atick.core.room.data.LocalDataSource
import dev.atick.core.room.model.JetpackEntity
import dev.atick.core.room.model.SyncAction
import dev.atick.core.testing.data.FakeUserPreferencesDataSource
import dev.atick.data.model.home.Jetpack
import dev.atick.data.utils.SyncManager
import dev.atick.firebase.firestore.data.FirebaseDataSource
import dev.atick.firebase.firestore.model.FirebaseJetpack
import dev.atick.firebase.firestore.model.serverUpdatedAtNanos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Covers the two-way sync in [HomeRepositoryImpl], and in particular which clock orders a pull.
 *
 * `lastUpdated` is stamped by whichever device edited a row, so it cannot order records across
 * devices: the fleet's fastest clock drags the cursor into the future and every record written by a
 * slower device is skipped from then on, with no error anywhere. The cursor therefore reads
 * `serverUpdatedAtNanos`, assigned by Firestore itself. The tests below fail if it goes back to a
 * device clock.
 */
class HomeRepositoryImplTest {

    private val userId = "user-1"

    private val local = FakeLocalDataSource()
    private val remote = FakeFirebaseDataSource()
    private val preferences = FakeUserPreferencesDataSource(UserDataPreferences(id = userId))
    private val syncManager = RecordingSyncManager()

    private val repository = HomeRepositoryImpl(
        localDataSource = local,
        preferencesDataSource = preferences,
        firebaseDataSource = remote,
        syncManager = syncManager,
    )

    @Test
    fun `sync ingests a remote record older than the newest local edit`() = runTest {
        // This device's clock is far ahead of the one that wrote the remote record.
        local.put(entity("local", lastUpdated = SKEWED_CLOCK, serverUpdatedAtNanos = 100))
        remote.documents += document("incoming", lastUpdated = 50, serverUpdatedAtNanos = 200)

        repository.sync().toList()

        assertThat(local.jetpacks.keys).contains("incoming")
    }

    @Test
    fun `sync pulls from the server cursor, not the local edit clock`() = runTest {
        local.put(entity("local", lastUpdated = SKEWED_CLOCK, serverUpdatedAtNanos = 100))

        repository.sync().toList()

        assertThat(remote.pullCursors).containsExactly(100L)
    }

    @Test
    fun `sync advances the cursor to the newest server timestamp pulled`() = runTest {
        remote.documents += document("a", serverUpdatedAtNanos = 200)
        remote.documents += document("b", serverUpdatedAtNanos = 700)

        repository.sync().toList()

        assertThat(local.getSyncCursor(userId)).isEqualTo(700)

        // A second pass resumes from there rather than replaying what it already has.
        repository.sync().toList()

        assertThat(remote.pullCursors).containsExactly(0L, 700L).inOrder()
    }

    @Test
    fun `sync leaves the cursor alone when nothing comes back`() = runTest {
        local.put(entity("local", serverUpdatedAtNanos = 400))

        repository.sync().toList()

        assertThat(local.getSyncCursor(userId)).isEqualTo(400)
    }

    @Test
    fun `sync pushes local changes before pulling`() = runTest {
        local.put(entity("pending", needsSync = true, syncAction = SyncAction.UPSERT))

        repository.sync().toList()

        // Pushing first is what stops the pull overwriting an unsynced local edit with older
        // remote data.
        assertThat(remote.calls).containsExactly("upsert:pending", "pull").inOrder()
    }

    @Test
    fun `a pushed row records its server timestamp when the echo comes back`() = runTest {
        local.put(entity("pending", needsSync = true, syncAction = SyncAction.UPSERT))
        // A push cannot report the timestamp the server assigned, so the row returns on the pull
        // that follows. That echo is what moves the cursor past this device's own writes.
        remote.documents += document("pending", serverUpdatedAtNanos = 900)

        repository.sync().toList()

        assertThat(local.jetpacks.getValue("pending").serverUpdatedAtNanos).isEqualTo(900)
        assertThat(local.getSyncCursor(userId)).isEqualTo(900)
    }

    @Test
    fun `createOrUpdateJetpack flags the row for the next push`() = runTest {
        val result = repository.createOrUpdateJetpack(Jetpack(id = "new", name = "Jetpack"))

        assertThat(result.isSuccess).isTrue()
        val saved = local.jetpacks.getValue("new")
        assertThat(saved.userId).isEqualTo(userId)
        assertThat(saved.needsSync).isTrue()
        assertThat(saved.syncAction).isEqualTo(SyncAction.UPSERT)
        // Untouched by a local write: only the server sets it.
        assertThat(saved.serverUpdatedAtNanos).isEqualTo(0)
        assertThat(syncManager.requests).isGreaterThan(0)
    }

    private fun entity(
        id: String,
        lastUpdated: Long = 0,
        serverUpdatedAtNanos: Long = 0,
        needsSync: Boolean = false,
        syncAction: SyncAction = SyncAction.NONE,
    ) = JetpackEntity(
        id = id,
        name = "Jetpack $id",
        price = 1.0,
        userId = userId,
        lastUpdated = lastUpdated,
        lastSynced = lastUpdated,
        serverUpdatedAtNanos = serverUpdatedAtNanos,
        needsSync = needsSync,
        syncAction = syncAction,
    )

    private fun document(
        id: String,
        lastUpdated: Long = 0,
        serverUpdatedAtNanos: Long,
    ) = FirebaseJetpack(
        id = id,
        name = "Jetpack $id",
        price = 1.0,
        userId = userId,
        lastUpdated = lastUpdated,
    ).apply {
        serverUpdatedAt = Timestamp(
            serverUpdatedAtNanos / NANOS_PER_SECOND,
            (serverUpdatedAtNanos % NANOS_PER_SECOND).toInt(),
        )
    }

    private companion object {
        /** A device clock running absurdly far ahead, in milliseconds. */
        const val SKEWED_CLOCK = 4_000_000_000_000L
        const val NANOS_PER_SECOND = 1_000_000_000L
    }
}

/** In-memory [LocalDataSource] mirroring the real DAO queries. */
private class FakeLocalDataSource : LocalDataSource {

    private val state = MutableStateFlow(emptyMap<String, JetpackEntity>())

    val jetpacks: Map<String, JetpackEntity> get() = state.value

    fun put(jetpack: JetpackEntity) {
        state.value = state.value + (jetpack.id to jetpack)
    }

    override fun getJetpacks(userId: String): Flow<List<JetpackEntity>> =
        state.map { jetpacks -> jetpacks.values.filter { it.userId == userId && !it.deleted } }

    override fun getJetpack(id: String): Flow<JetpackEntity> =
        state.map { it.getValue(id) }

    override suspend fun getUnsyncedJetpacks(userId: String): List<JetpackEntity> =
        state.value.values.filter {
            it.userId == userId && (it.needsSync || it.lastUpdated > it.lastSynced)
        }

    override suspend fun insertJetpack(jetpackEntity: JetpackEntity) = put(jetpackEntity)

    override suspend fun upsertJetpack(jetpackEntity: JetpackEntity) = put(jetpackEntity)

    override suspend fun upsertJetpacks(remoteJetpacks: List<JetpackEntity>) =
        remoteJetpacks.forEach(::put)

    override suspend fun updateJetpack(jetpackEntity: JetpackEntity) = put(jetpackEntity)

    override suspend fun markJetpackAsDeleted(id: String) {
        put(
            state.value.getValue(id)
                .copy(deleted = true, needsSync = true, syncAction = SyncAction.DELETE),
        )
    }

    override suspend fun deleteJetpackPermanently(id: String) {
        state.value = state.value - id
    }

    override suspend fun markAsSynced(id: String, timestamp: Long) {
        put(
            state.value.getValue(id)
                .copy(needsSync = false, syncAction = SyncAction.NONE, lastSynced = timestamp),
        )
    }

    override suspend fun getSyncCursor(userId: String): Long =
        state.value.values
            .filter { it.userId == userId }
            .maxOfOrNull { it.serverUpdatedAtNanos } ?: 0
}

/**
 * In-memory [FirebaseDataSource] that filters exactly the way the real Firestore query does, so a
 * cursor taken from the wrong field shows up as records that never arrive.
 */
private class FakeFirebaseDataSource : FirebaseDataSource {

    val documents = mutableListOf<FirebaseJetpack>()
    val pullCursors = mutableListOf<Long>()
    val calls = mutableListOf<String>()

    override suspend fun pullJetpacks(
        userId: String,
        syncedAfterNanos: Long,
    ): List<FirebaseJetpack> {
        pullCursors += syncedAfterNanos
        calls += "pull"
        return documents.filter {
            it.userId == userId && it.serverUpdatedAtNanos() > syncedAfterNanos
        }
    }

    override suspend fun createJetpack(firebaseJetpack: FirebaseJetpack) {
        calls += "create:${firebaseJetpack.id}"
    }

    override suspend fun createOrUpdateJetpack(firebaseJetpack: FirebaseJetpack) {
        calls += "upsert:${firebaseJetpack.id}"
    }

    override suspend fun deleteJetpack(firebaseJetpack: FirebaseJetpack) {
        calls += "delete:${firebaseJetpack.id}"
    }
}

private class RecordingSyncManager : SyncManager {
    var requests = 0
        private set

    override val isSyncing: Flow<Boolean> = MutableStateFlow(false)

    override fun requestSync() {
        requests++
    }
}
