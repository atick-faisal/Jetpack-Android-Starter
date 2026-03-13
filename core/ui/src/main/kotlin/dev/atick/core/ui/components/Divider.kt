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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * A divider with text in the middle.
 *
 * A horizontal divider split by centered text, commonly used to separate sections
 * or alternative options (e.g., "OR" between sign-in methods).
 *
 * **Features:**
 * - Centered text label with equal dividers on both sides
 * - Material 3 HorizontalDivider styling
 * - Small typography for subtle visual separation
 * - 16dp horizontal padding around text
 *
 * **Usage Example:**
 * ```kotlin
 * Column {
 *     // Email sign-in form
 *     JetpackTextFiled(...)
 *     JetpackPasswordFiled(...)
 *     JetpackButton(onClick = { ... }) { Text("Sign In") }
 *
 *     DividerWithText(text = R.string.or)
 *
 *     // Social sign-in buttons
 *     GoogleSignInButton(onClick = { ... })
 *     FacebookSignInButton(onClick = { ... })
 * }
 * ```
 *
 * **When to use:**
 * - Separating alternative sign-in methods ("OR")
 * - Breaking up long forms into logical sections
 * - Showing "More options" between primary and secondary actions
 *
 * @param text The string resource for the text to display.
 * @param modifier The modifier to apply to this layout.
 */
@Composable
fun DividerWithText(
    @StringRes text: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
        )

        Text(
            text = stringResource(id = text),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodySmall,
        )

        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
        )
    }
}
