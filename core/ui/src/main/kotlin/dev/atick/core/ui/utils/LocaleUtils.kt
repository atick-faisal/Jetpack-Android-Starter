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

package dev.atick.core.ui.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Returns the user's currently preferred application locale.
 *
 * This function retrieves the application-specific locale set via [setLanguagePreference].
 * If no application locale is set, it falls back to the system default locale.
 *
 * ## Usage in ViewModels
 * ```kotlin
 * @HiltViewModel
 * class SettingsViewModel @Inject constructor() : ViewModel() {
 *     private val _uiState = MutableStateFlow(
 *         UiState(
 *             SettingsScreenData(
 *                 currentLanguage = getPreferredLocale().displayLanguage
 *             )
 *         )
 *     )
 * }
 * ```
 *
 * ## Usage for Formatting
 * ```kotlin
 * val locale = getPreferredLocale()
 * val formattedDate = SimpleDateFormat("dd MMMM yyyy", locale).format(Date())
 * val formattedNumber = NumberFormat.getCurrencyInstance(locale).format(price)
 * ```
 *
 * ## Locale Information
 * ```kotlin
 * val locale = getPreferredLocale()
 * val languageCode = locale.language        // "en", "es", "fr", etc.
 * val countryCode = locale.country          // "US", "ES", "FR", etc.
 * val displayName = locale.displayLanguage  // "English", "Español", "Français"
 * ```
 *
 * @return The user's preferred [Locale], or system default if none is set
 *
 * @see setLanguagePreference
 * @see Locale
 */
fun getPreferredLocale(): Locale {
    val applicationLocales = AppCompatDelegate.getApplicationLocales()
    return if (applicationLocales.isEmpty) {
        Locale.getDefault()
    } else {
        applicationLocales.get(0) ?: Locale.getDefault()
    }
}

/**
 * Sets the application locale based on a language code.
 *
 * This function allows per-app language preferences, independent of the system
 * language. The locale change takes effect immediately and persists across
 * app restarts. All activities are automatically recreated to reflect the new locale.
 *
 * ## Supported Language Tags
 * Use IETF BCP 47 language tags:
 * - Simple: `"en"`, `"es"`, `"fr"`, `"de"`, `"ja"`, `"zh"`
 * - With region: `"en-US"`, `"en-GB"`, `"es-ES"`, `"es-MX"`, `"zh-CN"`, `"zh-TW"`
 * - With script: `"zh-Hans"` (Simplified Chinese), `"zh-Hant"` (Traditional Chinese)
 *
 * ## Usage in ViewModel
 * ```kotlin
 * @HiltViewModel
 * class SettingsViewModel @Inject constructor() : ViewModel() {
 *     fun updateLanguage(languageCode: String) {
 *         setLanguagePreference(languageCode)
 *         // Activities will be recreated automatically
 *     }
 * }
 * ```
 *
 * ## Usage in Settings Screen
 * ```kotlin
 * @Composable
 * fun LanguageSelector(onLanguageSelected: (String) -> Unit) {
 *     val languages = listOf(
 *         "en" to "English",
 *         "es" to "Español",
 *         "fr" to "Français",
 *         "de" to "Deutsch"
 *     )
 *
 *     languages.forEach { (code, name) ->
 *         TextButton(onClick = { onLanguageSelected(code) }) {
 *             Text(name)
 *         }
 *     }
 * }
 * ```
 *
 * ## Behavior
 * - Persists across app launches (stored by system)
 * - Triggers activity recreation to apply new locale
 * - Affects all string resources and date/time formatting
 * - Independent of system language setting (API 33+)
 *
 * ## Testing Language Changes
 * ```kotlin
 * @Test
 * fun testLanguageChange() {
 *     setLanguagePreference("es")
 *     assertEquals("es", getPreferredLocale().language)
 * }
 * ```
 *
 * @param languageCode IETF BCP 47 language tag (e.g., "en", "es-MX", "zh-Hans")
 *
 * @see getPreferredLocale
 * @see AppCompatDelegate.setApplicationLocales
 */
fun setLanguagePreference(languageCode: String) {
    val appLocale = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(appLocale)
}
