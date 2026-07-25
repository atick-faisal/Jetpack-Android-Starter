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

package dev.atick.core.extensions

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * [stateInDelayed] is the project's standard way of exposing a repository flow to a ViewModel.
 * The five second stop timeout is what keeps the upstream alive across a configuration change,
 * so that value is behaviour rather than a detail.
 */
class FlowExtensionsTest {

    @Test
    fun `emits the initial value before upstream produces anything`() = runTest {
        val upstream = MutableStateFlow("first")

        val state = upstream.stateInDelayed("initial", backgroundScope)

        assertThat(state.value).isEqualTo("initial")
    }

    @Test
    fun `emits upstream values once collected`() = runTest {
        val upstream = MutableStateFlow("first")
        val state = upstream.stateInDelayed("initial", backgroundScope)

        state.test {
            assertThat(awaitItem()).isEqualTo("initial")
            assertThat(awaitItem()).isEqualTo("first")

            upstream.value = "second"
            assertThat(awaitItem()).isEqualTo("second")
        }
    }

    @Test
    fun `keeps the upstream subscribed for five seconds after the last collector leaves`() =
        runTest {
            // subscriptionCount reports exactly what the stop timeout governs: whether
            // stateIn is still collecting upstream.
            val upstream = MutableSharedFlow<String>()
            val state = upstream.stateInDelayed("initial", backgroundScope)

            // No collector yet, so stateIn has not subscribed.
            runCurrent()
            assertThat(upstream.subscriptionCount.value).isEqualTo(0)

            state.test { awaitItem() }
            runCurrent()
            assertThat(upstream.subscriptionCount.value).isEqualTo(1)

            // Still inside the 5s window after the collector left: upstream stays subscribed.
            advanceTimeBy(4_000.milliseconds)
            assertThat(upstream.subscriptionCount.value).isEqualTo(1)

            // Past the window: stateIn unsubscribes.
            advanceTimeBy(2_000.milliseconds)
            assertThat(upstream.subscriptionCount.value).isEqualTo(0)
        }
}
