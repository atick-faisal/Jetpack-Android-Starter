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
 * ## Usage in Screen Data
 * ```kotlin
 * data class LoginScreenData(
 *     val email: TextFiledData = TextFiledData(""),
 *     val password: TextFiledData = TextFiledData("")
 * )
 * ```
 *
 * ## Usage in ViewModel
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
 *             copy(email = TextFiledData(email, validateEmail(email)))
 *         }
 *     }
 *
 *     private fun validateEmail(email: String): String? {
 *         return when {
 *             email.isBlank() -> "Email cannot be empty"
 *             !email.isValidEmail() -> "Invalid email format"
 *             else -> null
 *         }
 *     }
 * }
 * ```
 *
 * ## Usage in Composable
 * ```kotlin
 * @Composable
 * fun LoginScreen(
 *     screenData: LoginScreenData,
 *     onEmailChange: (String) -> Unit
 * ) {
 *     OutlinedTextField(
 *         value = screenData.email.value,
 *         onValueChange = onEmailChange,
 *         label = { Text("Email") },
 *         isError = screenData.email.errorMessage != null,
 *         supportingText = screenData.email.errorMessage?.let { { Text(it) } }
 *     )
 * }
 * ```
 *
 * ## With UiText for Type-Safe Errors
 * ```kotlin
 * data class TextFiledData(
 *     val value: String,
 *     val errorMessage: UiText? = null  // Use UiText instead of String
 * )
 *
 * // In Composable:
 * supportingText = screenData.email.errorMessage?.let { error ->
 *     { Text(error.asString()) }
 * }
 * ```
 *
 * @property value The current text content of the field
 * @property errorMessage Optional validation error message. When null, the field is valid.
 *
 * @see UiState
 * @see UiText
 */
data class TextFiledData(
    val value: String,
    val errorMessage: String? = null,
)
