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

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A composable that allows swiping from end to start to dismiss an item.
 *
 * This component provides:
 * - **End-to-start swipe only**: Prevents accidental dismissal from start-to-end
 * - **Visual feedback**: Shows delete icon with error container background color during swipe
 * - **Animated background**: Background color animates based on swipe progress
 * - **Confirmation**: onDelete is only called when swipe completes the threshold
 *
 * Common use cases include:
 * - Dismissing items in a LazyColumn list
 * - Removing notifications
 * - Deleting messages or emails
 *
 * Usage example:
 * ```kotlin
 * LazyColumn {
 *     items(items) { item ->
 *         SwipeToDismiss(
 *             onDelete = { viewModel.deleteItem(item.id) },
 *         ) {
 *             ItemCard(item = item)
 *         }
 *     }
 * }
 * ```
 *
 * @param onDelete The callback to be invoked when the item is swiped to dismiss.
 * @param modifier The modifier to be applied to the composable.
 * @param content The content to be displayed (the item being dismissed).
 */
@Composable
fun SwipeToDismiss(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    // Handle dismiss confirmation without deprecated confirmValueChange
    if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
        onDelete()
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                    SwipeToDismissBoxValue.StartToEnd -> Color.Transparent
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                },
                label = "backgroundColorAnimation",
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(color)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        content = { content() },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        gesturesEnabled = true,
        modifier = modifier,
    )
}
