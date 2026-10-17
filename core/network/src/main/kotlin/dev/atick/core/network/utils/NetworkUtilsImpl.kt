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

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of [NetworkUtils].
 *
 * @param connectivityManager [ConnectivityManager].
 */
internal class NetworkUtilsImpl @Inject constructor(
    private val connectivityManager: ConnectivityManager,
) : NetworkUtils {

    /**
     * Get the current network state.
     *
     * @return [Flow] of [NetworkState].
     */
    override fun getCurrentState(): Flow<NetworkState> = callbackFlow {
        // Seed from active-network capabilities before callback registration; the two can race.
        trySend(currentNetworkState())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(NetworkState.CONNECTED)
                Timber.i("NETWORK CONNECTED")
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                Timber.i("LOSING NETWORK CONNECTION ... ")
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(NetworkState.LOST)
                Timber.i("NETWORK CONNECTION LOST")
            }

            override fun onUnavailable() {
                super.onUnavailable()
                trySend(NetworkState.UNAVAILABLE)
                Timber.i("NETWORK UNAVAILABLE")
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                Timber.i("NETWORK TYPE CHANGED")
            }

            override fun onLinkPropertiesChanged(
                network: Network,
                linkProperties: LinkProperties,
            ) {
                super.onLinkPropertiesChanged(network, linkProperties)
                Timber.i("LINK PROPERTIES CHANGED")
            }

            override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                Timber.i("BLOCKED STATUS CHANGED")
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        // Suppress repeated states, including an onAvailable callback matching the initial sample.
        .distinctUntilChanged()

    /**
     * Reads connectivity as it stands right now, for the initial emission.
     *
     * @return [NetworkState.CONNECTED] if the active network reports internet capability,
     *         [NetworkState.UNAVAILABLE] otherwise.
     */
    private fun currentNetworkState(): NetworkState {
        val capabilities = connectivityManager.activeNetwork
            ?.let(connectivityManager::getNetworkCapabilities)
        val hasInternet = capabilities
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        return if (hasInternet) NetworkState.CONNECTED else NetworkState.UNAVAILABLE
    }
}
