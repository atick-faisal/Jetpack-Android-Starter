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

package dev.atick.compose.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.atick.core.extensions.stateInDelayed
import dev.atick.core.network.utils.NetworkState
import dev.atick.core.network.utils.NetworkUtils
import dev.atick.firebase.analytics.utils.CrashReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Remembers a [JetpackAppState].
 *
 * @param isUserLoggedIn Whether a user is signed in.
 * @param windowSizeClass The current window size class.
 * @param networkUtils Source of connectivity state.
 * @param crashReporter Used to report errors the user chooses to send.
 * @param userProfilePictureUri Avatar shown in the top app bar.
 * @param coroutineScope Scope backing [JetpackAppState.isOffline].
 */
@Composable
fun rememberJetpackAppState(
    isUserLoggedIn: Boolean,
    windowSizeClass: WindowSizeClass,
    networkUtils: NetworkUtils,
    crashReporter: CrashReporter,
    userProfilePictureUri: String? = null,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): JetpackAppState {
    return remember(
        isUserLoggedIn,
        userProfilePictureUri,
        windowSizeClass,
        coroutineScope,
        networkUtils,
        crashReporter,
    ) {
        JetpackAppState(
            isUserLoggedIn = isUserLoggedIn,
            userProfilePictureUri = userProfilePictureUri,
            windowSizeClass = windowSizeClass,
            crashReporter = crashReporter,
            coroutineScope = coroutineScope,
            networkUtils = networkUtils,
        )
    }
}

/**
 * App-wide state that is not navigation.
 *
 * Navigation state deliberately lives outside this class. It is created inside the signed-in and
 * signed-out branches of [JetpackApp] so that each has its own back stacks, and so that signing
 * in or out discards the other branch's history rather than leaving it to be returned to.
 *
 * @property isUserLoggedIn Whether a user is signed in. Decides which navigation graph is shown.
 * @property userProfilePictureUri Avatar shown in the top app bar.
 * @property windowSizeClass The current window size class.
 * @property crashReporter Used to report errors the user chooses to send.
 * @param coroutineScope Scope backing [isOffline].
 * @param networkUtils Source of connectivity state.
 */
@Stable
class JetpackAppState(
    val isUserLoggedIn: Boolean,
    val userProfilePictureUri: String?,
    val windowSizeClass: WindowSizeClass,
    val crashReporter: CrashReporter,
    coroutineScope: CoroutineScope,
    networkUtils: NetworkUtils,
) {
    /**
     * Whether the device is currently offline.
     */
    val isOffline: StateFlow<Boolean> = networkUtils.getCurrentState()
        .map { it != NetworkState.CONNECTED }
        .stateInDelayed(false, coroutineScope)
}
