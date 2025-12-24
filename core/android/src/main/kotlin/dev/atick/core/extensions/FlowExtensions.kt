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
 * Converts a Flow to a StateFlow with a 5-second stop timeout for improved performance.
 *
 * This extension function is a convenience wrapper around `stateIn` that uses
 * `SharingStarted.WhileSubscribed(5000L)` to automatically stop the upstream flow
 * 5 seconds after the last subscriber unsubscribes. This prevents unnecessary work
 * when there are no active collectors.
 *
 * ## When to Use
 *
 * Use this when you want to:
 * - Convert a cold Flow (like Room database queries) into a hot StateFlow
 * - Keep the flow active briefly after the last subscriber leaves (e.g., during configuration changes)
 * - Avoid restarting expensive operations during brief UI lifecycle transitions
 *
 * ## Common Use Cases
 *
 * ```kotlin
 * // In a ViewModel - observe database with automatic lifecycle management
 * class MyViewModel(repository: MyRepository) : ViewModel() {
 *     val users: StateFlow<List<User>> = repository.observeUsers()
 *         .stateInDelayed(
 *             initialValue = emptyList(),
 *             scope = viewModelScope
 *         )
 * }
 *
 * // In a Repository - expose Flow as StateFlow
 * class MyRepository(private val database: MyDatabase) {
 *     fun observeUsers(): Flow<List<User>> = database.userDao().observeAll()
 * }
 * ```
 *
 * ## Why 5 Seconds?
 *
 * The 5-second delay is optimal for:
 * - Configuration changes (screen rotation) - typically complete in < 1 second
 * - Brief app backgrounding - keeps data ready for quick return
 * - Navigation transitions - prevents unnecessary restarts
 *
 * While still stopping the flow quickly enough to avoid resource waste when the user
 * truly leaves the screen.
 *
 * ## Performance Benefits
 *
 * ```kotlin
 * // Without delay (restarts on every rotation)
 * val users = repository.observeUsers()
 *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
 *
 * // With delay (survives rotation, avoids restart)
 * val users = repository.observeUsers()
 *     .stateInDelayed(emptyList(), viewModelScope)  // ← Preferred
 * ```
 *
 * @param T The type of values emitted by the Flow.
 * @param initialValue The initial value of the StateFlow before the first emission from the source Flow.
 *                     This value is emitted immediately when the StateFlow is first collected.
 * @param scope The CoroutineScope in which the Flow will be collected. Typically `viewModelScope`.
 *              The Flow collection will be cancelled when this scope is cancelled.
 *
 * @return A StateFlow that replays the last emitted value to new subscribers and stays active
 *         for 5 seconds after the last subscriber unsubscribes.
 *
 * @see kotlinx.coroutines.flow.stateIn For the underlying implementation
 * @see kotlinx.coroutines.flow.SharingStarted.WhileSubscribed For sharing strategy details
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
