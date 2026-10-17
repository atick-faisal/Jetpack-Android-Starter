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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.atick.core.utils.OneTimeEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Renders [UiState.data], a loading indicator, and an error snackbar callback.
 *
 * The error is consumed during composition, before the callback completes. Recomposition can
 * cancel an unfinished callback; a replacement error under the same callback key can be consumed
 * without starting another callback. Consumption does not acknowledge successful delivery.
 *
 * Screens collect their ViewModel state with `collectAsStateWithLifecycle` and pass it here.
 * See the state-management guide for subscription lifetimes and event-delivery limitations.
 *
 * @param T The non-null screen-data type; its properties may be nullable.
 * @param state Data, loading flag, and error event to render.
 * @param onShowSnackbar Callback invoked with the error message, REPORT action, and throwable.
 *                       Its Boolean return value is ignored by this wrapper.
 * @param content Content rendered with the current data, including while loading.
 * @see UiState
 */
// This is a state/error wrapper, not a layout node, so it has nothing to apply a Modifier to.
@Suppress("ktlint:compose:modifier-missing-check")
@Composable
fun <T : Any> StatefulComposable(
    state: UiState<T>,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    content: @Composable (T) -> Unit,
) {
    content(state.data)

    if (state.loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            ContainedLoadingIndicator(
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.Center),
            )
        }
    }

    // Consumption happens before the effect; it does not confirm snackbar delivery.
    state.error.getContentIfNotHandled()?.let { error ->
        LaunchedEffect(onShowSnackbar) {
            onShowSnackbar(error.message.toString(), SnackbarAction.REPORT, error)
        }
    }
}

/**
 * Screen data with a loading flag and a consumable error event.
 *
 * [updateState] replaces data synchronously; [updateStateWith] replaces it on returned success;
 * [updateWith] preserves it. The wrapper has read-only properties, but [OneTimeEvent] has mutable
 * consumption state. Copying a wrapper retains that event unless it is explicitly replaced.
 *
 * @param T The non-null screen-data type; individual fields may be nullable.
 * @property data Current renderable data, retained while an operation is loading or returns failure.
 * @property loading Flag used by [StatefulComposable] to show a loading indicator.
 * @property error Error event, initially containing null. Consumption does not confirm display.
 */
data class UiState<T : Any>(
    val data: T,
    val loading: Boolean = false,
    val error: OneTimeEvent<Throwable?> = OneTimeEvent(null),
)

/**
 * Replaces [UiState.data] synchronously, preserving loading and the existing error event.
 *
 * For example, an Item form updates a field with `updateState { copy(jetpackName = name) }`.
 * The transform runs through [MutableStateFlow.update] and may be evaluated more than once under
 * contention, so keep it free of side effects. Exceptions propagate to the caller.
 *
 * @param T The screen-data type.
 * @param update Transform receiving current data and returning its replacement.
 * @see updateStateWith
 * @see updateWith
 */
inline fun <T : Any> MutableStateFlow<UiState<T>>.updateState(update: T.() -> T) {
    // Preserve loading and the existing error event while replacing data.
    update { it.copy(data = update(it.data)) }
}

/**
 * Launches an operation in the context ViewModel's scope and replaces data on returned success.
 *
 * A call observing `loading = true` is dropped. The flag is set inside the launched coroutine,
 * so multiple calls queued before it runs can overlap. Starting clears the previous error event.
 * Returned failure preserves current data and stores its throwable in [OneTimeEvent]; returned
 * success replaces data. Both paths clear loading. A successful null payload is treated as an error.
 *
 * Thrown exceptions and cancellation are not caught and can leave loading true. The operation's
 * receiver is a data snapshot read inside the coroutine; returning a copy of it can overwrite
 * edits made while the operation was suspended. This helper returns no operation result or Job.
 *
 * @param T The non-null screen-data type.
 * @param operation Suspend transform returning replacement data or a failure in [Result].
 * @see updateState
 * @see updateWith
 */
context(viewModel: ViewModel) inline fun <reified T : Any> MutableStateFlow<UiState<T>>.updateStateWith(
    crossinline operation: suspend T.() -> Result<T>,
) {
    if (value.loading) return
    viewModel.viewModelScope.launch {
        update { it.copy(loading = true, error = OneTimeEvent(null)) }

        val result = value.data.operation()

        if (result.isSuccess) {
            val data = result.getOrNull()
            if (data != null) {
                update { it.copy(data = data, loading = false) }
            } else {
                // Reject a null success payload for the non-null screen-data type.
                update {
                    it.copy(
                        loading = false,
                        error = OneTimeEvent(
                            IllegalStateException("Operation succeeded but returned no data"),
                        ),
                    )
                }
            }
        } else {
            update {
                it.copy(
                    error = OneTimeEvent(result.exceptionOrNull()),
                    loading = false,
                )
            }
        }
    }
}

/**
 * Launches a Result-returning action in the context ViewModel's scope without replacing data.
 *
 * A call observing `loading = true` is dropped; calls queued before the coroutine sets that flag
 * can overlap. Starting clears the old error event. Returned success clears loading; returned
 * failure also stores its throwable in [OneTimeEvent]. Current data is preserved on both paths.
 *
 * Thrown exceptions and cancellation are not caught and can leave loading true. The operation
 * receives a data snapshot, but completion does not write it back. This helper returns no result
 * or Job. Use it, for example, when deleting an item through a repository returning `Result<Unit>`.
 *
 * @param T The screen-data type.
 * @param operation Suspend action receiving current data and returning [Result] of [Unit].
 * @see updateState
 * @see updateStateWith
 */
context(viewModel: ViewModel) inline fun <T : Any> MutableStateFlow<UiState<T>>.updateWith(
    crossinline operation: suspend T.() -> Result<Unit>,
) {
    if (value.loading) return
    viewModel.viewModelScope.launch {
        update { it.copy(loading = true, error = OneTimeEvent(null)) }

        val result = value.data.operation()

        if (result.isSuccess) {
            update { it.copy(loading = false) }
        } else {
            update {
                it.copy(
                    error = OneTimeEvent(result.exceptionOrNull()),
                    loading = false,
                )
            }
        }
    }
}
