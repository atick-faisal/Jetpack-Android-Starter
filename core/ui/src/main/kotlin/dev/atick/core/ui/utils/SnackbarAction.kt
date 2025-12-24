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

import androidx.annotation.StringRes
import dev.atick.core.ui.R

/**
 * Represents different action types that can be attached to a Snackbar message.
 *
 * This enum provides standardized action buttons for Snackbars, enabling users to respond
 * to notifications or messages. Actions are automatically handled by [StatefulComposable]
 * when errors occur, and can be used in custom snackbar implementations.
 *
 * ## Usage in ViewModels
 * ```kotlin
 * @HiltViewModel
 * class MyViewModel @Inject constructor(
 *     private val repository: MyRepository
 * ) : ViewModel() {
 *     fun deleteItem(id: String) {
 *         _uiState.updateWith {
 *             repository.deleteItem(id)
 *         }
 *     }
 * }
 * ```
 *
 * ## Usage in UI Layer
 * ```kotlin
 * @Composable
 * fun MyRoute(
 *     onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
 *     viewModel: MyViewModel = hiltViewModel()
 * ) {
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *
 *     StatefulComposable(
 *         state = uiState,
 *         onShowSnackbar = onShowSnackbar
 *     ) { screenData ->
 *         MyScreen(
 *             onDelete = { id ->
 *                 viewModel.deleteItem(id)
 *             }
 *         )
 *     }
 * }
 * ```
 *
 * ## Custom Snackbar Handler
 * ```kotlin
 * val snackbarHostState = remember { SnackbarHostState() }
 *
 * val onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean = { message, action, _ ->
 *     val result = snackbarHostState.showSnackbar(
 *         message = message,
 *         actionLabel = if (action != SnackbarAction.NONE) {
 *             stringResource(action.actionText)
 *         } else {
 *             null
 *         },
 *         duration = SnackbarDuration.Short
 *     )
 *     result == SnackbarResult.ActionPerformed
 * }
 * ```
 *
 * @property actionText String resource ID for the action button text
 *
 * @see StatefulComposable
 */
enum class SnackbarAction(@StringRes val actionText: Int) {
    /**
     * No action button is shown on the Snackbar.
     *
     * Use this for informational messages that don't require user interaction.
     *
     * Example: "Data synced successfully"
     */
    NONE(R.string.empty),

    /**
     * Shows a "Report" action button.
     *
     * Use this to allow users to report errors or issues. The action typically
     * navigates to a bug report screen or opens an email client.
     *
     * Example: "Failed to load data" with REPORT action
     */
    REPORT(R.string.report),

    /**
     * Shows an "Undo" action button.
     *
     * Use this for destructive operations that can be reversed, such as
     * deleting items or dismissing notifications.
     *
     * Example: "Item deleted" with UNDO action
     */
    UNDO(R.string.undo),
}
