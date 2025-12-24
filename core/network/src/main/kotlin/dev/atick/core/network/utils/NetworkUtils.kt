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
 * Utility for monitoring network connectivity state changes.
 *
 * This interface provides reactive access to network connectivity status using Android's
 * [ConnectivityManager]. It enables real-time monitoring of network availability, allowing
 * the application to respond to connectivity changes (e.g., showing offline UI, pausing
 * network operations, or retrying failed requests).
 *
 * ## Usage Example
 *
 * In a ViewModel, observe network state to update UI accordingly:
 * ```kotlin
 * @HiltViewModel
 * class MyViewModel @Inject constructor(
 *     private val networkUtils: NetworkUtils
 * ) : ViewModel() {
 *     val networkState = networkUtils.getCurrentState()
 *         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetworkState.UNAVAILABLE)
 *
 *     val isOnline = networkState.map { it == NetworkState.CONNECTED }
 *         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
 * }
 * ```
 *
 * In a Repository, check network state before making requests:
 * ```kotlin
 * class MyRepository @Inject constructor(
 *     private val networkUtils: NetworkUtils,
 *     private val networkDataSource: NetworkDataSource
 * ) {
 *     suspend fun syncData(): Result<Unit> = suspendRunCatching {
 *         networkUtils.getCurrentState().first().let { state ->
 *             if (state != NetworkState.CONNECTED) {
 *                 throw IOException("No network connection")
 *             }
 *         }
 *         networkDataSource.fetchData()
 *     }
 * }
 * ```
 *
 * @see NetworkState for possible connectivity states
 */
interface NetworkUtils {
    /**
     * Observes the current network connectivity state.
     *
     * This function returns a cold [Flow] that emits [NetworkState] changes in real-time.
     * The flow registers a [ConnectivityManager.NetworkCallback] when collected and
     * unregisters it when the collector is cancelled, ensuring proper resource cleanup.
     *
     * ## Emitted States
     *
     * - [NetworkState.CONNECTED] - Network is available and connected
     * - [NetworkState.LOSING] - Network connection is degrading
     * - [NetworkState.LOST] - Network connection was lost
     * - [NetworkState.UNAVAILABLE] - No network is available
     *
     * ## Lifecycle
     *
     * - The callback is registered when the flow is collected
     * - The callback is automatically unregistered when the flow collector is cancelled
     * - Each collection creates a new callback registration (cold flow)
     *
     * ## Threading
     *
     * Network callbacks execute on a background thread managed by the system.
     * The flow emissions are thread-safe and can be collected from any coroutine context.
     *
     * ## Best Practices
     *
     * - Convert to [StateFlow] in ViewModels for UI consumption
     * - Use [Flow.distinctUntilChanged] to avoid redundant state updates
     * - Handle all network states, not just CONNECTED/LOST
     *
     * @return A cold [Flow] that emits [NetworkState] changes. The flow never completes
     *         unless the collector is cancelled.
     *
     * @see NetworkState
     */
    fun getCurrentState(): Flow<NetworkState>
}
