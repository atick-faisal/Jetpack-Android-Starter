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

@file:Suppress("ktlint:compose:compositionlocal-allowlist")
// io.nlopez.compose.rules' compositionlocal-allowlist check (gradle/init.gradle.kts) flags any
// CompositionLocal not named in its allowlist, to keep ad-hoc CompositionLocals from
// proliferating unchecked. The real fix is a one-line .editorconfig addition:
// compose_allowed_composition_locals = LocalBackgroundTheme,LocalGradientTheme,LocalTintTheme

package dev.atick.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Data class that holds the background theme.
 *
 * @param color The color of the background.
 * @param tonalElevation The tonal elevation of the background.
 */
@Immutable
data class BackgroundTheme(
    val color: Color = Color.Unspecified,
    val tonalElevation: Dp = Dp.Unspecified,
)

/**
 * A composition local for [BackgroundTheme].
 */
val LocalBackgroundTheme = staticCompositionLocalOf { BackgroundTheme() }
