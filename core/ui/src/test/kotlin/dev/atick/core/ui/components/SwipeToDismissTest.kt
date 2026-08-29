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

package dev.atick.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * A dismissed item stays dismissed, so `currentValue` keeps reporting `EndToStart` for as long as
 * the composable is alive. That makes deletion the one callback here that a recomposition can
 * silently repeat, and repeating it deletes rows the user never swiped.
 */
@RunWith(RobolectricTestRunner::class)
class SwipeToDismissTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `onDelete fires once for a completed swipe`() {
        var deleteCount = 0

        composeTestRule.setContent {
            SwipeToDismiss(onDelete = { deleteCount++ }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag(ITEM),
                )
            }
        }

        composeTestRule.onNodeWithTag(ITEM).performTouchInput { swipeLeft() }
        composeTestRule.waitForIdle()

        assertThat(deleteCount).isEqualTo(1)
    }

    @Test
    fun `onDelete does not fire again when the dismissed item recomposes`() {
        var deleteCount = 0
        var recompose by mutableIntStateOf(0)

        composeTestRule.setContent {
            // The counter has to be read out here, in the scope that calls SwipeToDismiss, so the
            // call itself re-executes. Reading it inside the content lambda would only recompose
            // that child scope and would not exercise the composable's own body. A LazyColumn row
            // recomposes this way for real on every list emission.
            val tick = recompose

            SwipeToDismiss(onDelete = { deleteCount++ }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag(ITEM),
                ) {
                    Text(text = "$tick")
                }
            }
        }

        composeTestRule.onNodeWithTag(ITEM).performTouchInput { swipeLeft() }
        composeTestRule.waitForIdle()

        repeat(3) {
            recompose++
            composeTestRule.waitForIdle()
        }

        assertThat(deleteCount).isEqualTo(1)
    }

    @Test
    fun `onDelete does not fire without a swipe`() {
        var deleteCount = 0

        composeTestRule.setContent {
            SwipeToDismiss(onDelete = { deleteCount++ }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag(ITEM),
                )
            }
        }

        composeTestRule.waitForIdle()

        assertThat(deleteCount).isEqualTo(0)
    }

    private companion object {
        const val ITEM = "item"
    }
}
