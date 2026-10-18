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
 * Settings maps this locale to its `Language` selection.
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
 * Delegates to [AppCompatDelegate.setApplicationLocales]. Locale application and persistence
 * depend on the Android version and AppCompat configuration; this function does not update a
 * DataStore preference or make arbitrary date/number formatters use the new locale.
 *
 * The app currently offers English and Arabic. To extend the picker, update resources, the
 * language model, options and locale mapping together; see
 * [localization](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/components.md#localization).
 * The manifest enables AppCompat locale storage, and the app generates its locale configuration
 * from resources. Verify selection, persistence and recreation on pre-33 and 33+ devices.
 *
 * Existing SettingsViewModel method excerpt; imports and containing class omitted:
 * ```kotlin
 * fun updateLanguagePreference(language: Language) {
 *     setLanguagePreference(language.code)
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
