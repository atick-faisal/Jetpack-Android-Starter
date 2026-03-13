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
 * A centralized composable wrapper that handles loading states, error handling, and content display.
 *
 * This composable implements the standard UI state pattern used throughout the application:
 * - Displays content based on the current state data
 * - Shows an overlay loading indicator when [UiState.loading] is true
 * - Automatically displays errors via snackbar when [UiState.error] contains an unhandled exception
 *
 * ## Usage Example
 * ```kotlin
 * @Composable
 * fun FeatureRoute(
 *     onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
 *     viewModel: FeatureViewModel = hiltViewModel()
 * ) {
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *
 *     StatefulComposable(
 *         state = uiState,
 *         onShowSnackbar = onShowSnackbar
 *     ) { screenData ->
 *         FeatureScreen(
 *             screenData = screenData,
 *             onAction = viewModel::handleAction
 *         )
 *     }
 * }
 * ```
 *
 * @sample dev.atick.feature.home.ui.home.HomeScreen Standard usage with repository data observation
 * @sample dev.atick.feature.auth.ui.signin.SignInScreen Usage with form validation
 *
 * ## Pattern
 * This composable enforces a clean separation of concerns:
 * 1. **Route composable**: Manages ViewModel and state collection (uses StatefulComposable)
 * 2. **Screen composable**: Pure UI that receives data and event callbacks
 *
 * @param T The type of the screen data. Must be a non-nullable type.
 * @param state The current UI state containing data, loading flag, and error events.
 * @param onShowSnackbar Callback to display error messages. Returns true if the error was handled.
 * @param content The screen content to display. Receives the current data from [state].
 *
 * @see UiState
 * @see updateState
 * @see updateStateWith
 * @see updateWith
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
            ContainedLoadingIndicator(
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.Center),
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
 * Wrapper class representing the complete state of a UI screen.
 *
 * This is the standard state container used across all ViewModels in the application.
 * It combines screen data with loading and error states in a single, immutable structure.
 *
 * ## Key Features
 * - **Type-safe data**: Generic type parameter ensures compile-time safety
 * - **Loading state**: Boolean flag for showing/hiding loading indicators
 * - **One-time errors**: Errors are delivered as events that are handled only once
 *
 * ## Usage in ViewModel
 * ```kotlin
 * @HiltViewModel
 * class FeatureViewModel @Inject constructor(
 *     private val repository: FeatureRepository
 * ) : ViewModel() {
 *     private val _uiState = MutableStateFlow(UiState(FeatureScreenData()))
 *     val uiState = _uiState.asStateFlow()
 * }
 * ```
 *
 * ## State Updates
 * Use the provided extension functions to update state:
 * - [updateState] for synchronous updates
 * - [updateStateWith] for async operations returning new data
 * - [updateWith] for async operations returning Unit
 *
 * @param T The type of the screen data. Must be a non-nullable type.
 * @property data The current screen data. This is always present (never null).
 * @property loading True when an async operation is in progress. Used to show loading indicators.
 * @property error A one-time event containing an error. Automatically consumed after being handled.
 *
 * @see StatefulComposable
 * @see updateState
 * @see updateStateWith
 * @see updateWith
 * @see OneTimeEvent
 */
data class UiState<T : Any>(
    val data: T,
    val loading: Boolean = false,
    val error: OneTimeEvent<Throwable?> = OneTimeEvent(null),
)

/**
 * Updates the UI state synchronously without showing loading indicators or handling errors.
 *
 * Use this function for immediate, synchronous state updates such as:
 * - Text input changes
 * - UI toggle states
 * - Local UI state modifications
 * - Navigation state updates
 *
 * This function does NOT:
 * - Set loading state
 * - Handle exceptions
 * - Run in a coroutine scope
 *
 * ## Usage Example
 * ```kotlin
 * @HiltViewModel
 * class ProfileViewModel @Inject constructor() : ViewModel() {
 *     private val _uiState = MutableStateFlow(UiState(ProfileScreenData()))
 *     val uiState = _uiState.asStateFlow()
 *
 *     fun updateName(name: String) {
 *         _uiState.updateState {
 *             copy(name = name)
 *         }
 *     }
 * }
 * ```
 *
 * @sample dev.atick.feature.auth.ui.signin.SignInViewModel.updateEmail Real-world form field validation
 * @sample dev.atick.feature.auth.ui.signin.SignInViewModel.updatePassword Form field with validation
 *
 * @param T The type of the screen data.
 * @param update Lambda that receives current data and returns updated data.
 *               Use data class `copy()` to create the new state.
 *
 * @see updateStateWith for async operations that return new data
 * @see updateWith for async operations that return Unit
 */
inline fun <T : Any> MutableStateFlow<UiState<T>>.updateState(update: T.() -> T) {
    update { UiState(update(it.data)) }
}

/**
 * Updates the UI state with the result of an async operation that returns new data.
 *
 * This function automatically handles the complete async operation lifecycle:
 * 1. Sets `loading = true` before starting the operation
 * 2. Executes the operation in the ViewModel's coroutine scope
 * 3. Updates state with new data on success
 * 4. Sets error event on failure
 * 5. Sets `loading = false` when complete
 *
 * Use this function for async operations that fetch or compute new data, such as:
 * - Loading data from a repository
 * - Refreshing screen content
 * - Search operations
 * - Any operation that returns `Result<T>` with new data
 *
 * ## Kotlin Context Parameters
 * This function uses Kotlin's context parameters feature (`context(viewModel: ViewModel)`).
 * The ViewModel context provides implicit access to `viewModelScope` for launching coroutines.
 * You never need to pass the ViewModel explicitly - it's automatically available when called
 * from within a ViewModel.
 *
 * ## Usage Example
 * ```kotlin
 * @HiltViewModel
 * class HomeViewModel @Inject constructor(
 *     private val repository: HomeRepository
 * ) : ViewModel() {
 *     private val _uiState = MutableStateFlow(UiState(HomeScreenData()))
 *     val uiState = _uiState.asStateFlow()
 *
 *     fun loadData() {
 *         _uiState.updateStateWith {
 *             repository.getData() // Returns Result<HomeScreenData>
 *         }
 *     }
 *
 *     fun search(query: String) {
 *         _uiState.updateStateWith {
 *             repository.search(query) // Returns Result<HomeScreenData>
 *         }
 *     }
 * }
 * ```
 *
 * ## Error Handling
 * - Errors are automatically caught and wrapped in [OneTimeEvent]
 * - The error is displayed via [StatefulComposable]'s snackbar mechanism
 * - Original data is preserved when an error occurs
 *
 * ## Duplicate Requests
 * If called while `loading = true`, the function returns immediately to prevent
 * duplicate concurrent operations.
 *
 * @sample dev.atick.feature.home.ui.item.ItemViewModel.createOrUpdateJetpack Creating/updating with navigation event
 *
 * @param T The type of the screen data.
 * @param operation Suspend lambda that receives current data and returns `Result<T>` with new data.
 *                  Typically calls a repository method that returns new screen data.
 *
 * @see updateState for synchronous updates
 * @see updateWith for async operations that don't return new data
 * @see suspendRunCatching for wrapping repository operations in Result
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
 * Updates the UI state with the result of an async operation that performs an action without returning new data.
 *
 * This function automatically handles the complete async operation lifecycle:
 * 1. Sets `loading = true` before starting the operation
 * 2. Executes the operation in the ViewModel's coroutine scope
 * 3. Preserves existing data on success (no data changes)
 * 4. Sets error event on failure
 * 5. Sets `loading = false` when complete
 *
 * Use this function for async operations that perform actions but don't update screen data:
 * - Saving/updating data without changing current view
 * - Deleting items
 * - Sending analytics events
 * - Triggering background jobs
 * - Any operation that returns `Result<Unit>`
 *
 * ## Kotlin Context Parameters
 * This function uses Kotlin's context parameters feature (`context(viewModel: ViewModel)`).
 * The ViewModel context provides implicit access to `viewModelScope` for launching coroutines.
 * You never need to pass the ViewModel explicitly - it's automatically available when called
 * from within a ViewModel.
 *
 * ## Usage Example
 * ```kotlin
 * @HiltViewModel
 * class SettingsViewModel @Inject constructor(
 *     private val repository: SettingsRepository
 * ) : ViewModel() {
 *     private val _uiState = MutableStateFlow(UiState(SettingsScreenData()))
 *     val uiState = _uiState.asStateFlow()
 *
 *     fun saveSettings() {
 *         _uiState.updateWith {
 *             repository.saveSettings(this) // Returns Result<Unit>
 *         }
 *     }
 *
 *     fun deleteAccount() {
 *         _uiState.updateWith {
 *             repository.deleteAccount() // Returns Result<Unit>
 *         }
 *     }
 * }
 * ```
 *
 * @sample dev.atick.feature.home.ui.home.HomeViewModel.deleteJetpack Deleting an item
 * @sample dev.atick.feature.auth.ui.signin.SignInViewModel.signInWithGoogle Authentication with Google
 * @sample dev.atick.feature.auth.ui.signin.SignInViewModel.loginWithEmailAndPassword Email/password authentication
 *
 * ## Difference from updateStateWith
 * - `updateStateWith`: For operations that return new data → Updates [UiState.data]
 * - `updateWith`: For operations that return Unit → Preserves [UiState.data]
 *
 * ## Error Handling
 * - Errors are automatically caught and wrapped in [OneTimeEvent]
 * - The error is displayed via [StatefulComposable]'s snackbar mechanism
 * - Original data is preserved when an error occurs
 *
 * ## Duplicate Requests
 * If called while `loading = true`, the function returns immediately to prevent
 * duplicate concurrent operations.
 *
 * @param T The type of the screen data.
 * @param operation Suspend lambda that receives current data and returns `Result<Unit>`.
 *                  Typically calls a repository method that performs an action.
 *
 * @see updateState for synchronous updates
 * @see updateStateWith for async operations that return new data
 * @see suspendRunCatching for wrapping repository operations in Result
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
