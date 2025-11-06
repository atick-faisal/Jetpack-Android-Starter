/*
 * Copyright 2025 Atick Faisal
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Jetpack tab. Wraps Material 3 [Tab] and shifts text label down.
 *
 * A styled tab component with centered text and custom padding. Use with [JetpackTabRow]
 * to create tabbed navigation or content switching interfaces.
 *
 * **Features:**
 * - Custom top padding for visual alignment
 * - Center-aligned text with labelLarge typography
 * - Accessibility support for enabled/disabled states
 *
 * **Usage Example:**
 * ```kotlin
 * var selectedTab by remember { mutableIntStateOf(0) }
 * val tabs = listOf("Overview", "Details", "Reviews")
 *
 * JetpackTabRow(selectedTabIndex = selectedTab) {
 *     tabs.forEachIndexed { index, title ->
 *         JetpackTab(
 *             selected = selectedTab == index,
 *             onClick = { selectedTab = index },
 *             text = { Text(title) }
 *         )
 *     }
 * }
 * ```
 *
 * @param selected Whether this tab is selected or not.
 * @param onClick The callback to be invoked when this tab is selected.
 * @param modifier Modifier to be applied to the tab.
 * @param enabled Controls the enabled state of the tab. When `false`, this tab will not be
 * clickable and will appear disabled to accessibility services.
 * @param text The text label content.
 *
 * @see JetpackTabRow Container for tabs with indicator
 */
@Composable
fun JetpackTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable () -> Unit,
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        text = {
            val style = MaterialTheme.typography.labelLarge.copy(textAlign = TextAlign.Center)
            ProvideTextStyle(
                value = style,
                content = {
                    Box(modifier = Modifier.padding(top = JetpackTabDefaults.TabTopPadding)) {
                        text()
                    }
                },
            )
        },
    )
}

/**
 * Jetpack tab row. Wraps Material 3 [TabRow].
 *
 * A container for tabs with a secondary indicator that shows the selected tab position.
 * Tabs are evenly distributed across the available width.
 *
 * **Features:**
 * - Transparent container background
 * - Secondary indicator (2dp height) that animates between tabs
 * - Equal spacing for all tabs
 * - Theme-aware colors from MaterialTheme
 *
 * **Usage Example:**
 * ```kotlin
 * var selectedTab by remember { mutableIntStateOf(0) }
 * val tabs = listOf("Feed", "Explore", "Profile")
 *
 * Column {
 *     JetpackTabRow(selectedTabIndex = selectedTab) {
 *         tabs.forEachIndexed { index, title ->
 *             JetpackTab(
 *                 selected = selectedTab == index,
 *                 onClick = { selectedTab = index },
 *                 text = { Text(title) }
 *             )
 *         }
 *     }
 *
 *     // Content for selected tab
 *     when (selectedTab) {
 *         0 -> FeedScreen()
 *         1 -> ExploreScreen()
 *         2 -> ProfileScreen()
 *     }
 * }
 * ```
 *
 * @param selectedTabIndex The index of the currently selected tab.
 * @param modifier Modifier to be applied to the tab row.
 * @param tabs The tabs inside this tab row. Typically this will be multiple [JetpackTab]s. Each element
 * inside this lambda will be measured and placed evenly across the row, each taking up equal space.
 *
 * @see JetpackTab Individual tab component
 */
@Composable
fun JetpackTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit,
) {
    SecondaryTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tabs = tabs,
    )
}

/**
 * Jetpack tab default values.
 */
object JetpackTabDefaults {
    val TabTopPadding = 7.dp
}
