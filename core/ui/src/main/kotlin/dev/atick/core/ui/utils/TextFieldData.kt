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

/**
 * Represents the state of a text input field with optional validation error.
 *
 * This data class is designed to work within the [UiState] wrapper pattern, providing
 * a clean way to manage text field state and validation errors. It's commonly used
 * in screen data classes to represent form inputs.
 *
 * ## Illustrative screen data (not an existing class)
 * ```kotlin
 * data class LoginScreenData(
 *     val email: TextFieldData = TextFieldData(""),
 *     val password: TextFieldData = TextFieldData("")
 * )
 * ```
 *
 * ## Illustrative ViewModel excerpt (imports and repository omitted)
 * ```kotlin
 * @HiltViewModel
 * class LoginViewModel @Inject constructor(
 *     private val authRepository: AuthRepository
 * ) : ViewModel() {
 *     private val _uiState = MutableStateFlow(UiState(LoginScreenData()))
 *     val uiState = _uiState.asStateFlow()
 *
 *     fun updateEmail(email: String) {
 *         _uiState.updateState {
 *             copy(email = TextFieldData(email, validateEmail(email)))
 *         }
 *     }
 *
 *     private fun validateEmail(email: String): String? {
 *         return when {
 *             email.isBlank() -> "Email cannot be empty"
 *             !email.isEmailValid() -> "Invalid email format"
 *             else -> null
 *         }
 *     }
 * }
 * ```
 *
 * ## Illustrative composable excerpt (imports omitted)
 * ```kotlin
 * @Composable
 * fun LoginScreen(
 *     screenData: LoginScreenData,
 *     onEmailChange: (String) -> Unit
 * ) {
 *     JetpackTextField(
 *         value = screenData.email.value,
 *         onValueChange = onEmailChange,
 *         label = { Text("Email") },
 *         errorMessage = screenData.email.errorMessage,
 *     )
 * }
 * ```
 *
 * [UiText] is available for localized UI text, but this class currently stores `String?` errors.
 * Changing that type requires updating the form state and rendering calls together; the
 * [form guide](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/components.md#uitext-and-form-errors)
 * explains the current validation and localization boundaries.
 *
 * @property value The current text content of the field
 * @property errorMessage Optional validation error message. Null means no stored error, not proof
 *     that validation ran or that the backend accepts the value.
 *
 * @see UiState
 * @see UiText
 */
data class TextFieldData(
    val value: String,
    val errorMessage: String? = null,
)
