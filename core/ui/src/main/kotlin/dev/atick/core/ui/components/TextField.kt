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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import dev.atick.core.ui.R

/**
 * A Jetpack Compose text field with customizable appearance and optional error message display.
 *
 * This component automatically handles:
 * - **Error state**: Red border and error message display when errorMessage is not null
 * - **Animated error**: Error message appears/disappears with animation
 * - **Rounded shape**: Uses 50% rounded corners for a pill-shaped appearance
 *
 * Usage example:
 * ```kotlin
 * var email by remember { mutableStateOf("") }
 * var emailError by remember { mutableStateOf<String?>(null) }
 *
 * JetpackTextFiled(
 *     value = email,
 *     onValueChange = { email = it },
 *     label = { Text("Email") },
 *     leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
 *     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
 *     errorMessage = emailError,
 *     modifier = Modifier.fillMaxWidth(),
 * )
 * ```
 *
 * See also:
 * - [JetpackPasswordFiled] for password input with visibility toggle
 * - Use with [TextFieldData] for integrated state management
 *
 * @param value The current text value of the text field.
 * @param onValueChange The callback invoked when the text value changes.
 * @param label A composable function that represents the label of the text field.
 * @param leadingIcon A composable function that represents the leading icon of the text field.
 * @param modifier The modifier for this text field.
 * @param keyboardOptions The keyboard options for the text field (e.g., email, number).
 * @param trailingIcon A composable function that represents the trailing icon of the text field.
 * @param errorMessage The error message to display below the text field. Shows red border when not null.
 */
@Composable
fun JetpackTextFiled(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable () -> Unit = {},
    errorMessage: String? = null,
) {
    JetpackTextFieldWithError(
        value = value,
        onValueChange = onValueChange,
        label = label,
        leadingIcon = leadingIcon,
        errorMessage = errorMessage,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        modifier = modifier,
    )
}

/**
 * A Jetpack Compose password field with visibility toggle and error message display.
 *
 * This component automatically provides:
 * - **Password masking**: Text is hidden by default with PasswordVisualTransformation
 * - **Visibility toggle**: Trailing icon button to show/hide password
 * - **Keyboard type**: Automatically sets keyboard type to Password
 * - **Error handling**: Red border and error message when errorMessage is not null
 * - **State preservation**: Password visibility state survives configuration changes
 *
 * Usage example:
 * ```kotlin
 * var password by remember { mutableStateOf("") }
 * var passwordError by remember { mutableStateOf<String?>(null) }
 *
 * JetpackPasswordFiled(
 *     value = password,
 *     onValueChange = { password = it },
 *     label = { Text("Password") },
 *     leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
 *     errorMessage = passwordError,
 *     modifier = Modifier.fillMaxWidth(),
 * )
 * ```
 *
 * @param value The current text value of the password field.
 * @param onValueChange The callback invoked when the text value changes.
 * @param label A composable function that represents the label of the password field.
 * @param leadingIcon A composable function that represents the leading icon of the password field.
 * @param modifier The modifier for this password field.
 * @param errorMessage The error message to display below the password field. Shows red border when not null.
 */
@Composable
fun JetpackPasswordFiled(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    JetpackTextFieldWithError(
        value = value,
        onValueChange = onValueChange,
        label = label,
        leadingIcon = leadingIcon,
        errorMessage = errorMessage,
        modifier = modifier,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            val image = if (passwordVisible) {
                Icons.Filled.Visibility
            } else {
                Icons.Filled.VisibilityOff
            }
            val description = if (passwordVisible) {
                stringResource(R.string.hide_password)
            } else {
                stringResource(R.string.show_password)
            }
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, description)
            }
        },
    )
}

/**
 * A Jetpack Compose internal component for rendering a text field with optional error message display.
 *
 * @param value The current text value of the text field.
 * @param onValueChange The callback invoked when the text value changes.
 * @param label A composable function that represents the label of the text field.
 * @param leadingIcon A composable function that represents the leading icon of the text field.
 * @param modifier The modifier for this text field.
 * @param trailingIcon A composable function that represents the trailing icon of the text field.
 * @param errorMessage The error message to display below the text field, if any.
 * @param keyboardOptions The keyboard options for the text field.
 * @param visualTransformation The visual transformation to apply to the text.
 * @param shape The shape of the text field.
 */
@Composable
private fun JetpackTextFieldWithError(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable () -> Unit = {},
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(percent = 50),
) {
    Column(
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            keyboardOptions = keyboardOptions,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            shape = shape,
            colors = if (errorMessage == null) {
                OutlinedTextFieldDefaults.colors()
            } else {
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.error,
                    unfocusedBorderColor = MaterialTheme.colorScheme.error,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
