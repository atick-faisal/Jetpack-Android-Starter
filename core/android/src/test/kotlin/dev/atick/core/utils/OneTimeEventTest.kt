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
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * [OneTimeEvent] is what stops an error being shown twice when a screen recomposes, so the
 * "exactly once" guarantee is the whole point of the type.
 */
class OneTimeEventTest {

    @Test
    fun `getContentIfNotHandled returns the content on first call`() {
        val event = OneTimeEvent("boom")

        assertThat(event.getContentIfNotHandled()).isEqualTo("boom")
    }

    @Test
    fun `getContentIfNotHandled returns null on every call after the first`() {
        val event = OneTimeEvent("boom")

        event.getContentIfNotHandled()

        assertThat(event.getContentIfNotHandled()).isNull()
        assertThat(event.getContentIfNotHandled()).isNull()
    }

    @Test
    fun `peekContent does not consume the event`() {
        val event = OneTimeEvent("boom")

        assertThat(event.peekContent()).isEqualTo("boom")
        assertThat(event.peekContent()).isEqualTo("boom")
        // Still unconsumed, so the real read still succeeds.
        assertThat(event.getContentIfNotHandled()).isEqualTo("boom")
    }

    @Test
    fun `peekContent still works after the event is consumed`() {
        val event = OneTimeEvent("boom")

        event.getContentIfNotHandled()

        assertThat(event.peekContent()).isEqualTo("boom")
    }

    @Test
    fun `null content is consumed like any other value`() {
        val event = OneTimeEvent<String?>(null)

        // Both calls return null, but for different reasons: the first is the content,
        // the second is the already-handled signal.
        assertThat(event.getContentIfNotHandled()).isNull()
        assertThat(event.peekContent()).isNull()
    }

    @Test
    fun `only one of many concurrent readers gets the content`() {
        val threads = 64
        val event = OneTimeEvent("boom")
        val winners = AtomicInteger(0)
        val startLine = CountDownLatch(1)
        val finishLine = CountDownLatch(threads)
        val pool = Executors.newFixedThreadPool(threads)

        repeat(threads) {
            pool.execute {
                // Line every thread up so they contend on the same compareAndSet.
                startLine.await()
                if (event.getContentIfNotHandled() != null) winners.incrementAndGet()
                finishLine.countDown()
            }
        }

        startLine.countDown()
        val finished = finishLine.await(10, TimeUnit.SECONDS)
        pool.shutdownNow()

        assertThat(finished).isTrue()
        assertThat(winners.get()).isEqualTo(1)
    }
}
