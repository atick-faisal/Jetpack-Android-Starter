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

package dev.atick.core.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Shares this flow in [scope], stopping its upstream five seconds after the last subscriber leaves.
 *
 * Collection starts with the first subscriber. A later subscriber receives the retained latest
 * value and restarts the upstream if it stopped. Reading `value` alone does not start collection.
 * The timeout governs this shared upstream, not separate jobs launched into [scope] by callbacks.
 * Cancelling [scope] cancels the sharing coroutine.
 *
 * @param T The emitted value type.
 * @param initialValue Value available before the source first emits.
 * @param scope Scope owning the sharing coroutine, usually a ViewModel's scope.
 * @return StateFlow retaining the latest value for new subscribers.
 * @see stateIn
 * @see SharingStarted.WhileSubscribed
 */
fun <T> Flow<T>.stateInDelayed(
    initialValue: T,
    scope: CoroutineScope,
): StateFlow<T> {
    return this.stateIn(
        scope = scope,
        initialValue = initialValue,
        started = SharingStarted.WhileSubscribed(5000L),
    )
}
