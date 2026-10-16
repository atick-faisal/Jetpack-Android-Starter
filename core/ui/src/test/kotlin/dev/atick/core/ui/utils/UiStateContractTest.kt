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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/** Characterizes limitations for the docs review; these assertions do not endorse the behavior. */
@OptIn(ExperimentalCoroutinesApi::class)
class UiStateContractTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private data class Screen(val name: String = "before")
    private class ScopedViewModel(scope: CoroutineScope) : ViewModel(scope)

    @Test
    fun `both helpers can start overlapping calls before loading becomes true`() = runTest {
        for (replaceData in listOf(true, false)) {
            val scope = CoroutineScope(SupervisorJob() + mainDispatcherRule.testDispatcher)
            try {
                val viewModel = ScopedViewModel(scope)
                val state = MutableStateFlow(UiState(Screen()))
                val gate = CompletableDeferred<Unit>()
                var calls = 0
                with(viewModel) {
                    repeat(2) {
                        if (replaceData) {
                            state.updateStateWith {
                                calls++
                                gate.await()
                                Result.success(this)
                            }
                        } else {
                            state.updateWith {
                                calls++
                                gate.await()
                                Result.success(Unit)
                            }
                        }
                    }
                }
                assertThat(state.value.loading).isFalse()
                runCurrent()
                assertThat(calls).isEqualTo(2)
                assertThat(state.value.loading).isTrue()
                gate.complete(Unit)
                runCurrent()
                assertThat(state.value.loading).isFalse()
            } finally {
                scope.cancel()
            }
        }
    }

    @Test
    fun `thrown exceptions escape both helpers and leave loading set`() = runTest {
        val failure = IllegalStateException("operation threw")
        assertInterruptedOperation(failure)
    }

    @Test
    fun `cancellation escapes both helpers without clearing loading`() = runTest {
        assertInterruptedOperation(CancellationException("operation cancelled"))
    }

    private suspend fun TestScope.assertInterruptedOperation(failure: Throwable) {
        for (replaceData in listOf(true, false)) {
            val errors = mutableListOf<Throwable>()
            // A separate supervised scope lets us inspect uncaught errors without failing runTest.
            val scope = CoroutineScope(
                SupervisorJob() + mainDispatcherRule.testDispatcher +
                    CoroutineExceptionHandler { _, error -> errors += error },
            )
            try {
                val viewModel = ScopedViewModel(scope)
                val state = MutableStateFlow(UiState(Screen()))
                with(viewModel) {
                    if (replaceData) {
                        state.updateStateWith { throw failure }
                    } else {
                        state.updateWith { throw failure }
                    }
                }
                runCurrent()
                assertThat(state.value.loading).isTrue()
                assertThat(state.value.data).isEqualTo(Screen())
                assertThat(state.value.error.peekContent()).isNull()
                if (failure is CancellationException) {
                    assertThat(errors).isEmpty()
                } else {
                    assertThat(errors).containsExactly(failure)
                }
            } finally {
                scope.cancel()
            }
        }
    }

    @Test
    fun `successful data replacement overwrites edits made while suspended`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + mainDispatcherRule.testDispatcher)
        try {
            val viewModel = ScopedViewModel(scope)
            val state = MutableStateFlow(UiState(Screen()))
            val gate = CompletableDeferred<Unit>()
            with(viewModel) {
                state.updateStateWith {
                    gate.await()
                    Result.success(copy(name = "saved $name"))
                }
            }
            runCurrent()
            state.updateState { copy(name = "typed while saving") }
            assertThat(state.value.loading).isTrue()
            gate.complete(Unit)
            runCurrent()
            assertThat(state.value.data.name).isEqualTo("saved before")
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `unit operation preserves edits made while suspended`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + mainDispatcherRule.testDispatcher)
        try {
            val viewModel = ScopedViewModel(scope)
            val state = MutableStateFlow(UiState(Screen()))
            val gate = CompletableDeferred<Unit>()
            with(viewModel) {
                state.updateWith {
                    gate.await()
                    Result.success(Unit)
                }
            }
            runCurrent()
            state.updateState { copy(name = "typed while saving") }
            gate.complete(Unit)
            runCurrent()
            assertThat(state.value.data.name).isEqualTo("typed while saving")
            assertThat(state.value.loading).isFalse()
        } finally {
            scope.cancel()
        }
    }
}
