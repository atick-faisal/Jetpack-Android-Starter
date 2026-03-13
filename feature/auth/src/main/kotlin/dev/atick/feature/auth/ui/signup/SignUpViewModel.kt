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

package dev.atick.feature.auth.ui.signup

import android.app.Activity
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atick.core.extensions.isEmailValid
import dev.atick.core.extensions.isPasswordValid
import dev.atick.core.extensions.isValidFullName
import dev.atick.core.ui.utils.TextFiledData
import dev.atick.core.ui.utils.UiState
import dev.atick.core.ui.utils.updateState
import dev.atick.core.ui.utils.updateWith
import dev.atick.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the sign-up screen, managing user registration and form validation.
 *
 * This ViewModel handles new user registration with both Google Sign-In and email/password
 * methods. Similar to [dev.atick.feature.auth.ui.signin.SignInViewModel], it demonstrates
 * form validation with [TextFiledData] using [updateState] for field updates and [updateWith]
 * for async registration operations.
 *
 * Form validation uses extension functions:
 * - [dev.atick.core.extensions.isValidFullName] for name validation
 * - [dev.atick.core.extensions.isEmailValid] for email validation
 * - [dev.atick.core.extensions.isPasswordValid] for password validation
 *
 * @param authRepository Repository providing authentication and registration operations.
 *
 * @see SignUpScreenData Immutable data class representing registration form state
 * @see UiState State wrapper with loading and error handling
 * @see updateState Extension function for synchronous state updates
 * @see updateWith Extension function for async operations
 * @see TextFiledData Data class for text field state with validation
 * @see AuthRepository Data layer interface for authentication
 */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _signUpUiState = MutableStateFlow(UiState(SignUpScreenData()))
    val signUpUiState = _signUpUiState.asStateFlow()

    fun updateName(name: String) {
        _signUpUiState.updateState {
            copy(
                name = TextFiledData(
                    value = name,
                    errorMessage = if (name.isValidFullName()) null else "Name Not Valid",
                ),
            )
        }
    }

    fun updateEmail(email: String) {
        _signUpUiState.updateState {
            copy(
                email = TextFiledData(
                    value = email,
                    errorMessage = if (email.isEmailValid()) null else "Email Not Valid",
                ),
            )
        }
    }

    fun updatePassword(password: String) {
        _signUpUiState.updateState {
            copy(
                password = TextFiledData(
                    value = password,
                    errorMessage = if (password.isPasswordValid()) null else "Password Not Valid",
                ),
            )
        }
    }

    fun registerWithGoogle(activity: Activity) {
        _signUpUiState.updateWith { authRepository.registerWithGoogle(activity) }
    }

    fun registerWithEmailAndPassword(activity: Activity) {
        _signUpUiState.updateWith {
            authRepository.registerWithEmailAndPassword(
                name = name.value,
                email = email.value,
                password = password.value,
                activity = activity,
            )
        }
    }
}

/**
 * Immutable data class representing the state of the registration form.
 *
 * This class manages the state of the sign-up form with three validated fields. Each field
 * uses [TextFiledData] to encapsulate both the field value and validation error messages,
 * providing real-time feedback to users as they fill out the form.
 *
 * Form validation is performed in [SignUpViewModel] methods ([SignUpViewModel.updateName],
 * [SignUpViewModel.updateEmail], [SignUpViewModel.updatePassword]) which update the corresponding
 * [TextFiledData.errorMessage] based on validation rules. The form is considered valid when all
 * three fields have null error messages.
 *
 * Usage context:
 * - Route composable observes [SignUpViewModel.signUpUiState] which wraps this data class
 * - Text field composables bind to [name], [email], and [password] with two-way data flow
 * - Error messages from [TextFiledData] are displayed below each input field
 * - Submit button is enabled only when all fields are valid (no error messages)
 * - Registration success triggers navigation to home screen via repository flow
 *
 * @param name Name field state with validation for full name format.
 * @param email Email field state with validation for email format.
 * @param password Password field state with validation for minimum requirements.
 *
 * @see SignUpViewModel ViewModel that manages this screen data
 * @see TextFiledData Data class for text field state with validation
 * @see UiState Wrapper providing loading and error state
 */
@Immutable
data class SignUpScreenData(
    val name: TextFiledData = TextFiledData(String()),
    val email: TextFiledData = TextFiledData(String()),
    val password: TextFiledData = TextFiledData(String()),
)
