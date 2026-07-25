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

package dev.atick.feature.home.navigation

import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.atick.core.navigation.Navigator
import dev.atick.core.ui.utils.SnackbarAction
import dev.atick.feature.home.ui.home.HomeScreen
import dev.atick.feature.home.ui.item.ItemScreen
import kotlinx.serialization.Serializable

/**
 * Top-level destination for the home tab.
 */
@Serializable
data object HomeNavKey : NavKey

/**
 * Detail destination for a single item.
 *
 * @property itemId The item to edit, or null to create a new one.
 */
@Serializable
data class ItemNavKey(val itemId: String?) : NavKey

/**
 * Navigates to the home tab. Re-selecting it pops back to the list.
 */
fun Navigator.navigateToHome() = navigate(HomeNavKey)

/**
 * Opens the item screen. Pass null to create a new item.
 */
fun Navigator.navigateToItem(itemId: String?) = navigate(ItemNavKey(itemId))

/**
 * Registers the home tab's destinations.
 *
 * The list and detail panes carry [ListDetailSceneStrategy] metadata, so on a wide screen they
 * render side by side and on a phone they behave as separate screens. Which layout applies is
 * decided by the scene strategy in the app module, not here.
 *
 * @param navigator Used to move between the two destinations.
 * @param onShowSnackbar Shows a message, returning true if the action was taken.
 */
fun EntryProviderScope<NavKey>.homeEntries(
    navigator: Navigator,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
) {
    entry<HomeNavKey>(
        metadata = ListDetailSceneStrategy.listPane(),
    ) {
        HomeScreen(
            onJetpackClick = navigator::navigateToItem,
            onShowSnackbar = onShowSnackbar,
        )
    }

    entry<ItemNavKey>(
        metadata = ListDetailSceneStrategy.detailPane(),
    ) { key ->
        ItemScreen(
            itemId = key.itemId,
            onBackClick = navigator::goBack,
            onShowSnackbar = onShowSnackbar,
        )
    }
}
