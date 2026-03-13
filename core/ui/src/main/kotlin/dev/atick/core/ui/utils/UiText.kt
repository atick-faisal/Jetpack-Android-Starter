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

package dev.atick.core.ui.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Represents text that can be either a string resource (localized) or a dynamic string (runtime).
 *
 * This sealed class ensures type-safe handling of text throughout the application,
 * particularly useful for passing text from business logic to UI without requiring Android Context.
 *
 * @see DynamicString For runtime-generated strings
 * @see StringResource For localized string resources
 */
sealed class UiText {
    /**
     * Represents a dynamic string generated at runtime.
     *
     * Use this for:
     * - User-generated content (names, messages, etc.)
     * - API responses (error messages from server)
     * - Formatted numbers or dates
     * - Any text that isn't localized
     *
     * ## Examples
     * ```kotlin
     * UiText.DynamicString("Welcome, ${user.name}!")
     * UiText.DynamicString(apiError.message)
     * UiText.DynamicString("${count} items")
     * ```
     *
     * @property value The string content to display.
     */
    data class DynamicString(val value: String) : UiText()

    /**
     * Represents a localized string resource with optional formatting arguments.
     *
     * Use this for:
     * - All user-facing static text
     * - Error messages that should be localized
     * - Labels, titles, descriptions
     * - Any text that needs translation
     *
     * ## Examples
     * ```kotlin
     * // Simple string resource
     * UiText.StringResource(R.string.app_name)
     *
     * // With single argument
     * UiText.StringResource(R.string.welcome_user, userName)
     *
     * // With multiple arguments
     * UiText.StringResource(
     *     R.string.items_count_format,  // "%d of %d items"
     *     currentCount,
     *     totalCount
     * )
     * ```
     *
     * @property resId The string resource ID from R.string.
     * @property args Optional formatting arguments for string placeholders (%s, %d, etc.).
     */
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any,
    ) : UiText()

    /**
     * Resolves this UiText to a String within a Composable context.
     *
     * This is the preferred method for resolving UiText in Compose UI, as it properly
     * handles configuration changes and recomposition.
     *
     * @return The resolved string value.
     */
    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    /**
     * Resolves this UiText to a String using an Android Context.
     *
     * Use this method in non-Composable code such as:
     * - WorkManager workers
     * - Notification builders
     * - Service classes
     * - ViewModels (when absolutely necessary)
     *
     * **Note:** Prefer the Composable `asString()` variant in UI code.
     *
     * @param context Android context for resolving string resources.
     * @return The resolved string value.
     */
    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}
