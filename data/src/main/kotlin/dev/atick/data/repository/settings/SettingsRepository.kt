/*
 * Copyright 2023 Atick Faisal
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

import dev.atick.data.model.settings.DarkThemeConfig
import dev.atick.data.model.settings.Settings
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing user settings.
 *
 * This repository uses DataStore for local persistence of user preferences like theme
 * configuration and dynamic color preferences. All settings are stored locally only.
 *
 * @see SettingsRepositoryImpl Implementation class with DataStore integration
 */
interface SettingsRepository {
    /**
     * Retrieves the user settings as a Flow.
     *
     * @return A Flow emitting the user settings.
     */
    fun getSettings(): Flow<Settings>

    /**
     * Sets the dark theme configuration.
     *
     * @param darkThemeConfig The dark theme configuration to set.
     * @return A Result indicating the success or failure of the operation.
     */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig): Result<Unit>

    /**
     * Sets the dynamic color preference.
     *
     * @param useDynamicColor Whether to use dynamic colors.
     * @return A Result indicating the success or failure of the operation.
     */
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean): Result<Unit>

    /**
     * Signs out the current user.
     *
     * @return A Result indicating the success or failure of the operation.
     */
    suspend fun signOut(): Result<Unit>
}
