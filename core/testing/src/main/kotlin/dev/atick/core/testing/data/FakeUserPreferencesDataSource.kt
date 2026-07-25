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

package dev.atick.core.testing.data

import dev.atick.core.preferences.data.UserPreferencesDataSource
import dev.atick.core.preferences.model.DarkThemeConfigPreferences
import dev.atick.core.preferences.model.PreferencesUserProfile
import dev.atick.core.preferences.model.UserDataPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory [UserPreferencesDataSource] for tests.
 *
 * A fake rather than a mock: it holds real state in a [MutableStateFlow], so a test can write
 * through the interface and then assert on what a collector observes. That is the behaviour
 * that matters for a DataStore-backed source, and mocks cannot express it.
 *
 * @param initial The state to start from.
 */
class FakeUserPreferencesDataSource(
    initial: UserDataPreferences = UserDataPreferences(),
) : UserPreferencesDataSource {

    private val state = MutableStateFlow(initial)

    /** The current value, for assertions that do not need to collect the flow. */
    val current: UserDataPreferences get() = state.value

    override fun getUserDataPreferences(): Flow<UserDataPreferences> = state.asStateFlow()

    override suspend fun getUserIdOrThrow(): String =
        state.value.id.ifEmpty { throw IllegalStateException("User is not logged in") }

    override suspend fun setUserProfile(preferencesUserProfile: PreferencesUserProfile) {
        state.update {
            it.copy(
                id = preferencesUserProfile.id,
                userName = preferencesUserProfile.userName,
                profilePictureUriString = preferencesUserProfile.profilePictureUriString,
            )
        }
    }

    override suspend fun setDarkThemeConfig(
        darkThemeConfigPreferences: DarkThemeConfigPreferences,
    ) {
        state.update { it.copy(darkThemeConfigPreferences = darkThemeConfigPreferences) }
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        state.update { it.copy(useDynamicColor = useDynamicColor) }
    }

    override suspend fun resetUserPreferences() {
        state.update { UserDataPreferences() }
    }
}
