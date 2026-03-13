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

package dev.atick.core.ui.utils

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multi-preview annotation for testing Composables across various device sizes.
 *
 * Applying this annotation to a Composable function automatically generates preview
 * variants for phone, landscape, foldable, and tablet screen sizes. This ensures
 * your UI is responsive and adapts correctly to different form factors.
 *
 * ## Device Specifications
 * - **Phone**: 360×640dp @ 480dpi (standard portrait phone)
 * - **Landscape**: 640×360dp @ 480dpi (phone in landscape orientation)
 * - **Foldable**: 673×841dp @ 480dpi (unfolded device like Galaxy Fold)
 * - **Tablet**: 1280×800dp @ 480dpi (10-inch tablet)
 *
 * ## Usage
 * ```kotlin
 * @PreviewDevices
 * @Composable
 * fun MyScreenPreview() {
 *     JetpackTheme {
 *         MyScreen(
 *             screenData = MyScreenData(/* preview data */),
 *             onAction = {}
 *         )
 *     }
 * }
 * ```
 *
 * ## Combining with Other Preview Annotations
 * ```kotlin
 * @PreviewDevices
 * @PreviewThemes  // Also test light and dark themes
 * @Composable
 * fun MyComponentPreview() {
 *     JetpackTheme {
 *         MyComponent()
 *     }
 * }
 * ```
 *
 * @see PreviewThemes
 * @see Preview
 */
@Preview(name = "phone", device = "spec:width=360dp,height=640dp,dpi=480")
@Preview(name = "landscape", device = "spec:width=640dp,height=360dp,dpi=480")
@Preview(name = "foldable", device = "spec:width=673dp,height=841dp,dpi=480")
@Preview(name = "tablet", device = "spec:width=1280dp,height=800dp,dpi=480")
annotation class PreviewDevices

/**
 * Multi-preview annotation for testing Composables in light and dark themes.
 *
 * Applying this annotation automatically generates preview variants for both
 * light and dark UI modes, ensuring your components look correct in all theme
 * configurations.
 *
 * ## Usage
 * ```kotlin
 * @PreviewThemes
 * @Composable
 * fun MyButtonPreview() {
 *     JetpackTheme {
 *         Surface {
 *             MyButton(
 *                 text = "Click Me",
 *                 onClick = {}
 *             )
 *         }
 *     }
 * }
 * ```
 *
 * ## Best Practices
 * - Always wrap previews in `JetpackTheme` to test theme colors
 * - Use `Surface` for components that need a background
 * - Combine with `@PreviewDevices` to test themes across device sizes
 *
 * ## Combining Multiple Preview Annotations
 * ```kotlin
 * @PreviewThemes
 * @PreviewDevices
 * @Composable
 * fun ComprehensivePreview() {
 *     JetpackTheme {
 *         MyScreen(/* ... */)
 *     }
 * }
 * // Generates 8 previews: 4 devices × 2 themes
 * ```
 *
 * @see PreviewDevices
 * @see Preview
 */
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark theme")
annotation class PreviewThemes
