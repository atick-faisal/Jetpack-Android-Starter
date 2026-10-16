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

package dev.atick.core.utils

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Event content that can be consumed at most once per wrapper instance.
 *
 * [getContentIfNotHandled] atomically marks the instance consumed before returning its content.
 * Competing consumers share that flag; only one can obtain non-null content. This does not
 * acknowledge a completed UI action, retry a cancelled action, or persist across process death.
 * A newly created wrapper has its own consumption flag, even for equal content.
 *
 * @param T The content type, which may be nullable.
 * @property content Content returned by the first consuming call, or by [peekContent].
 */
class OneTimeEvent<T>(private val content: T) {
    private var hasBeenHandled = AtomicBoolean(false)

    /**
     * Atomically marks this event consumed, returning its content only on the first call.
     *
     * This is the primary way to consume the event. Only the first consuming call can return the content, including when
     * called concurrently. Null content is indistinguishable from an already-consumed event.
     *
     * ## Usage Example
     * ```kotlin
     * val event = OneTimeEvent("Error occurred")
     * val content1 = event.getContentIfNotHandled() // Returns "Error occurred"
     * val content2 = event.getContentIfNotHandled() // Returns null (already handled)
     * ```
     *
     * @return The event content on first call, null on subsequent calls.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled.compareAndSet(false, true)) content else null
    }

    /**
     * Returns the content without marking it as handled.
     *
     * This is useful for debugging, logging, or cases where you need to inspect
     * the event content without consuming it. Use sparingly in production code.
     *
     * ## Usage Example
     * ```kotlin
     * val event = OneTimeEvent("Error occurred")
     * val peeked = event.peekContent() // Returns "Error occurred"
     * val content = event.getContentIfNotHandled() // Still returns "Error occurred"
     * ```
     *
     * @return The event content, regardless of whether it has been handled.
     */
    fun peekContent(): T = content
}
