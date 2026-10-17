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

package dev.atick.core.network.utils

import kotlinx.coroutines.flow.Flow

/**
 * Observes the default network for connectivity UI. CONNECTED does not prove validated Internet
 * access or backend reachability; network operations must still handle failures.
 *
 * @see NetworkState
 */
interface NetworkUtils {
    /**
     * Returns a cold [Flow] that samples the active network's INTERNET capability before registering
     * a default-network callback. Sampling and registration are not an atomic snapshot.
     *
     * The sample emits [NetworkState.CONNECTED] or [NetworkState.UNAVAILABLE]. Subsequent callbacks
     * emit CONNECTED, [NetworkState.LOST], or UNAVAILABLE; LOSING and capability changes are logged
     * without emitting. Consecutive equal values are suppressed.
     *
     * Each collector registers its own callback, removed when collection closes. Callback
     * registration errors can terminate collection.
     *
     * @return Connectivity observations, not a check that a particular request will succeed.
     */
    fun getCurrentState(): Flow<NetworkState>
}
