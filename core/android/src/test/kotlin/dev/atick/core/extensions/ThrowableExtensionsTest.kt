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

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ThrowableExtensionsTest {

    @Test
    fun `getStackTraceString joins frames with newlines`() {
        val throwable = IllegalStateException("boom")

        val trace = throwable.getStackTraceString()

        assertThat(trace).contains("ThrowableExtensionsTest")
        assertThat(trace.lines()).hasSize(throwable.stackTrace.size)
    }

    @Test
    fun `getStackTraceString is empty when there are no frames`() {
        val throwable = IllegalStateException("boom").apply { stackTrace = emptyArray() }

        assertThat(throwable.getStackTraceString()).isEmpty()
    }

    @Test
    fun `asOneTimeEvent wraps the throwable and is consumable exactly once`() {
        val throwable = IllegalStateException("boom")

        val event = throwable.asOneTimeEvent()

        assertThat(event.getContentIfNotHandled()).isSameInstanceAs(throwable)
        assertThat(event.getContentIfNotHandled()).isNull()
    }
}
