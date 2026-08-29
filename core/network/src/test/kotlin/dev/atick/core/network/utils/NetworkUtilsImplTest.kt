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

package dev.atick.core.network.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetworkCapabilities

/**
 * A `NetworkCallback` only reports transitions, so the state a collector sees before anything
 * changes is entirely down to the initial emission. That first value is what an offline banner
 * renders from on a cold start, and getting it wrong is invisible until someone opens the app
 * with no connectivity.
 */
@RunWith(RobolectricTestRunner::class)
class NetworkUtilsImplTest {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkUtils: NetworkUtils

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        networkUtils = NetworkUtilsImpl(connectivityManager)
    }

    @Test
    fun `emits CONNECTED on collection when the device is already online`() = runTest {
        setInternetCapability(hasInternet = true)

        networkUtils.getCurrentState().test {
            assertThat(awaitItem()).isEqualTo(NetworkState.CONNECTED)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits UNAVAILABLE on collection when the device is already offline`() = runTest {
        setInternetCapability(hasInternet = false)

        networkUtils.getCurrentState().test {
            assertThat(awaitItem()).isEqualTo(NetworkState.UNAVAILABLE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits UNAVAILABLE on collection when there is no active network at all`() = runTest {
        shadowOf(connectivityManager).setDefaultNetworkActive(false)
        shadowOf(connectivityManager).setActiveNetworkInfo(null)

        networkUtils.getCurrentState().test {
            assertThat(awaitItem()).isEqualTo(NetworkState.UNAVAILABLE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Replaces the capabilities reported for the active network, which is what the initial
     * emission is derived from.
     */
    private fun setInternetCapability(hasInternet: Boolean) {
        val capabilities = ShadowNetworkCapabilities.newInstance()
        if (hasInternet) {
            shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        shadowOf(connectivityManager).setNetworkCapabilities(
            connectivityManager.activeNetwork,
            capabilities,
        )
    }
}
