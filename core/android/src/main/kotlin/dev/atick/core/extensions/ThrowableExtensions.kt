/*
 * Copyright 2024 Atick Faisal
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

package dev.atick.core.extensions

import dev.atick.core.utils.OneTimeEvent

/**
 * Converts the stack trace of this Throwable into a formatted string.
 *
 * Useful for logging errors in a readable format or displaying detailed error information
 * during development/debugging.
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // In error logging
 * try {
 *     riskyOperation()
 * } catch (e: Exception) {
 *     Log.e("MyApp", "Operation failed:\n${e.getStackTraceString()}")
 * }
 *
 * // In crash reporting
 * fun reportCrash(error: Throwable) {
 *     crashReporter.log(
 *         message = error.message ?: "Unknown error",
 *         stackTrace = error.getStackTraceString()
 *     )
 * }
 *
 * // For debugging in development builds
 * if (BuildConfig.DEBUG) {
 *     println("Detailed error:\n${error.getStackTraceString()}")
 * }
 * ```
 *
 * @receiver Throwable The throwable whose stack trace to format.
 * @return A multi-line string containing the formatted stack trace, with each frame on a new line.
 *
 * @see java.lang.Throwable.getStackTrace
 */
fun Throwable.getStackTraceString(): String {
    return stackTrace.joinToString("\n")
}

/**
 * Wraps this Throwable in a OneTimeEvent for use in UI state management.
 *
 * This is useful when you want to ensure errors are only shown once to the user,
 * even if the state is re-collected (e.g., during configuration changes).
 *
 * **Note:** The template uses `UiState` wrapper which already includes OneTimeEvent for errors.
 * This function is mainly useful if you're managing errors separately or building custom state.
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // In a custom state class (if not using UiState)
 * data class MyScreenState(
 *     val data: List<Item> = emptyList(),
 *     val error: OneTimeEvent<Throwable?> = OneTimeEvent(null)
 * )
 *
 * // In a ViewModel
 * private val _state = MutableStateFlow(MyScreenState())
 *
 * fun loadData() {
 *     viewModelScope.launch {
 *         try {
 *             val data = repository.getData()
 *             _state.update { it.copy(data = data) }
 *         } catch (e: Exception) {
 *             _state.update { it.copy(error = e.asOneTimeEvent()) }
 *         }
 *     }
 * }
 *
 * // In UI - error only shown once
 * val state by viewModel.state.collectAsStateWithLifecycle()
 * state.error.consume { error ->
 *     // This block runs only once per error, even after rotation
 *     showSnackbar(error.message ?: "Unknown error")
 * }
 * ```
 *
 * @receiver Throwable The error to wrap in a one-time event.
 * @return A OneTimeEvent containing this throwable.
 *
 * @see dev.atick.core.utils.OneTimeEvent
 * @see dev.atick.core.ui.utils.UiState For the recommended state wrapper pattern
 */
fun Throwable.asOneTimeEvent(): OneTimeEvent<Throwable?> {
    return OneTimeEvent(this)
}
