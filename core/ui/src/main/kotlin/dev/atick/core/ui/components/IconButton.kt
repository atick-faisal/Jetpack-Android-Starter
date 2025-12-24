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

package dev.atick.core.ui.components

import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Jetpack toggle button with icon and checked icon content slots. Wraps Material 3
 * [FilledIconToggleButton].
 *
 * A filled icon button that toggles between two states with different icons. Uses primary
 * container color when checked. Common use cases include favorites, bookmarks, or visibility toggles.
 *
 * **Features:**
 * - Automatic icon switching between checked/unchecked states
 * - Filled background with rounded shape
 * - Primary container color when checked, transparent when unchecked
 * - Disabled state support with reduced alpha
 *
 * **Usage Example:**
 * ```kotlin
 * var isFavorite by remember { mutableStateOf(false) }
 *
 * JetpackIconToggleButton(
 *     checked = isFavorite,
 *     onCheckedChange = { isFavorite = it },
 *     icon = {
 *         Icon(
 *             imageVector = Icons.Default.FavoriteBorder,
 *             contentDescription = "Add to favorites"
 *         )
 *     },
 *     checkedIcon = {
 *         Icon(
 *             imageVector = Icons.Default.Favorite,
 *             contentDescription = "Remove from favorites"
 *         )
 *     }
 * )
 * ```
 *
 * **When to use:**
 * - Binary toggle actions (favorite/unfavorite, bookmark/unbookmark)
 * - Visibility toggles (show/hide password, expand/collapse)
 * - State indicators that can be toggled
 *
 * @param checked Whether the toggle button is currently checked.
 * @param onCheckedChange Called when the user clicks the toggle button and toggles checked.
 * @param icon The icon content to show when unchecked.
 * @param modifier Modifier to be applied to the toggle button.
 * @param enabled Controls the enabled state of the toggle button. When `false`, this toggle button
 * will not be clickable and will appear disabled to accessibility services.
 * @param checkedIcon The icon content to show when checked. Defaults to [icon] if not provided.
 */
@Composable
fun JetpackIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checkedIcon: @Composable () -> Unit = icon,
) {
    // TODO: File bug
    // Can't use regular IconToggleButton as it doesn't include a shape (appears square)
    FilledIconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.iconToggleButtonColors(
            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = if (checked) {
                MaterialTheme.colorScheme.onBackground.copy(
                    alpha = JetpackIconButtonDefaults.DISABLED_ICON_BUTTON_CONTAINER_ALPHA,
                )
            } else {
                Color.Transparent
            },
        ),
    ) {
        if (checked) checkedIcon() else icon()
    }
}

/**
 * Jetpack icon button default values.
 */
object JetpackIconButtonDefaults {
    // TODO: File bug
    // IconToggleButton disabled container alpha not exposed by IconButtonDefaults
    const val DISABLED_ICON_BUTTON_CONTAINER_ALPHA = 0.12f
}
