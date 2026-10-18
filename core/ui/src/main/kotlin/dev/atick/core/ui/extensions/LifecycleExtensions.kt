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

package dev.atick.core.ui.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.atick.core.utils.OneTimeEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Observes a LiveData and executes an action when non-null values are emitted.
 *
 * This extension provides a cleaner syntax for LiveData observation with automatic
 * null filtering. The action is only invoked when the LiveData emits a non-null value.
 *
 * ## Illustrative Fragment usage (ViewModel and binding supplied by the caller)
 * ```kotlin
 * class MyFragment : Fragment() {
 *     private val viewModel: MyViewModel by viewModels()
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *
 *         viewLifecycleOwner.observe(viewModel.userData) { user ->
 *             binding.userName.text = user.name
 *             binding.userEmail.text = user.email
 *         }
 *     }
 * }
 * ```
 *
 * ## Modern Alternative (Recommended)
 * Prefer StateFlow with `collectWithLifecycle` for new code:
 * ```kotlin
 * collectWithLifecycle(viewModel.userStateFlow) { user ->
 *     // Handle user update
 * }
 * ```
 *
 * @param T The type of data emitted by the LiveData
 * @param liveData The LiveData to observe
 * @param action Callback invoked with non-null values
 *
 * @see LiveData
 * @see collectWithLifecycle
 */
inline fun <T> LifecycleOwner.observe(
    liveData: LiveData<T>,
    crossinline action: (T) -> Unit,
) {
    liveData.observe(this) { value ->
        value?.let { action(value) }
    }
}

/**
 * Observes a LiveData containing OneTimeEvents and consumes unhandled events.
 *
 * Consumption is at most once per [OneTimeEvent] instance. The content is marked consumed
 * before [action] runs; an exception in the action does not make the event available again.
 * A newly created event has its own consumption flag, and the flag is not persisted across
 * process death. This helper does not acknowledge completed navigation or snackbar display.
 *
 * Illustrative Fragment excerpt; ViewModel, binding, and Material Snackbar imports are omitted.
 * Register in `onViewCreated` with the view's lifecycle owner:
 * ```kotlin
 * viewLifecycleOwner.observeEvent(viewModel.snackbarMessage) { message ->
 *     Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
 * }
 * ```
 *
 * @param T The type of event data
 * @param liveData LiveData containing OneTimeEvents
 * @param action Callback invoked with unhandled event content
 *
 * @see OneTimeEvent
 * @see LiveData
 */
inline fun <T> LifecycleOwner.observeEvent(
    liveData: LiveData<OneTimeEvent<T>>,
    crossinline action: (T) -> Unit,
) {
    liveData.observe(this) {
        it?.getContentIfNotHandled()?.let(action)
    }
}

/**
 * Observes a MutableLiveData containing OneTimeEvents and consumes unhandled events.
 *
 * This is an overload of [observeEvent] for MutableLiveData. Functionally identical
 * to the LiveData variant but accepts MutableLiveData directly.
 *
 * @param T The type of event data
 * @param liveData MutableLiveData containing OneTimeEvents
 * @param action Callback invoked with unhandled event content
 *
 * @see OneTimeEvent
 * @see observeEvent
 */
inline fun <T> LifecycleOwner.observeEvent(
    liveData: MutableLiveData<OneTimeEvent<T>>,
    crossinline action: (T) -> Unit,
) {
    liveData.observe(this) {
        it?.getContentIfNotHandled()?.let(action)
    }
}

/**
 * Collects a Flow in a lifecycle-aware manner, automatically canceling when lifecycle stops.
 *
 * This extension collects the Flow when the lifecycle is at least STARTED and
 * automatically cancels collection when the lifecycle falls below STARTED. This
 * restarts collection on a later STARTED transition. Separately launched upstream jobs have
 * their own lifetimes; this helper does not cancel those jobs. In a Fragment, use
 * `viewLifecycleOwner` when updating its views.
 *
 * ## Illustrative Fragment usage (ViewModel and binding supplied by the caller)
 * ```kotlin
 * class MyFragment : Fragment() {
 *     private val viewModel: MyViewModel by viewModels()
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *
 *         viewLifecycleOwner.collectWithLifecycle(viewModel.uiState) { state ->
 *             binding.progressBar.isVisible = state.loading
 *             binding.errorText.text = state.error
 *         }
 *     }
 * }
 * ```
 *
 * ## Multiple Flows
 * ```kotlin
 * collectWithLifecycle(viewModel.uiState) { state ->
 *     updateUI(state)
 * }
 *
 * collectWithLifecycle(viewModel.events) { event ->
 *     handleEvent(event)
 * }
 * ```
 *
 * ## In Compose (Alternative)
 * For Composables, use `collectAsStateWithLifecycle()` instead:
 * ```kotlin
 * @Composable
 * fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *     // Use uiState...
 * }
 * ```
 *
 * ## Lifecycle Behavior
 * - Collection starts when lifecycle reaches STARTED
 * - Collection is cancelled when lifecycle falls below STARTED (e.g., app backgrounded)
 * - A new collection starts when lifecycle reaches STARTED again
 * - Collection stops permanently when lifecycle is DESTROYED
 *
 * @param T The type of data emitted by the Flow
 * @param flow The Flow to collect
 * @param action Callback invoked with each non-null emitted value
 *
 * @see Flow
 * @see Lifecycle.State.STARTED
 * @see repeatOnLifecycle
 */
inline fun <T> LifecycleOwner.collectWithLifecycle(
    flow: Flow<T>,
    crossinline action: (T) -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect {
                it?.let { action(it) }
            }
        }
    }
}
