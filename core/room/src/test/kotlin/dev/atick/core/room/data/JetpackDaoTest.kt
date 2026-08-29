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

package dev.atick.core.room.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.atick.core.room.model.JetpackEntity
import dev.atick.core.room.model.SyncAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Reference test for the Room layer, and the only executable description of what the sync
 * metadata on [JetpackEntity] actually means.
 *
 * The offline-first scheme is easy to get subtly wrong: `deleted` is a soft delete that must
 * stay queryable for the sync pass, and "needs syncing" is two conditions OR'd together, not
 * just the `needsSync` flag. Both are pinned here.
 *
 * Lives in `:core:room` next to the entity it describes, so an adopter replacing the demo model
 * deletes the entity and this test together.
 */
@RunWith(RobolectricTestRunner::class)
class JetpackDaoTest {

    private lateinit var database: JetpackDatabase
    private lateinit var dao: JetpackDao

    private val userId = "user-1"
    private val otherUserId = "user-2"

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            JetpackDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.getJetpackDao()
    }

    @After
    fun closeDatabase() = database.close()

    private fun jetpack(
        id: String,
        name: String = "Jetpack $id",
        user: String = userId,
        lastUpdated: Long = 0,
        lastSynced: Long = 0,
        serverUpdatedAtNanos: Long = 0,
        needsSync: Boolean = false,
        deleted: Boolean = false,
        syncAction: SyncAction = SyncAction.NONE,
    ) = JetpackEntity(
        id = id,
        name = name,
        price = 1.0,
        userId = user,
        lastUpdated = lastUpdated,
        lastSynced = lastSynced,
        serverUpdatedAtNanos = serverUpdatedAtNanos,
        needsSync = needsSync,
        deleted = deleted,
        syncAction = syncAction,
    )

    @Test
    fun `getJetpacks returns only the given user's rows`() = runTest {
        dao.insertJetpack(jetpack("a"))
        dao.insertJetpack(jetpack("b", user = otherUserId))

        val result = dao.getJetpacks(userId).first()

        assertThat(result.map { it.id }).containsExactly("a")
    }

    @Test
    fun `getJetpacks orders by lastUpdated descending`() = runTest {
        dao.insertJetpack(jetpack("old", lastUpdated = 100))
        dao.insertJetpack(jetpack("new", lastUpdated = 300))
        dao.insertJetpack(jetpack("mid", lastUpdated = 200))

        val result = dao.getJetpacks(userId).first()

        assertThat(result.map { it.id }).containsExactly("new", "mid", "old").inOrder()
    }

    @Test
    fun `getJetpacks hides soft-deleted rows`() = runTest {
        dao.insertJetpack(jetpack("visible"))
        dao.insertJetpack(jetpack("gone", deleted = true))

        val result = dao.getJetpacks(userId).first()

        assertThat(result.map { it.id }).containsExactly("visible")
    }

    @Test
    fun `markJetpackAsDeleted soft deletes and queues a DELETE for sync`() = runTest {
        dao.insertJetpack(jetpack("a"))

        dao.markJetpackAsDeleted("a")

        // Hidden from the UI...
        assertThat(dao.getJetpacks(userId).first()).isEmpty()

        // ...but still present, so the sync pass can push the deletion to the remote.
        val row = dao.getJetpack("a").first()
        assertThat(row.deleted).isTrue()
        assertThat(row.needsSync).isTrue()
        assertThat(row.syncAction).isEqualTo(SyncAction.DELETE)
    }

    @Test
    fun `deleteJetpackPermanently removes the row entirely`() = runTest {
        dao.insertJetpack(jetpack("a"))

        dao.deleteJetpackPermanently("a")

        assertThat(dao.getUnsyncedJetpacks(userId)).isEmpty()
        assertThat(dao.getJetpacks(userId).first()).isEmpty()
    }

    @Test
    fun `getUnsyncedJetpacks includes rows flagged with needsSync`() = runTest {
        dao.insertJetpack(jetpack("dirty", needsSync = true, lastUpdated = 5, lastSynced = 10))

        assertThat(dao.getUnsyncedJetpacks(userId).map { it.id }).containsExactly("dirty")
    }

    @Test
    fun `getUnsyncedJetpacks includes rows edited since their last sync`() = runTest {
        // needsSync is false, but the timestamps say this row changed after it was last pushed.
        // The OR in the query is what stops that edit being silently dropped.
        dao.insertJetpack(jetpack("stale", needsSync = false, lastUpdated = 20, lastSynced = 10))

        assertThat(dao.getUnsyncedJetpacks(userId).map { it.id }).containsExactly("stale")
    }

    @Test
    fun `getUnsyncedJetpacks excludes rows that are already in sync`() = runTest {
        dao.insertJetpack(jetpack("clean", needsSync = false, lastUpdated = 10, lastSynced = 20))

        assertThat(dao.getUnsyncedJetpacks(userId)).isEmpty()
    }

    @Test
    fun `getUnsyncedJetpacks still returns soft-deleted rows`() = runTest {
        dao.insertJetpack(jetpack("gone", deleted = true, needsSync = true))

        // A deletion that never reached the remote is exactly what sync exists to push.
        assertThat(dao.getUnsyncedJetpacks(userId).map { it.id }).containsExactly("gone")
    }

    @Test
    fun `getUnsyncedJetpacks is scoped to the user`() = runTest {
        dao.insertJetpack(jetpack("mine", needsSync = true))
        dao.insertJetpack(jetpack("theirs", user = otherUserId, needsSync = true))

        assertThat(dao.getUnsyncedJetpacks(userId).map { it.id }).containsExactly("mine")
    }

    @Test
    fun `markAsSynced clears the sync flags and stamps lastSynced`() = runTest {
        dao.insertJetpack(
            jetpack("a", needsSync = true, syncAction = SyncAction.UPSERT, lastUpdated = 10),
        )

        dao.markAsSynced("a", timestamp = 50)

        val row = dao.getJetpack("a").first()
        assertThat(row.needsSync).isFalse()
        assertThat(row.syncAction).isEqualTo(SyncAction.NONE)
        assertThat(row.lastSynced).isEqualTo(50)
        assertThat(dao.getUnsyncedJetpacks(userId)).isEmpty()
    }

    @Test
    fun `upsert replaces an existing row rather than duplicating it`() = runTest {
        dao.insertJetpack(jetpack("a", name = "before"))

        dao.upsertJetpack(jetpack("a", name = "after"))

        val result = dao.getJetpacks(userId).first()
        assertThat(result).hasSize(1)
        assertThat(result.single().name).isEqualTo("after")
    }

    @Test
    fun `upsertJetpacks applies a batch from the remote`() = runTest {
        dao.insertJetpack(jetpack("a", name = "before"))

        dao.upsertJetpacks(listOf(jetpack("a", name = "after"), jetpack("b")))

        assertThat(dao.getJetpacks(userId).first().map { it.id })
            .containsExactly("a", "b")
    }

    @Test
    fun `getSyncCursor returns the newest server timestamp`() = runTest {
        dao.insertJetpack(jetpack("a", serverUpdatedAtNanos = 100))
        dao.insertJetpack(jetpack("b", serverUpdatedAtNanos = 300))

        assertThat(dao.getSyncCursor(userId)).isEqualTo(300)
    }

    @Test
    fun `getSyncCursor ignores local edit timestamps`() = runTest {
        // A row this device edited but has not yet pushed: the server has never seen it, so it
        // must not move the cursor no matter what the device clock says.
        dao.insertJetpack(
            jetpack("pending", lastUpdated = Long.MAX_VALUE, needsSync = true),
        )
        dao.insertJetpack(jetpack("pulled", lastUpdated = 1, serverUpdatedAtNanos = 300))

        assertThat(dao.getSyncCursor(userId)).isEqualTo(300)
    }

    @Test
    fun `getSyncCursor counts soft-deleted rows`() = runTest {
        dao.insertJetpack(jetpack("a", serverUpdatedAtNanos = 100))
        dao.insertJetpack(jetpack("b", serverUpdatedAtNanos = 300, deleted = true))

        // The server timestamp on a deleted row has still been consumed. Skipping it would rewind
        // the cursor and re-pull everything written between the two.
        assertThat(dao.getSyncCursor(userId)).isEqualTo(300)
    }

    @Test
    fun `getSyncCursor is scoped to the user`() = runTest {
        dao.insertJetpack(jetpack("mine", serverUpdatedAtNanos = 100))
        dao.insertJetpack(jetpack("theirs", user = otherUserId, serverUpdatedAtNanos = 300))

        assertThat(dao.getSyncCursor(userId)).isEqualTo(100)
    }

    @Test
    fun `getSyncCursor is null when the user has no rows`() = runTest {
        assertThat(dao.getSyncCursor(userId)).isNull()
    }
}
