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

package dev.atick.core.utils

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

/**
 * [suspendRunCatching] exists because `runCatching` swallows [CancellationException] and so
 * breaks structured concurrency. That distinction is the only reason the function exists, so it
 * is the thing worth pinning.
 */
class CoroutineUtilsTest {

    @Test
    fun `returns success with the value`() = runTest {
        val result = suspendRunCatching { 42 }

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(42)
    }

    @Test
    fun `wraps a thrown exception as failure`() = runTest {
        val boom = IllegalStateException("boom")

        val result = suspendRunCatching<Int> { throw boom }

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isSameInstanceAs(boom)
    }

    @Test
    fun `rethrows CancellationException instead of wrapping it`() = runTest {
        assertFailsWith<CancellationException> {
            suspendRunCatching<Int> { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun `cancelling the caller propagates rather than producing a failed Result`() = runTest {
        val job = async {
            suspendRunCatching {
                delay(10_000)
                "never"
            }
        }

        job.cancel()

        assertFailsWith<CancellationException> { job.await() }
    }

    @Test
    fun `suspendCoroutineWithTimeout returns the resumed value`() = runTest {
        val value = suspendCoroutineWithTimeout(1.seconds) { continuation ->
            continuation.resume("done")
        }

        assertThat(value).isEqualTo("done")
    }

    @Test
    fun `suspendCoroutineWithTimeout throws when nothing resumes it`() = runTest {
        assertFailsWith<TimeoutCancellationException> {
            suspendCoroutineWithTimeout<String>(1.seconds) {
                // Deliberately never resumed. runTest's virtual clock skips the wait.
            }
        }
    }
}
