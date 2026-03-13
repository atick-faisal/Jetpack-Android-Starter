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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Jetpack tag.
 *
 * A clickable tag component with follow/unfollow state. When followed, displays with primary
 * container color; when unfollowed, shows with reduced opacity surface variant. Use for topics,
 * interests, or categories that users can follow.
 *
 * **Features:**
 * - Visual distinction between followed (solid) and unfollowed (translucent) states
 * - Rounded button shape with small text
 * - Theme-aware colors
 * - Disabled state support
 *
 * **Usage Example:**
 * ```kotlin
 * var followedTopics by remember { mutableStateOf(setOf<String>()) }
 * val availableTopics = listOf("Android", "Kotlin", "Compose", "Testing")
 *
 * FlowRow(
 *     horizontalArrangement = Arrangement.spacedBy(8.dp),
 *     verticalArrangement = Arrangement.spacedBy(8.dp)
 * ) {
 *     availableTopics.forEach { topic ->
 *         JetpackTag(
 *             followed = topic in followedTopics,
 *             onClick = {
 *                 followedTopics = if (topic in followedTopics) {
 *                     followedTopics - topic
 *                 } else {
 *                     followedTopics + topic
 *                 }
 *             },
 *             text = { Text(topic) }
 *         )
 *     }
 * }
 * ```
 *
 * **When to use:**
 * - Interest or topic selection (e.g., "Follow topics you're interested in")
 * - Category management
 * - Tag-based filtering with persistent selection
 *
 * @param followed Whether this tag is followed or not.
 * @param onClick The callback to be invoked when this tag is clicked.
 * @param modifier Modifier to be applied to the tag.
 * @param enabled Controls the enabled state of the tag. When `false`, this tag will not be
 * clickable and will appear disabled to accessibility services.
 * @param text The text label content.
 *
 * @see JetpackFilterChip For temporary filtering (non-persistent selection)
 */
@Composable
fun JetpackTag(
    followed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        val containerColor = if (followed) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = JetpackTagDefaults.UNFOLLOWED_TOPIC_TAG_CONTAINER_ALPHA,
            )
        }
        TextButton(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(
                containerColor = containerColor,
                contentColor = contentColorFor(backgroundColor = containerColor),
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = JetpackTagDefaults.DISABLED_TOPIC_TAG_CONTAINER_ALPHA,
                ),
            ),
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.labelSmall) {
                text()
            }
        }
    }
}

/**
 * Jetpack tag default values.
 */
object JetpackTagDefaults {
    const val UNFOLLOWED_TOPIC_TAG_CONTAINER_ALPHA = 0.5f

    // TODO: File bug
    // Button disabled container alpha value not exposed by ButtonDefaults
    const val DISABLED_TOPIC_TAG_CONTAINER_ALPHA = 0.12f
}
