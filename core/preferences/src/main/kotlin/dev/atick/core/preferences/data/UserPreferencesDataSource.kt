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

package dev.atick.core.preferences.data

import dev.atick.core.preferences.model.DarkThemeConfigPreferences
import dev.atick.core.preferences.model.PreferencesUserProfile
import dev.atick.core.preferences.model.UserDataPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Reads and updates [UserDataPreferences] in a typed JSON DataStore.
 *
 * Reads expose a [Flow]; writes use transactional updateData calls through the implementation's
 * IO dispatcher. Stored profile data is not a check of the current Firebase authentication session.
 * Read/write failures propagate; malformed JSON has no configured default-value recovery handler.
 * Repositories map this storage model to screen-facing data and wrap one-time operations in Result.
 *
 * @see UserDataPreferences
 * @see DarkThemeConfigPreferences
 * @see PreferencesUserProfile
 */
interface UserPreferencesDataSource {

    /**
     * Observes the complete user preferences data.
     *
     * This function returns a cold [Flow] that emits the current [UserDataPreferences]
     * whenever any preference value changes. The flow is backed by DataStore and provides
     * reactive updates to all preference changes.
     *
     * ## When to Use
     *
     * - Observing user session data (ID, name, profile picture)
     * - Tracking theme preferences for UI updates
     * - Monitoring dynamic color settings
     *
     * ## Example
     *
     * ```kotlin
     * // In ViewModel
     * val isDarkMode = userPreferencesDataSource.getUserDataPreferences()
     *     .map { it.darkThemeConfigPreferences == DarkThemeConfigPreferences.DARK }
     *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
     * ```
     *
     * @return A [Flow] of stored preferences; storage and decoding failures propagate.
     */
    fun getUserDataPreferences(): Flow<UserDataPreferences>

    /**
     * Returns the stored profile ID, rejecting an empty value.
     *
     * This does not check Firebase Auth or refresh an expired session.
     *
     * @return Stored user ID.
     * @throws IllegalStateException if the stored ID is empty.
     */
    suspend fun getUserIdOrThrow(): String

    /**
     * Updates the user's profile information in preferences.
     *
     * This function performs a transactional update to the user profile data, including
     * user ID, name, and profile picture URI. The update is atomic and crash-safe.
     *
     * ## When to Use
     *
     * - After successful authentication (store user session)
     * - After profile updates from remote
     * - When user edits their profile locally
     *
     * ## Example
     *
     * ```kotlin
     * suspend fun saveUserProfile(user: User): Result<Unit> = suspendRunCatching {
     *     val profile = PreferencesUserProfile(
     *         id = user.id,
     *         userName = user.name,
     *         profilePictureUriString = user.avatarUrl
     *     )
     *     userPreferencesDataSource.setUserProfile(profile)
     * }
     * ```
     *
     * @param preferencesUserProfile The user profile data to store.
     */
    suspend fun setUserProfile(preferencesUserProfile: PreferencesUserProfile)

    /**
     * Updates the dark theme configuration preference.
     *
     * This function atomically updates the user's preferred dark theme setting.
     * The change triggers a reactive update in all collectors of [getUserDataPreferences].
     *
     * ## When to Use
     *
     * - User toggles theme in settings
     * - Applying theme from synced preferences
     * - Resetting theme to system default
     *
     * ## Example
     *
     * ```kotlin
     * fun setLightMode() {
     *     _uiState.updateWith {
     *         userPreferencesDataSource.setDarkThemeConfig(
     *             DarkThemeConfigPreferences.LIGHT
     *         )
     *     }
     * }
     * ```
     *
     * @param darkThemeConfigPreferences The desired dark theme configuration.
     * @see DarkThemeConfigPreferences for available options
     */
    suspend fun setDarkThemeConfig(darkThemeConfigPreferences: DarkThemeConfigPreferences)

    /**
     * Updates the dynamic color preference.
     *
     * Dynamic colors use the system's Material You color scheme (Android 12+).
     * This preference controls whether the app adopts the system color palette.
     *
     * ## When to Use
     *
     * - User toggles dynamic color in settings
     * - Applying synced color preferences
     * - Disabling dynamic colors for branded themes
     *
     * ## Example
     *
     * ```kotlin
     * fun enableDynamicColors() {
     *     _uiState.updateWith {
     *         userPreferencesDataSource.setDynamicColorPreference(true)
     *     }
     * }
     * ```
     *
     * @param useDynamicColor `true` to enable dynamic colors, `false` to use static theme colors.
     */
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    /**
     * Resets all user preferences to their default values.
     *
     * This function clears all user data and preference settings, returning them to
     * the defaults defined in [UserDataPreferences]. This is typically used during logout.
     *
     * ## When to Use
     *
     * - User logout (clear session data)
     * - Account switching
     * - Factory reset / clear app data
     *
     * ## Example
     *
     * ```kotlin
     * suspend fun logout(): Result<Unit> = suspendRunCatching {
     *     userPreferencesDataSource.resetUserPreferences()
     *     authService.signOut()
     * }
     * ```
     *
     * ## Post-Reset State
     *
     * After reset, [getUserDataPreferences] will emit:
     * - `id = ""`
     * - `userName = null`
     * - `profilePictureUriString = null`
     * - `darkThemeConfigPreferences = FOLLOW_SYSTEM`
     * - `useDynamicColor = true`
     */
    suspend fun resetUserPreferences()
}
