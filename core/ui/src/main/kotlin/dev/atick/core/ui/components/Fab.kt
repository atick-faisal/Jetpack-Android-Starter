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

import androidx.annotation.StringRes
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

/**
 * Jetpack extended floating action button.
 *
 * An extended FAB with both icon and text label. Provides more context than a standard FAB
 * while remaining prominent on screen. The icon and text share the same string resource for
 * content description and label.
 *
 * **Features:**
 * - Combined icon and text for clarity
 * - Material 3 theming and colors
 * - Standard FAB elevation and behavior
 * - Uses the same resource for icon description and button text
 *
 * **Usage Example:**
 * ```kotlin
 * Scaffold(
 *     floatingActionButton = {
 *         JetpackExtendedFab(
 *             icon = Icons.Default.Add,
 *             text = R.string.create_item,
 *             onClick = { navController.navigate(CreateItem) }
 *         )
 *     }
 * ) { padding ->
 *     ItemList(Modifier.padding(padding))
 * }
 * ```
 *
 * **When to use:**
 * - Primary actions that need text label for clarity (e.g., "Create", "Compose")
 * - When users need extra context beyond just an icon
 * - First-time user experiences where action should be obvious
 *
 * **Note:** For icon-only FABs, use Material 3's `FloatingActionButton` directly.
 *
 * @param icon The icon to be displayed on the floating action button.
 * @param text The string resource for both the text label and icon content description.
 * @param onClick Callback when the floating action button is clicked.
 * @param modifier Modifier to be applied to the floating action button.
 */
@Composable
fun JetpackExtendedFab(
    icon: ImageVector,
    @StringRes text: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(icon, stringResource(text)) },
        text = { Text(text = stringResource(text)) },
        modifier = modifier,
    )
}
