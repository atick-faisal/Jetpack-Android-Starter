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

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavKey

/**
 * The single entry point for navigation events. [NavigationState] holds the state; this class is
 * the only thing that changes it.
 *
 * Feature modules never touch the back stacks directly — they declare
 * `fun Navigator.navigateToX()` extensions next to their [NavKey], which is what keeps a feature
 * from having to know how the rest of the app is structured.
 *
 * @property state The state this navigator updates.
 */
@Stable
class Navigator(val state: NavigationState) {

    /**
     * Navigates to [key], picking the behaviour from what kind of destination it is:
     *
     * - the current top-level destination: pops its stack back to its root, the standard
     *   "tap the selected tab again to go home" gesture
     * - another top-level destination: switches to it, preserving where the user was
     * - anything else: pushes onto the current top-level destination's stack
     *
     * A non-top-level [key] must belong to exactly one top-level destination. Pushing the same
     * key from two different tabs puts it in [NavigationState.backStack] twice, and Navigation 3
     * identifies an entry by `NavEntry.contentKey`, which defaults to the key itself — so the two
     * copies would share one `rememberSaveable` bundle and one `ViewModelStore`. Give each tab
     * its own key type rather than reusing one across tabs.
     */
    fun navigate(key: NavKey) {
        when (key) {
            state.currentTopLevelKey -> clearSubStack()
            in state.topLevelKeys -> goToTopLevel(key)
            else -> goToKey(key)
        }
    }

    /**
     * Goes back one step.
     *
     * At the root of a non-start top-level destination this returns to the previously visited
     * top-level destination rather than popping within the current one.
     *
     * Calling this from [NavigationState.startKey] does nothing: there is nowhere left to go, and
     * only the system back handler can finish the activity. Screens can therefore wire a back
     * button straight to this method — a second tap arriving during an exit transition is
     * harmless. Use [canGoBack] to decide whether to *handle* back at all.
     */
    fun goBack() {
        when (state.currentKey) {
            state.startKey -> Unit
            state.currentTopLevelKey -> state.topLevelStack.removeLastOrNull()
            else -> state.currentSubStack.removeLastOrNull()
        }
    }

    /**
     * Whether [goBack] has somewhere to go. False means back should exit the app.
     */
    fun canGoBack(): Boolean = state.currentKey != state.startKey

    /** Pushes a non-top-level destination onto the current stack. */
    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {
            // Remove first so re-navigating to a destination already in the stack moves it to
            // the top rather than duplicating it.
            remove(key)
            add(key)
        }
    }

    /** Switches to another top-level destination. */
    private fun goToTopLevel(key: NavKey) {
        state.topLevelStack.apply {
            if (key == state.startKey) {
                // Returning to the start destination resets the top-level history, so back from
                // there exits the app rather than cycling through previously visited tabs.
                clear()
            } else {
                remove(key)
            }
            add(key)
        }
    }

    /** Pops the current top-level destination's stack back to its root. */
    private fun clearSubStack() {
        state.currentSubStack.run {
            if (size > 1) subList(1, size).clear()
        }
    }
}
