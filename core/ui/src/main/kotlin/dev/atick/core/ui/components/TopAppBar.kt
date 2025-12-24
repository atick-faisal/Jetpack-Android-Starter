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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.atick.core.ui.R

/**
 * A Jetpack Compose top app bar with a title, navigation icon, and action icon.
 *
 * Center-aligned top app bar with navigation and action icons. Use for screens
 * that need both back navigation and a primary action.
 *
 * **Usage Example:**
 * ```kotlin
 * JetpackTopAppBar(
 *     titleRes = R.string.edit_profile,
 *     navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
 *     navigationIconContentDescription = "Navigate back",
 *     actionIcon = Icons.Default.Save,
 *     actionIconContentDescription = "Save changes",
 *     onNavigationClick = { navController.navigateUp() },
 *     onActionClick = { viewModel.saveProfile() }
 * )
 * ```
 *
 * @param titleRes The string resource ID for the title of the top app bar.
 * @param navigationIcon The navigation icon to be displayed on the top app bar.
 * @param navigationIconContentDescription The content description for the navigation icon.
 * @param actionIcon The action icon to be displayed on the top app bar.
 * @param actionIconContentDescription The content description for the action icon.
 * @param modifier The modifier for this top app bar.
 * @param colors The colors for this top app bar.
 * @param onNavigationClick The callback when the navigation icon is clicked.
 * @param onActionClick The callback when the action icon is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JetpackTopAppBar(
    @StringRes titleRes: Int,
    navigationIcon: ImageVector,
    navigationIconContentDescription: String?,
    actionIcon: ImageVector,
    actionIconContentDescription: String?,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    onNavigationClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = titleRes)) },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = navigationIcon,
                    contentDescription = navigationIconContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionIconContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        colors = colors,
        modifier = modifier,
    )
}

/**
 * A Jetpack Compose top app bar with a title and action icon.
 *
 * Center-aligned top app bar with only an action icon (no navigation icon).
 * Use for top-level screens where back navigation isn't needed.
 *
 * **Usage Example:**
 * ```kotlin
 * JetpackTopAppBar(
 *     titleRes = R.string.settings,
 *     actionIcon = Icons.Default.Info,
 *     actionIconContentDescription = "About",
 *     onActionClick = { navController.navigate(About) }
 * )
 * ```
 *
 * @param titleRes The string resource ID for the title of the top app bar.
 * @param actionIcon The action icon to be displayed on the top app bar.
 * @param actionIconContentDescription The content description for the action icon.
 * @param modifier The modifier for this top app bar.
 * @param colors The colors for this top app bar.
 * @param onActionClick The callback when the action icon is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JetpackTopAppBar(
    @StringRes titleRes: Int,
    actionIcon: ImageVector,
    actionIconContentDescription: String?,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    onActionClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = titleRes)) },
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionIconContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        colors = colors,
        modifier = modifier,
    )
}

/**
 * A Jetpack Compose top app bar with a title and avatar.
 *
 * Center-aligned top app bar with a user avatar as the action item. Avatar is loaded
 * asynchronously with Coil, with fallback to a default avatar icon.
 *
 * **Usage Example:**
 * ```kotlin
 * val userAvatar by viewModel.userAvatar.collectAsStateWithLifecycle()
 *
 * JetpackTopAppBarWithAvatar(
 *     titleRes = R.string.home,
 *     avatarUri = userAvatar,
 *     avatarContentDescription = "User profile",
 *     onAvatarClick = { navController.navigate(Profile) }
 * )
 * ```
 *
 * @param titleRes The string resource ID for the title of the top app bar.
 * @param avatarUri The URI for the avatar image.
 * @param avatarContentDescription The content description for the avatar.
 * @param modifier The modifier for this top app bar.
 * @param colors The colors for this top app bar.
 * @param onAvatarClick The callback when the avatar is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JetpackTopAppBarWithAvatar(
    @StringRes titleRes: Int,
    avatarUri: String?,
    avatarContentDescription: String?,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    onAvatarClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = titleRes)) },
        actions = {
            IconButton(onClick = onAvatarClick) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = avatarContentDescription,
                    placeholder = painterResource(id = R.drawable.ic_avatar),
                    fallback = painterResource(id = R.drawable.ic_avatar),
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape),
                )
            }
        },
        colors = colors,
        modifier = modifier,
    )
}

/**
 * A Jetpack Compose top app bar with a title, navigation icon, and action button.
 *
 * Left-aligned top app bar with a text button instead of an icon for the action.
 * Use when the action needs to be more prominent or when text is clearer than an icon.
 *
 * **Usage Example:**
 * ```kotlin
 * JetpackActionBar(
 *     titleRes = R.string.select_items,
 *     actionRes = R.string.done,
 *     onActionClick = { viewModel.confirmSelection() },
 *     onNavigateBackClick = { navController.navigateUp() }
 * )
 * ```
 *
 * @param titleRes The string resource ID for the title of the top app bar.
 * @param actionRes The string resource ID for the action button.
 * @param onActionClick The callback when the action button is clicked.
 * @param onNavigateBackClick The callback when the navigation icon is clicked.
 * @param modifier The modifier for this top app bar.
 * @param colors The colors for this top app bar.
 */
@Composable
fun JetpackActionBar(
    @StringRes titleRes: Int,
    @StringRes actionRes: Int,
    onActionClick: () -> Unit,
    onNavigateBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
) {
    TopAppBar(
        title = { Text(text = stringResource(id = titleRes)) },
        navigationIcon = {
            IconButton(onClick = onNavigateBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                )
            }
        },
        actions = {
            JetpackButton(onClick = onActionClick, modifier = Modifier.padding(end = 16.dp)) {
                Text(text = stringResource(id = actionRes))
            }
        },
        colors = colors,
        modifier = modifier,
    )
}
