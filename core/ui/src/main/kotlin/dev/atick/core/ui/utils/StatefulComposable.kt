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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.atick.core.ui.components.JetpackOverlayLoadingWheel
import dev.atick.core.utils.OneTimeEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * A composable function that represents a stateful UI component.
 *
 * @param T The type of the data.
 * @param state The current state of the UI.
 * @param onShowSnackbar A suspend function to show a snackbar with a message and an action.
 * @param content A composable function that defines the UI content based on the state data.
 */
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
            JetpackOverlayLoadingWheel(
                modifier = Modifier
                    .align(Alignment.Center),
                contentDesc = "",
            )
        }
    }

    state.error.getContentIfNotHandled()?.let { error ->
        LaunchedEffect(onShowSnackbar) {
            onShowSnackbar(error.message.toString(), SnackbarAction.REPORT, error)
        }
    }
}

/**
 * Data class representing the state of the UI.
 *
 * @param T The type of the data.
 * @property data The current data of the UI.
 * @property loading A flag indicating whether the UI is in a loading state.
 * @property error An event representing an error that may have occurred.
 */
data class UiState<T : Any>(
    val data: T,
    val loading: Boolean = false,
    val error: OneTimeEvent<Throwable?> = OneTimeEvent(null),
)

/**
 * Extension function to update the state of a MutableStateFlow.
 *
 * @param T The type of the data.
 * @param update A function to update the data.
 */
inline fun <T : Any> MutableStateFlow<UiState<T>>.updateState(update: T.() -> T) {
    update { UiState(update(it.data)) }
}

/**
 * Extension function to update the state of a MutableStateFlow with a suspend operation.
 *
 * @param T The type of the data.
 * @param scope The CoroutineScope to launch the operation.
 * @param operation A suspend function that returns a Result of the data.
 */
inline fun <reified T : Any> MutableStateFlow<UiState<T>>.updateStateWith(
    scope: CoroutineScope,
    crossinline operation: suspend T.() -> Result<T>,
) {
    if (value.loading) return
    scope.launch {
        update { it.copy(loading = true, error = OneTimeEvent(null)) }

        val result = value.data.operation()

        if (result.isSuccess) {
            val data = result.getOrNull()
            if (data != null) {
                update { it.copy(data = data, loading = false) }
            } else {
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
 * Extension function to update the state of a MutableStateFlow with a suspend operation that returns Unit.
 *
 * @param T The type of the data.
 * @param scope The CoroutineScope to launch the operation.
 * @param operation A suspend function that returns a Result of Unit.
 */
inline fun <T : Any> MutableStateFlow<UiState<T>>.updateWith(
    scope: CoroutineScope,
    crossinline operation: suspend T.() -> Result<Unit>,
) {
    if (value.loading) return
    scope.launch {
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

/**
 * Extension function to create a new instance of a class as a UiState.
 *
 * @param T The type of the data.
 * @return A new instance of the class as a UiState.
 */
fun <T : Any> KClass<T>.asUiState() = UiState(data = createInstance())
