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

package dev.atick.core.preferences.utils

import androidx.datastore.core.CorruptionException
import com.google.common.truth.Truth.assertThat
import dev.atick.core.preferences.model.DarkThemeConfigPreferences
import dev.atick.core.preferences.model.UserDataPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertFailsWith

/**
 * This serializer sits between the app and the user's on-disk preferences. If it throws
 * something other than [CorruptionException], DataStore cannot apply its corruption handler and
 * the app crashes on launch with no way for the user to recover short of clearing app data.
 */
class UserDataSerializerTest {

    private val serializer = UserDataSerializer

    @Test
    fun `default value is an empty preferences object`() {
        assertThat(serializer.defaultValue).isEqualTo(UserDataPreferences())
    }

    @Test
    fun `round-trips a fully populated value`() = runTest {
        val original = UserDataPreferences(
            id = "user-1",
            userName = "Ada",
            profilePictureUriString = "content://avatar",
            darkThemeConfigPreferences = DarkThemeConfigPreferences.DARK,
            useDynamicColor = false,
        )

        val restored = serializer.roundTrip(original)

        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `round-trips the default value`() = runTest {
        assertThat(serializer.roundTrip(UserDataPreferences()))
            .isEqualTo(UserDataPreferences())
    }

    @Test
    fun `round-trips a null profile picture and user name`() = runTest {
        val original = UserDataPreferences(
            id = "user-1",
            userName = null,
            profilePictureUriString = null,
        )

        assertThat(serializer.roundTrip(original)).isEqualTo(original)
    }

    @Test
    fun `every dark theme config survives a round trip`() = runTest {
        // DarkThemeConfigPreferences is written by name through a custom KSerializer, so
        // renaming an entry is a silent on-disk format break.
        DarkThemeConfigPreferences.entries.forEach { config ->
            val original = UserDataPreferences(darkThemeConfigPreferences = config)

            assertThat(serializer.roundTrip(original).darkThemeConfigPreferences)
                .isEqualTo(config)
        }
    }

    @Test
    fun `malformed json is reported as corruption`() = runTest {
        assertFailsWith<CorruptionException> {
            serializer.readFrom(ByteArrayInputStream("not json".toByteArray()))
        }
    }

    @Test
    fun `empty input is reported as corruption`() = runTest {
        assertFailsWith<CorruptionException> {
            serializer.readFrom(ByteArrayInputStream(ByteArray(0)))
        }
    }

    @Test
    fun `truncated json is reported as corruption`() = runTest {
        assertFailsWith<CorruptionException> {
            serializer.readFrom(ByteArrayInputStream("""{"id":"user-1""".toByteArray()))
        }
    }

    @Test
    fun `an unknown dark theme config is reported as corruption`() = runTest {
        // Reading a value written by a newer build that added an enum entry.
        val json = """{"id":"u","darkThemeConfigPreferences":"NEON"}"""

        assertFailsWith<CorruptionException> {
            serializer.readFrom(ByteArrayInputStream(json.toByteArray()))
        }
    }

    @Test
    fun `missing fields fall back to their defaults`() = runTest {
        val json = """{"id":"user-1"}"""

        val restored = serializer.readFrom(ByteArrayInputStream(json.toByteArray()))

        assertThat(restored.id).isEqualTo("user-1")
        assertThat(restored.useDynamicColor).isTrue()
        assertThat(restored.darkThemeConfigPreferences)
            .isEqualTo(DarkThemeConfigPreferences.FOLLOW_SYSTEM)
    }

    private suspend fun UserDataSerializer.roundTrip(
        value: UserDataPreferences,
    ): UserDataPreferences {
        val bytes = ByteArrayOutputStream().also { writeTo(value, it) }.toByteArray()
        return readFrom(ByteArrayInputStream(bytes))
    }
}
