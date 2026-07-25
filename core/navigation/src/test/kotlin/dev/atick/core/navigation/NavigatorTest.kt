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

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.Serializable
import org.junit.Test
import kotlin.test.assertFailsWith

/**
 * Back stack behaviour is the part of navigation users notice immediately when it is wrong —
 * a tab losing its place, back exiting the app one press too early — and it is awkward to check
 * by hand on a device. It is plain state manipulation, so it tests directly.
 */
class NavigatorTest {

    @Serializable
    private data object Home : NavKey

    @Serializable
    private data object Profile : NavKey

    @Serializable
    private data class Item(val id: String) : NavKey

    @Serializable
    private data object Detail : NavKey

    private fun navigator(): Navigator {
        val state = NavigationState(
            startKey = Home,
            topLevelStack = NavBackStack<NavKey>(Home),
            subStacks = mapOf(
                Home to NavBackStack<NavKey>(Home),
                Profile to NavBackStack<NavKey>(Profile),
            ),
        )
        return Navigator(state)
    }

    @Test
    fun `starts on the start destination`() {
        val navigator = navigator()

        assertThat(navigator.state.currentTopLevelKey).isEqualTo(Home)
        assertThat(navigator.state.currentKey).isEqualTo(Home)
    }

    @Test
    fun `navigating to a nested key pushes it onto the current stack`() {
        val navigator = navigator()

        navigator.navigate(Item("1"))

        assertThat(navigator.state.currentKey).isEqualTo(Item("1"))
        assertThat(navigator.state.currentTopLevelKey).isEqualTo(Home)
    }

    @Test
    fun `navigating to another top level key switches to it`() {
        val navigator = navigator()

        navigator.navigate(Profile)

        assertThat(navigator.state.currentTopLevelKey).isEqualTo(Profile)
        assertThat(navigator.state.currentKey).isEqualTo(Profile)
    }

    @Test
    fun `switching top level keys preserves each stack`() {
        val navigator = navigator()

        navigator.navigate(Item("1"))
        navigator.navigate(Profile)
        navigator.navigate(Detail)

        // Home kept its own position while Profile was in front.
        navigator.navigate(Home)
        assertThat(navigator.state.currentKey).isEqualTo(Item("1"))

        navigator.navigate(Profile)
        assertThat(navigator.state.currentKey).isEqualTo(Detail)
    }

    @Test
    fun `re-selecting the current top level key pops back to its root`() {
        val navigator = navigator()
        navigator.navigate(Item("1"))
        navigator.navigate(Detail)

        navigator.navigate(Home)

        assertThat(navigator.state.currentKey).isEqualTo(Home)
    }

    @Test
    fun `re-selecting a top level key already at its root is a no-op`() {
        val navigator = navigator()

        navigator.navigate(Home)

        assertThat(navigator.state.currentKey).isEqualTo(Home)
    }

    @Test
    fun `navigating to a key already in the stack moves it to the top`() {
        val navigator = navigator()
        navigator.navigate(Item("1"))
        navigator.navigate(Detail)

        navigator.navigate(Item("1"))

        assertThat(navigator.state.currentKey).isEqualTo(Item("1"))
        // Moved rather than duplicated: one back press returns to Home, not to Detail.
        navigator.goBack()
        assertThat(navigator.state.currentKey).isEqualTo(Detail)
    }

    @Test
    fun `going back pops within the current stack`() {
        val navigator = navigator()
        navigator.navigate(Item("1"))

        navigator.goBack()

        assertThat(navigator.state.currentKey).isEqualTo(Home)
    }

    @Test
    fun `going back from the root of a top level key returns to the previous one`() {
        val navigator = navigator()
        navigator.navigate(Profile)

        navigator.goBack()

        assertThat(navigator.state.currentTopLevelKey).isEqualTo(Home)
    }

    @Test
    fun `going back from a nested key does not switch top level destination`() {
        val navigator = navigator()
        navigator.navigate(Profile)
        navigator.navigate(Detail)

        navigator.goBack()

        assertThat(navigator.state.currentTopLevelKey).isEqualTo(Profile)
        assertThat(navigator.state.currentKey).isEqualTo(Profile)
    }

    @Test
    fun `returning to the start destination resets the top level history`() {
        val navigator = navigator()
        navigator.navigate(Profile)

        navigator.navigate(Home)

        // Back from here must exit the app rather than going back to Profile.
        assertThat(navigator.canGoBack()).isFalse()
    }

    @Test
    fun `canGoBack is false only at the start destination`() {
        val navigator = navigator()

        assertThat(navigator.canGoBack()).isFalse()

        navigator.navigate(Item("1"))
        assertThat(navigator.canGoBack()).isTrue()

        navigator.goBack()
        assertThat(navigator.canGoBack()).isFalse()

        navigator.navigate(Profile)
        assertThat(navigator.canGoBack()).isTrue()
    }

    @Test
    fun `going back from the start destination fails rather than emptying the stack`() {
        val navigator = navigator()

        // The system back handler should finish the activity instead of calling this.
        assertFailsWith<IllegalStateException> { navigator.goBack() }
    }
}
