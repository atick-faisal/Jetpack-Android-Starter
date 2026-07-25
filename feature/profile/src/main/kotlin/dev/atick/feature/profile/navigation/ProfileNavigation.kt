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

package dev.atick.feature.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.atick.core.navigation.Navigator
import dev.atick.core.ui.utils.SnackbarAction
import dev.atick.feature.profile.ui.ProfileScreen
import kotlinx.serialization.Serializable

/**
 * Top-level destination for the profile tab.
 */
@Serializable
data object ProfileNavKey : NavKey

/**
 * Navigates to the profile tab.
 */
fun Navigator.navigateToProfile() = navigate(ProfileNavKey)

/**
 * Registers the profile tab's destinations.
 *
 * @param onShowSnackbar Shows a message, returning true if the action was taken.
 */
fun EntryProviderScope<NavKey>.profileEntries(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
) {
    entry<ProfileNavKey> {
        ProfileScreen(
            onShowSnackbar = onShowSnackbar,
        )
    }
}
