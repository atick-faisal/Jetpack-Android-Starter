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

package dev.atick.core.utils

import java.util.concurrent.atomic.AtomicBoolean

/**
 * A thread-safe wrapper for one-time events that should be consumed only once, even when observed multiple times.
 *
 * This pattern solves a common problem in Android development: preventing events (like showing a snackbar,
 * navigating to another screen, or displaying an error) from being triggered multiple times when the UI
 * is recreated (e.g., due to configuration changes).
 *
 * ## The Problem It Solves
 * When using StateFlow or LiveData, UI recompositions or lifecycle events can cause the same state to be
 * observed multiple times. For events like "show error snackbar", this would result in the same error
 * appearing repeatedly, which is undesirable.
 *
 * ## How It Works
 * - On first access via [getContentIfNotHandled], returns the content and marks it as handled
 * - Subsequent calls to [getContentIfNotHandled] return null
 * - Uses [AtomicBoolean] for thread-safe handling without synchronization
 * - [peekContent] allows inspecting the content without consuming it (useful for debugging/logging)
 *
 * ## Usage in UiState
 * This class is primarily used for error events in [UiState]:
 * ```kotlin
 * data class UiState<T : Any>(
 *     val data: T,
 *     val loading: Boolean = false,
 *     val error: OneTimeEvent<Throwable?> = OneTimeEvent(null)
 * )
 * ```
 *
 * ## Usage in StatefulComposable
 * The [StatefulComposable] automatically handles one-time events:
 * ```kotlin
 * state.error.getContentIfNotHandled()?.let { error ->
 *     LaunchedEffect(onShowSnackbar) {
 *         onShowSnackbar(error.message.toString(), SnackbarAction.REPORT, error)
 *     }
 * }
 * ```
 *
 * ## When to Use
 * Use OneTimeEvent for:
 * - Error messages/snackbars
 * - Navigation events
 * - Showing dialogs
 * - One-time animations
 * - Any event that should happen once per trigger, not once per observation
 *
 * ## When NOT to Use
 * Don't use OneTimeEvent for:
 * - Regular state data (use plain properties)
 * - Loading states (use Boolean)
 * - Data that should persist across recompositions
 *
 * ## Thread Safety
 * This implementation uses [AtomicBoolean.compareAndSet] to ensure thread-safe handling
 * without requiring explicit synchronization, making it safe to use from multiple threads.
 *
 * ## Related Reading
 * Based on the SingleLiveEvent pattern:
 * https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 *
 * @param T The type of the event content. Can be nullable.
 * @property content The event content that will be delivered once.
 *
 * @see UiState
 * @see StatefulComposable
 */
class OneTimeEvent<T>(private val content: T) {
    private var hasBeenHandled = AtomicBoolean(false)

    /**
     * Returns the content if it hasn't been handled yet, then marks it as handled.
     *
     * This is the primary way to consume the event. It guarantees that the content
     * is returned exactly once, even if called from multiple threads concurrently.
     *
     * ## Usage Example
     * ```kotlin
     * val event = OneTimeEvent("Error occurred")
     * val content1 = event.getContentIfNotHandled() // Returns "Error occurred"
     * val content2 = event.getContentIfNotHandled() // Returns null (already handled)
     * ```
     *
     * @return The event content on first call, null on subsequent calls.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled.compareAndSet(false, true)) content else null
    }

    /**
     * Returns the content without marking it as handled.
     *
     * This is useful for debugging, logging, or cases where you need to inspect
     * the event content without consuming it. Use sparingly in production code.
     *
     * ## Usage Example
     * ```kotlin
     * val event = OneTimeEvent("Error occurred")
     * val peeked = event.peekContent() // Returns "Error occurred"
     * val content = event.getContentIfNotHandled() // Still returns "Error occurred"
     * ```
     *
     * @return The event content, regardless of whether it has been handled.
     */
    fun peekContent(): T = content
}
