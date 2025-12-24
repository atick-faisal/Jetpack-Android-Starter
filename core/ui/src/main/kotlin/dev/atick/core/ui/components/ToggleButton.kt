/*
 * Copyright 2024 Atick Faisal
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

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * A data class representing a toggle option.
 *
 * Usage example:
 * ```kotlin
 * val themeOptions = listOf(
 *     ToggleOption(text = R.string.light_theme, icon = Icons.Default.LightMode),
 *     ToggleOption(text = R.string.dark_theme, icon = Icons.Default.DarkMode),
 *     ToggleOption(text = R.string.system_theme, icon = Icons.Default.Settings),
 * )
 * ```
 *
 * @param text The string resource ID for the text to display for the option.
 * @param icon The icon to display for the option.
 */
data class ToggleOption(
    @StringRes val text: Int,
    val icon: ImageVector,
)

/**
 * A row of toggle options that can be selected. Displays a segmented button-style selector
 * with animated transitions between states.
 *
 * This component is useful for mutually exclusive choices like theme selection (Light/Dark/System),
 * view modes (List/Grid), or any other set of 2-4 options.
 *
 * Usage example:
 * ```kotlin
 * val themeOptions = listOf(
 *     ToggleOption(text = R.string.light, icon = Icons.Default.LightMode),
 *     ToggleOption(text = R.string.dark, icon = Icons.Default.DarkMode),
 *     ToggleOption(text = R.string.system, icon = Icons.Default.SettingsBrightness),
 * )
 * var selectedTheme by remember { mutableIntStateOf(2) } // System
 *
 * JetpackToggleOptions(
 *     options = themeOptions,
 *     selectedIndex = selectedTheme,
 *     onSelectionChange = { selectedTheme = it },
 * )
 * ```
 *
 * @param options The list of options to display. Recommended 2-4 options for best UX.
 * @param selectedIndex The zero-based index of the selected option.
 * @param onSelectionChange The callback to invoke when the selected option changes.
 * @param modifier The modifier to apply to the row.
 */
@Composable
fun JetpackToggleOptions(
    options: List<ToggleOption>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .selectableGroup(),
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = selectedIndex == index
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    label = "background color",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.medium)
                        .background(backgroundColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            onSelectionChange(index)
                        }
                        .padding(horizontal = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = option.text),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
}
