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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter.State.Error
import coil.compose.AsyncImagePainter.State.Loading
import coil.compose.rememberAsyncImagePainter
import dev.atick.core.ui.R
import dev.atick.core.ui.theme.LocalTintTheme

/**
 * A wrapper around [AsyncImage] which determines the colorFilter based on the theme.
 *
 * This component automatically handles:
 * - **Loading state**: Displays [JetpackLoadingWheel] while the image is loading
 * - **Error handling**: Falls back to placeholder image on load failure
 * - **Theme integration**: Applies tint color from [LocalTintTheme] when appropriate
 * - **Preview mode**: Shows placeholder in Android Studio preview
 *
 * Usage example:
 * ```kotlin
 * DynamicAsyncImage(
 *     imageUrl = "https://example.com/avatar.jpg",
 *     contentDescription = "User avatar",
 *     placeholder = painterResource(R.drawable.ic_placeholder),
 *     modifier = Modifier
 *         .size(80.dp)
 *         .clip(CircleShape),
 * )
 * ```
 *
 * @param imageUrl The URL of the image to load
 * @param contentDescription Text used by accessibility services to describe the image
 * @param modifier Modifier to be applied to the image
 * @param placeholder The painter to display while loading or on error. Defaults to ic_placeholder.
 */
@Composable
fun DynamicAsyncImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter = painterResource(R.drawable.ic_placeholder),
) {
    val iconTint = LocalTintTheme.current.iconTint
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    val imageLoader = rememberAsyncImagePainter(
        model = imageUrl,
        onState = { state ->
            isLoading = state is Loading
            isError = state is Error
        },
    )
    val isLocalInspection = LocalInspectionMode.current
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading && !isLocalInspection) {
            // Display a progress bar while loading
            JetpackLoadingWheel(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp),
                contentDesc = contentDescription.toString(),
            )
        }
        Image(
            contentScale = ContentScale.Crop,
            painter = if (isError.not() && !isLocalInspection) imageLoader else placeholder,
            contentDescription = contentDescription,
            colorFilter = if (iconTint != Unspecified && iconTint != null) {
                ColorFilter.tint(iconTint)
            } else {
                null
            },
        )
    }
}
