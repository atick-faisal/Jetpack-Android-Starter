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

package dev.atick.data.model.settings

import dev.atick.core.preferences.model.DarkThemeConfigPreferences
import dev.atick.core.preferences.model.UserDataPreferences

/**
 * Domain model representing user-configurable application settings.
 *
 * This model encapsulates all editable user preferences for themes, appearance, and localization.
 * Settings are persisted in DataStore via [UserDataPreferences] and observed by the app's theme
 * system in [dev.atick.app.JetpackApp] to apply user preferences across the entire application.
 *
 * Settings data flow:
 * - User modifies settings in [dev.atick.feature.settings.ui.SettingsScreen]
 * - [dev.atick.feature.settings.ui.SettingsViewModel] updates via [dev.atick.data.repository.settings.SettingsRepository]
 * - Repository persists changes to DataStore immediately (no sync required)
 * - App composable observes settings and applies theme/language changes reactively
 * - Settings survive app restarts and device reboots (DataStore persistence)
 *
 * Mapping extensions for layer conversion:
 * - [UserDataPreferences.asSettings]: Convert from DataStore preferences to domain model
 * - [DarkThemeConfigPreferences.toDarkThemeConfig]: Convert theme enum from preferences
 * - [DarkThemeConfig.toDarkThemeConfigPreferences]: Convert theme enum to preferences
 *
 * Usage context:
 * - Applied in app-level theme configuration ([dev.atick.app.JetpackApp])
 * - Displayed and edited in settings screen
 * - [useDynamicColor] enables Material You dynamic theming on Android 12+
 * - [darkThemeConfig] controls light/dark/system theme preference
 * - [language] controls app localization (English/Arabic)
 *
 * @property userName Optional user name displayed in settings (nullable for unauthenticated users).
 * @property useDynamicColor Whether to enable Material You dynamic colors (default: true).
 * @property darkThemeConfig Theme preference: follow system, light, or dark (default: FOLLOW_SYSTEM).
 * @property language App language preference (default: ENGLISH).
 *
 * @see UserDataPreferences DataStore preferences model for persistence
 * @see DarkThemeConfig Enum representing theme configuration options
 * @see Language Enum representing supported languages
 * @see dev.atick.data.repository.settings.SettingsRepository Repository providing settings operations
 * @see dev.atick.feature.settings.ui.SettingsViewModel ViewModel managing settings state
 */
data class Settings(
    val userName: String? = null,
    val useDynamicColor: Boolean = true,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val language: Language = Language.ENGLISH,
)

/**
 * Enum class representing configuration options for the dark theme.
 *
 * @property FOLLOW_SYSTEM The dark theme configuration follows the system-wide setting.
 * @property LIGHT The app's dark theme is disabled, using the light theme.
 * @property DARK The app's dark theme is enabled, using the dark theme.
 */
enum class DarkThemeConfig {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
}

/**
 * Enum class representing the supported languages.
 *
 * @property code The language code.
 */
enum class Language(val code: String) {
    ENGLISH("en-US"),
    ARABIC("ar-QA"),
}

/**
 * Extension function to map [UserDataPreferences] to [Settings].
 *
 * @return The mapped [Settings].
 */
fun UserDataPreferences.asSettings(): Settings {
    return Settings(
        userName = userName,
        useDynamicColor = useDynamicColor,
        darkThemeConfig = darkThemeConfigPreferences.toDarkThemeConfig(),
    )
}

/**
 * Extension function to map [DarkThemeConfigPreferences] to [DarkThemeConfig].
 *
 * @return The mapped [DarkThemeConfig].
 */
fun DarkThemeConfigPreferences.toDarkThemeConfig(): DarkThemeConfig {
    return when (this) {
        DarkThemeConfigPreferences.FOLLOW_SYSTEM -> DarkThemeConfig.FOLLOW_SYSTEM
        DarkThemeConfigPreferences.LIGHT -> DarkThemeConfig.LIGHT
        DarkThemeConfigPreferences.DARK -> DarkThemeConfig.DARK
    }
}

/**
 * Extension function to map [DarkThemeConfig] to [DarkThemeConfigPreferences].
 *
 * @return The mapped [DarkThemeConfigPreferences].
 */
fun DarkThemeConfig.toDarkThemeConfigPreferences(): DarkThemeConfigPreferences {
    return when (this) {
        DarkThemeConfig.FOLLOW_SYSTEM -> DarkThemeConfigPreferences.FOLLOW_SYSTEM
        DarkThemeConfig.LIGHT -> DarkThemeConfigPreferences.LIGHT
        DarkThemeConfig.DARK -> DarkThemeConfigPreferences.DARK
    }
}
