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

package dev.atick.data.repository.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.atick.core.preferences.model.DarkThemeConfigPreferences
import dev.atick.core.preferences.model.UserDataPreferences
import dev.atick.core.testing.data.FakeAuthDataSource
import dev.atick.core.testing.data.FakeUserPreferencesDataSource
import dev.atick.data.model.settings.DarkThemeConfig
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Reference test for the repository pattern used throughout `:data`.
 *
 * Shows the three things every repository test here should do: read through the exposed Flow
 * rather than the fake's internals, assert that writes land in the data source, and check that
 * a thrown exception comes back as a failed [Result] instead of propagating — that last part is
 * what `suspendRunCatching` buys and what the UI layer depends on.
 *
 * The settings repository was chosen because it survives an adopter deleting the demo feature.
 */
class SettingsRepositoryImplTest {

    private val preferences = FakeUserPreferencesDataSource()
    private val auth = FakeAuthDataSource()

    private val repository = SettingsRepositoryImpl(
        authDataSource = auth,
        userPreferencesDataSource = preferences,
    )

    @Test
    fun `getSettings maps preferences into the domain model`() = runTest {
        val source = FakeUserPreferencesDataSource(
            UserDataPreferences(
                id = "user-1",
                userName = "Ada",
                darkThemeConfigPreferences = DarkThemeConfigPreferences.DARK,
                useDynamicColor = false,
            ),
        )
        val repository = SettingsRepositoryImpl(auth, source)

        repository.getSettings().test {
            val settings = awaitItem()

            assertThat(settings.userName).isEqualTo("Ada")
            assertThat(settings.darkThemeConfig).isEqualTo(DarkThemeConfig.DARK)
            assertThat(settings.useDynamicColor).isFalse()
        }
    }

    @Test
    fun `getSettings emits again when the underlying preferences change`() = runTest {
        repository.getSettings().test {
            assertThat(awaitItem().darkThemeConfig).isEqualTo(DarkThemeConfig.FOLLOW_SYSTEM)

            repository.setDarkThemeConfig(DarkThemeConfig.DARK)

            assertThat(awaitItem().darkThemeConfig).isEqualTo(DarkThemeConfig.DARK)
        }
    }

    @Test
    fun `setDarkThemeConfig writes through and reports success`() = runTest {
        val result = repository.setDarkThemeConfig(DarkThemeConfig.LIGHT)

        assertThat(result.isSuccess).isTrue()
        assertThat(preferences.current.darkThemeConfigPreferences)
            .isEqualTo(DarkThemeConfigPreferences.LIGHT)
    }

    @Test
    fun `setDynamicColorPreference writes through and reports success`() = runTest {
        val result = repository.setDynamicColorPreference(false)

        assertThat(result.isSuccess).isTrue()
        assertThat(preferences.current.useDynamicColor).isFalse()
    }

    @Test
    fun `signOut clears the auth session and resets preferences`() = runTest {
        val source = FakeUserPreferencesDataSource(
            UserDataPreferences(id = "user-1", userName = "Ada"),
        )
        val repository = SettingsRepositoryImpl(auth, source)

        val result = repository.signOut()

        assertThat(result.isSuccess).isTrue()
        assertThat(auth.getCurrentUser()).isNull()
        assertThat(source.current).isEqualTo(UserDataPreferences())
    }

    @Test
    fun `signOut returns a failed Result rather than throwing`() = runTest {
        val boom = IllegalStateException("network down")
        auth.signOutError = boom

        val result = repository.signOut()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isSameInstanceAs(boom)
    }

    @Test
    fun `a failed signOut leaves preferences untouched`() = runTest {
        val source = FakeUserPreferencesDataSource(UserDataPreferences(id = "user-1"))
        val repository = SettingsRepositoryImpl(auth, source)
        auth.signOutError = IllegalStateException("network down")

        repository.signOut()

        // signOut() runs before resetUserPreferences(), so failing there must not clear
        // local state and leave the user half signed out.
        assertThat(source.current.id).isEqualTo("user-1")
    }
}
