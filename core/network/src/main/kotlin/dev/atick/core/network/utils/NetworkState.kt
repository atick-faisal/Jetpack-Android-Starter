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

import androidx.annotation.StringRes
import dev.atick.core.network.R

/**
 * Network state.
 *
 * @param description [StringRes] description.
 */
enum class NetworkState(@StringRes val description: Int) {
    /**
     * Network is connected.
     */
    CONNECTED(R.string.core_network_network_connected),

    /**
     * Network is connecting.
     */
    LOSING(R.string.core_network_network_losing),

    /**
     * Network is disconnected.
     */
    LOST(R.string.core_network_network_lost),

    /**
     * Network is unavailable.
     */
    UNAVAILABLE(R.string.core_network_network_not_available),
}
