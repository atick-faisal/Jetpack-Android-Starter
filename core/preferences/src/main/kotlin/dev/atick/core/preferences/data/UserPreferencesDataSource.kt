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
 * Data source for managing user preferences using DataStore (Proto DataStore pattern).
 *
 * This interface provides type-safe access to user preferences stored via Jetpack DataStore.
 * Unlike SharedPreferences, DataStore offers:
 * - **Type safety** through Protocol Buffers (Proto DataStore)
 * - **Reactive updates** via [Flow]
 * - **Coroutine support** for all operations
 * - **Data consistency** with transactional updates
 * - **Crash safety** with atomic read/write operations
 *
 * ## Design Pattern
 *
 * This follows the **DataStore Pattern**:
 * - All reads return [Flow] for reactive UI updates
 * - All writes are suspend functions (transactional and async)
 * - Preferences are strongly typed via [UserDataPreferences]
 * - No blocking I/O operations
 *
 * ## Common Use Cases
 *
 * 1. **Store user session data** (user ID, profile info)
 * 2. **Manage UI preferences** (theme, dynamic colors)
 * 3. **Persist app settings** that survive app restarts
 * 4. **Track onboarding state** or first-run flags
 *
 * ## Usage in Repositories
 *
 * ```kotlin
 * class UserRepository @Inject constructor(
 *     private val userPreferencesDataSource: UserPreferencesDataSource
 * ) {
 *     // Reactive query for UI
 *     fun observeUserData(): Flow<UserData> =
 *         userPreferencesDataSource.getUserDataPreferences()
 *             .map { it.toDomainModel() }
 *
 *     // Check authentication
 *     suspend fun requireUserId(): String =
 *         userPreferencesDataSource.getUserIdOrThrow()
 *
 *     // Update theme
 *     suspend fun setDarkMode(enabled: Boolean): Result<Unit> = suspendRunCatching {
 *         val config = if (enabled) DarkThemeConfigPreferences.DARK
 *                      else DarkThemeConfigPreferences.LIGHT
 *         userPreferencesDataSource.setDarkThemeConfig(config)
 *     }
 * }
 * ```
 *
 * ## Usage in ViewModels
 *
 * ```kotlin
 * @HiltViewModel
 * class SettingsViewModel @Inject constructor(
 *     private val userPreferencesDataSource: UserPreferencesDataSource
 * ) : ViewModel() {
 *     val userData = userPreferencesDataSource.getUserDataPreferences()
 *         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserDataPreferences())
 *
 *     fun toggleDarkMode() {
 *         _uiState.updateWith {
 *             val newConfig = when (userData.value.darkThemeConfigPreferences) {
 *                 DarkThemeConfigPreferences.LIGHT -> DarkThemeConfigPreferences.DARK
 *                 DarkThemeConfigPreferences.DARK -> DarkThemeConfigPreferences.LIGHT
 *                 DarkThemeConfigPreferences.FOLLOW_SYSTEM -> DarkThemeConfigPreferences.DARK
 *             }
 *             userPreferencesDataSource.setDarkThemeConfig(newConfig)
 *         }
 *     }
 * }
 * ```
 *
 * ## Threading
 *
 * All suspend functions execute on IO dispatcher automatically (handled by DataStore).
 * Callers should **not** wrap calls with [withContext].
 *
 * @see UserDataPreferences for the preferences data model
 * @see DarkThemeConfigPreferences for theme options
 * @see PreferencesUserProfile for user profile data
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
     * @return A [Flow] emitting [UserDataPreferences] on every preference change.
     *         The flow never completes unless DataStore is cleared.
     */
    fun getUserDataPreferences(): Flow<UserDataPreferences>

    /**
     * Retrieves the authenticated user's ID, or throws if user is not authenticated.
     *
     * This is a convenience function to get the user ID when authentication is **required**.
     * If the user ID is empty (not authenticated), this function throws [IllegalStateException].
     *
     * ## When to Use
     *
     * - Before operations that require authentication
     * - In repositories that need the current user ID
     * - When user ID is mandatory for the operation
     *
     * ## Example
     *
     * ```kotlin
     * suspend fun loadUserData(): Result<UserData> = suspendRunCatching {
     *     val userId = userPreferencesDataSource.getUserIdOrThrow()
     *     networkDataSource.fetchUserData(userId)
     * }
     * ```
     *
     * @return The authenticated user's ID as a [String].
     * @throws IllegalStateException if the user is not authenticated (ID is empty).
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
