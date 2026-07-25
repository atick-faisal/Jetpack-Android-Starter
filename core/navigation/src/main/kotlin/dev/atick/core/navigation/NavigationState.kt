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

package dev.atick.core.navigation

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

/**
 * Creates a [NavigationState] that survives configuration changes and process death.
 *
 * Each top-level destination gets its own back stack, so switching tabs preserves where the user
 * was in each one. A separate stack of top-level keys records the order tabs were visited, which
 * is what back navigation walks.
 *
 * @param startKey The destination the user exits the app from. Must be in [topLevelKeys].
 * @param topLevelKeys Every top-level destination, typically one per navigation bar item.
 */
@Composable
fun rememberNavigationState(
    startKey: NavKey,
    topLevelKeys: Set<NavKey>,
): NavigationState {
    val topLevelStack = rememberNavBackStack(startKey)
    val subStacks = topLevelKeys.associateWith { key -> rememberNavBackStack(key) }

    return remember(startKey, topLevelKeys) {
        NavigationState(
            startKey = startKey,
            topLevelStack = topLevelStack,
            subStacks = subStacks,
        )
    }
}

/**
 * Holds the app's navigation state. This class never modifies itself — all changes go through
 * [Navigator], which keeps "what the state is" separate from "what a navigation event does".
 *
 * @property startKey The destination the user exits the app from.
 * @property topLevelStack The order in which top-level destinations were visited.
 * @property subStacks One back stack per top-level destination.
 */
class NavigationState(
    val startKey: NavKey,
    val topLevelStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    /** The top-level destination currently shown, i.e. the selected navigation bar item. */
    val currentTopLevelKey: NavKey by derivedStateOf { topLevelStack.last() }

    /** Every top-level destination. */
    val topLevelKeys: Set<NavKey> get() = subStacks.keys

    /** The back stack belonging to [currentTopLevelKey]. */
    @get:VisibleForTesting
    val currentSubStack: NavBackStack<NavKey>
        get() = subStacks[currentTopLevelKey]
            ?: error("Sub stack for $currentTopLevelKey does not exist")

    /** The destination actually on screen. */
    val currentKey: NavKey by derivedStateOf { currentSubStack.last() }
}

/**
 * The flattened back stack `NavDisplay` renders: every visited top-level destination's stack,
 * in the order those destinations were visited.
 *
 * Keeping the start destination's stack first is what makes back from another tab return to
 * home rather than exiting the app.
 */
val NavigationState.backStack: List<NavKey>
    get() = topLevelStack.flatMap { subStacks[it].orEmpty() }

/**
 * The decorators every destination is wrapped in.
 *
 * [rememberSaveableStateHolderNavEntryDecorator] preserves `rememberSaveable` state per
 * destination, and [rememberViewModelStoreNavEntryDecorator] gives each one its own
 * [androidx.lifecycle.ViewModelStore]. Together they are what makes a ViewModel scoped to a
 * destination survive a tab switch and get cleared when that destination is popped.
 */
@Composable
fun rememberDefaultEntryDecorators(): List<NavEntryDecorator<NavKey>> = listOf(
    rememberSaveableStateHolderNavEntryDecorator(),
    rememberViewModelStoreNavEntryDecorator(),
)
