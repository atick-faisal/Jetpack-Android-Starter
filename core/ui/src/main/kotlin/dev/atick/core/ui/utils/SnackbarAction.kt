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
 * Values identify localized labels; the snackbar callback owns the resulting behavior.
 * [StatefulComposable] requests REPORT for errors and ignores the callback's Boolean result.
 * The app's callback reports the throwable only when that action is selected. UNDO has no
 * built-in rollback implementation.
 *
 * ## Illustrative ViewModel excerpt (repository and state declarations omitted)
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
 * ## Illustrative Screen excerpt (imports and content omitted)
 * ```kotlin
 * @Composable
 * fun ExampleScreen(
 *     onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
 *     viewModel: MyViewModel = hiltViewModel()
 * ) {
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *
 *     StatefulComposable(
 *         state = uiState,
 *         onShowSnackbar = onShowSnackbar
 *     ) { screenData ->
 *         ExampleContent(
 *             onDelete = { id ->
 *                 viewModel.deleteItem(id)
 *             }
 *         )
 *     }
 * }
 * ```
 *
 * ## Illustrative custom handler inside a composable
 * Imports and surrounding UI are omitted; resource lookup uses the captured Context
 * because the suspend callback runs outside composition.
 * ```kotlin
 * val snackbarHostState = remember { SnackbarHostState() }
 * val context = LocalContext.current
 *
 * val onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean = { message, action, _ ->
 *     val result = snackbarHostState.showSnackbar(
 *         message = message,
 *         actionLabel = if (action != SnackbarAction.NONE) {
 *             context.getString(action.actionText)
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
     * Empty action label; a custom handler can map this to null to omit the button.
     *
     * Use this for informational messages that don't require user interaction.
     *
     * Example: "Data synced successfully"
     */
    NONE(R.string.core_ui_empty),

    /**
     * Shows a "Report" action button.
     *
     * The app callback forwards the throwable to its crash reporter when this action is selected.
     *
     * Example: "Failed to load data" with REPORT action
     */
    REPORT(R.string.core_ui_report),

    /**
     * Shows an "Undo" action button.
     *
     * The caller must implement the reversal. Home does not currently offer undo.
     *
     * Example: "Item deleted" with UNDO action
     */
    UNDO(R.string.core_ui_undo),
}
