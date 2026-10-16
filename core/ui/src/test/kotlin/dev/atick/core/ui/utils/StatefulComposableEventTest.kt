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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.common.truth.Truth.assertThat
import dev.atick.core.utils.OneTimeEvent
import kotlinx.coroutines.awaitCancellation
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Characterizes event loss separately from OneTimeEvent's atomic consumption guarantee. */
@RunWith(RobolectricTestRunner::class)
class StatefulComposableEventTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `recomposition with the consumed error cancels an unfinished snackbar callback`() {
        var state by mutableStateOf(UiState(0, error = OneTimeEvent<Throwable?>(IllegalStateException("first"))))
        var started = 0
        var cancelled = 0
        val callback: suspend (String, SnackbarAction, Throwable?) -> Boolean = { _, _, _ ->
            started++
            try {
                awaitCancellation()
            } finally {
                cancelled++
            }
        }
        composeTestRule.setContent {
            StatefulComposable(state, callback) {}
        }
        composeTestRule.runOnIdle {
            assertThat(started).isEqualTo(1)
            state = state.copy(data = 1)
        }
        composeTestRule.runOnIdle {
            assertThat(cancelled).isEqualTo(1)
            assertThat(state.error.getContentIfNotHandled()).isNull()
        }
    }

    @Test
    fun `a new error with the same callback key is consumed without starting a new callback`() {
        val first = IllegalStateException("first")
        val second = IllegalStateException("second")
        var state by mutableStateOf(UiState(0, error = OneTimeEvent<Throwable?>(first)))
        val delivered = mutableListOf<Throwable?>()
        val callback: suspend (String, SnackbarAction, Throwable?) -> Boolean = { _, _, error ->
            delivered += error
            awaitCancellation()
        }
        composeTestRule.setContent {
            StatefulComposable(state, callback) {}
        }
        composeTestRule.runOnIdle {
            assertThat(delivered).containsExactly(first)
            state = state.copy(error = OneTimeEvent(second))
        }
        composeTestRule.runOnIdle {
            assertThat(delivered).containsExactly(first)
            assertThat(state.error.getContentIfNotHandled()).isNull()
        }
    }
}
