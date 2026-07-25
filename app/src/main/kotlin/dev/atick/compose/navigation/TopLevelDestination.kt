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

package dev.atick.compose.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import dev.atick.compose.R
import dev.atick.feature.home.navigation.HomeNavKey
import dev.atick.feature.profile.navigation.ProfileNavKey

/**
 * The destinations shown in the navigation bar or rail.
 *
 * Each entry owns a back stack of its own, so switching between them preserves where the user
 * was. The first entry is the start destination: the app exits from there, and back from any
 * other top-level destination returns to it.
 *
 * @property selectedIcon Icon shown when this destination is current.
 * @property unselectedIcon Icon shown otherwise.
 * @property iconTextId Label under the icon.
 * @property titleTextId Title shown in the top app bar.
 * @property key The navigation key this destination maps to.
 */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
    val key: NavKey,
) {
    HOME(
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        iconTextId = R.string.home,
        titleTextId = R.string.home,
        key = HomeNavKey,
    ),
    PROFILE(
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        iconTextId = R.string.profile,
        titleTextId = R.string.profile,
        key = ProfileNavKey,
    ),
    ;

    companion object {
        /** The destination the app starts on and exits from. */
        val START: TopLevelDestination = HOME

        /** Every top-level key, for building the navigation state. */
        val keys: Set<NavKey> = entries.map { it.key }.toSet()

        /** The destination [key] belongs to, or null if it is not a top-level key. */
        fun fromKey(key: NavKey): TopLevelDestination? = entries.firstOrNull { it.key == key }
    }
}
