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

import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.Serializable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Serializable
internal data object RestoreHome : NavKey

@Serializable
internal data object RestoreProfile : NavKey

@Serializable
internal data class RestoreItem(val id: String) : NavKey

@Serializable
internal data object RestoreDetail : NavKey

/**
 * [NavigatorTest] builds a [NavigationState] by hand, so nothing there exercises the save and
 * restore path. That path is where a per-tab back stack can quietly come back attached to the
 * wrong tab, which the user sees as a tab that lost its place after the process was killed.
 */
@RunWith(RobolectricTestRunner::class)
class NavigationStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `each top level destination restores its own back stack`() {
        // The iteration order of topLevelKeys is not part of rememberNavigationState's contract,
        // so the stacks have to be restored by which destination they belong to. Reordering the
        // set across the restore is what tells the two apart: sharing one saved state slot for
        // every sub stack still looks correct while the order happens to hold.
        var topLevelKeys: Set<NavKey> = linkedSetOf(RestoreHome, RestoreProfile)

        val restorationTester = StateRestorationTester(composeTestRule)
        lateinit var navigator: Navigator

        restorationTester.setContent {
            val state = rememberNavigationState(
                startKey = RestoreHome,
                topLevelKeys = topLevelKeys,
            )
            navigator = remember(state) { Navigator(state) }
        }

        composeTestRule.runOnIdle {
            navigator.navigate(RestoreItem("1"))
            navigator.navigate(RestoreProfile)
            navigator.navigate(RestoreDetail)
        }

        topLevelKeys = linkedSetOf(RestoreProfile, RestoreHome)
        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.runOnIdle {
            assertThat(navigator.state.subStacks.getValue(RestoreHome))
                .containsExactly(RestoreHome, RestoreItem("1")).inOrder()
            assertThat(navigator.state.subStacks.getValue(RestoreProfile))
                .containsExactly(RestoreProfile, RestoreDetail).inOrder()
            assertThat(navigator.state.topLevelStack)
                .containsExactly(RestoreHome, RestoreProfile).inOrder()
            assertThat(navigator.state.currentKey).isEqualTo(RestoreDetail)
        }
    }
}
