/*
 * Copyright 2026 Atick Faisal
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

import androidx.lifecycle.ViewModel
import com.google.common.truth.Truth.assertThat
import dev.atick.core.testing.rule.MainDispatcherRule
import dev.atick.core.utils.OneTimeEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * These helpers are the state-management contract every ViewModel in the project is written
 * against, so their edge cases — the re-entry guard, error routing, what survives a failure —
 * are the most load-bearing behaviour in the codebase.
 *
 * [MainDispatcherRule] installs an `UnconfinedTestDispatcher` as Main, so work launched into
 * `viewModelScope` runs eagerly up to its first suspension point. That makes these assertions
 * read in call order without any scheduler nudging.
 */
class StatefulComposableTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private data class Screen(val name: String = "", val count: Int = 0)

    /** The helpers take a ViewModel as a context parameter, so tests need a real one. */
    private class TestViewModel : ViewModel()

    // region updateState

    @Test
    fun `updateState applies the transform to data`() {
        val state = MutableStateFlow(UiState(Screen(name = "before")))

        state.updateState { copy(name = "after") }

        assertThat(state.value.data.name).isEqualTo("after")
    }

    @Test
    fun `updateState preserves the loading flag`() {
        val state = MutableStateFlow(UiState(Screen(), loading = true))

        state.updateState { copy(name = "typed") }

        // Regression: updateState used to build a fresh UiState rather than copy the existing
        // one, so a synchronous field update silently cleared an in-flight loading indicator.
        assertThat(state.value.loading).isTrue()
    }

    @Test
    fun `updateState preserves an unconsumed error`() {
        val boom = IllegalStateException("boom")
        val state = MutableStateFlow(UiState(Screen(), error = OneTimeEvent<Throwable?>(boom)))

        state.updateState { copy(name = "typed") }

        // Regression: the error event used to be dropped, so an error raised while the user was
        // still typing never reached the snackbar.
        assertThat(state.value.error.getContentIfNotHandled()).isSameInstanceAs(boom)
    }

    // endregion

    // region updateStateWith

    @Test
    fun `updateStateWith replaces data and clears loading on success`() = runTest {
        val viewModel = TestViewModel()
        val state = MutableStateFlow(UiState(Screen(name = "before")))

        with(viewModel) {
            state.updateStateWith { Result.success(Screen(name = "after", count = 1)) }
        }

        assertThat(state.value.data).isEqualTo(Screen(name = "after", count = 1))
        assertThat(state.value.loading).isFalse()
        assertThat(state.value.error.getContentIfNotHandled()).isNull()
    }

    @Test
    fun `updateStateWith keeps the old data and surfaces the error on failure`() = runTest {
        val viewModel = TestViewModel()
        val boom = IllegalStateException("boom")
        val state = MutableStateFlow(UiState(Screen(name = "before")))

        with(viewModel) {
            state.updateStateWith { Result.failure<Screen>(boom) }
        }

        assertThat(state.value.data.name).isEqualTo("before")
        assertThat(state.value.loading).isFalse()
        assertThat(state.value.error.getContentIfNotHandled()).isSameInstanceAs(boom)
    }

    @Test
    fun `updateStateWith sets loading while the operation is in flight`() = runTest {
        val viewModel = TestViewModel()
        val state = MutableStateFlow(UiState(Screen()))
        val gate = CompletableDeferred<Result<Screen>>()

        with(viewModel) {
            state.updateStateWith { gate.await() }
        }

        // The coroutine has run up to gate.await() and parked there.
        assertThat(state.value.loading).isTrue()

        gate.complete(Result.success(Screen(name = "done")))

        assertThat(state.value.loading).isFalse()
        assertThat(state.value.data.name).isEqualTo("done")
    }

    @Test
    fun `updateStateWith ignores a second call while one is already running`() = runTest {
        val viewModel = TestViewModel()
        val state = MutableStateFlow(UiState(Screen()))
        val gate = CompletableDeferred<Result<Screen>>()
        var invocations = 0

        with(viewModel) {
            state.updateStateWith {
                invocations++
                gate.await()
            }
        }

        // Second call arrives while loading == true and must be dropped, not queued. This is
        // what stops a double tap firing two network requests.
        with(viewModel) {
            state.updateStateWith {
                invocations++
                Result.success(Screen(name = "second"))
            }
        }

        gate.complete(Result.success(Screen(name = "first")))

        assertThat(invocations).isEqualTo(1)
        assertThat(state.value.data.name).isEqualTo("first")
    }

    @Test
    fun `updateStateWith reports an error when a successful result carries no data`() = runTest {
        val viewModel = TestViewModel()
        val state = MutableStateFlow(UiState(Screen(name = "before")))

        // Screen is non-null, so only an unchecked cast can reach this defensive branch.
        // Covered anyway: it is what the helper falls back to if a repository ever hands back
        // Result.success(null) through a platform type.
        @Suppress("UNCHECKED_CAST")
        val nullSuccess = Result.success(null) as Result<Screen>

        with(viewModel) {
            state.updateStateWith { nullSuccess }
        }

        assertThat(state.value.loading).isFalse()
        assertThat(state.value.error.getContentIfNotHandled())
            .isInstanceOf(IllegalStateException::class.java)
    }

    // endregion

    // region updateWith

    @Test
    fun `updateWith clears loading and leaves data untouched on success`() = runTest {
        val viewModel = TestViewModel()
        val state = MutableStateFlow(UiState(Screen(name = "kept", count = 3)))

        with(viewModel) {
            state.updateWith { Result.success(Unit) }
        }

        assertThat(state.value.data).isEqualTo(Screen(name = "kept", count = 3))
        assertThat(state.value.loading).isFalse()
        assertThat(state.value.error.getContentIfNotHandled()).isNull()
    }

    @Test
    fun `updateWith surfaces the error and keeps data on failure`() = runTest {
        val viewModel = TestViewModel()
        val boom = IllegalStateException("boom")
        val state = MutableStateFlow(UiState(Screen(name = "kept")))

        with(viewModel) {
            state.updateWith { Result.failure(boom) }
        }

        assertThat(state.value.data.name).isEqualTo("kept")
        assertThat(state.value.loading).isFalse()
        assertThat(state.value.error.getContentIfNotHandled()).isSameInstanceAs(boom)
    }

    @Test
    fun `updateWith ignores a second call while one is already running`() = runTest {
        val viewModel = TestViewModel()
        val state = MutableStateFlow(UiState(Screen()))
        val gate = CompletableDeferred<Result<Unit>>()
        var invocations = 0

        with(viewModel) {
            state.updateWith {
                invocations++
                gate.await()
            }
        }
        with(viewModel) {
            state.updateWith {
                invocations++
                Result.success(Unit)
            }
        }

        gate.complete(Result.success(Unit))

        assertThat(invocations).isEqualTo(1)
    }

    // endregion

    @Test
    fun `UiState defaults to not loading with no error`() {
        val state = UiState(Screen())

        assertThat(state.loading).isFalse()
        assertThat(state.error.peekContent()).isNull()
    }
}
