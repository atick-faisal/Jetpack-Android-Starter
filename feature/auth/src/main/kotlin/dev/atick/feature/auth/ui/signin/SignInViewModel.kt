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

package dev.atick.feature.auth.ui.signin

import android.app.Activity
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atick.core.extensions.isEmailValid
import dev.atick.core.extensions.isPasswordValid
import dev.atick.core.ui.utils.TextFiledData
import dev.atick.core.ui.utils.UiState
import dev.atick.core.ui.utils.updateState
import dev.atick.core.ui.utils.updateWith
import dev.atick.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the sign-in screen, managing authentication state and validation.
 *
 * This ViewModel demonstrates form validation with [TextFiledData] and uses [updateState]
 * for synchronous field updates and [updateWith] for async authentication operations.
 * Field validation uses extension functions from core module (isEmailValid, isPasswordValid).
 *
 * @param authRepository Repository providing authentication operations.
 *
 * @see SignInScreenData Immutable data class representing form state
 * @see UiState State wrapper with loading and error handling
 * @see updateState Extension function for synchronous state updates
 * @see updateWith Extension function for async operations
 * @see TextFiledData Data class for text field state with validation
 * @see AuthRepository Data layer interface for authentication
 */
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _signInUiState = MutableStateFlow(UiState(SignInScreenData()))
    val signInUiState = _signInUiState.asStateFlow()

    fun updateEmail(email: String) {
        _signInUiState.updateState {
            copy(
                email = TextFiledData(
                    value = email,
                    errorMessage = if (email.isEmailValid()) null else "Email Not Valid",
                ),
            )
        }
    }

    fun updatePassword(password: String) {
        _signInUiState.updateState {
            copy(
                password = TextFiledData(
                    value = password,
                    errorMessage = if (password.isPasswordValid()) null else "Password Not Valid",
                ),
            )
        }
    }

    fun signInWithSavedCredentials(activity: Activity) {
        _signInUiState.updateWith {
            authRepository.signInWithSavedCredentials(activity)
        }
    }

    fun signInWithGoogle(activity: Activity) {
        _signInUiState.updateWith { authRepository.signInWithGoogle(activity) }
    }

    fun loginWithEmailAndPassword() {
        _signInUiState.updateWith {
            authRepository.signInWithEmailAndPassword(
                email = email.value,
                password = password.value,
            )
        }
    }
}

/**
 * Immutable data class representing the state of the sign-in form.
 *
 * This class uses [TextFiledData] for form fields to encapsulate both the field value and
 * validation error messages. Being immutable enables efficient Compose recomposition.
 *
 * @param email Email field state with validation error message.
 * @param password Password field state with validation error message.
 *
 * @see SignInViewModel ViewModel that manages this screen data
 * @see TextFiledData Data class for text field state with validation
 * @see UiState Wrapper providing loading and error state
 */
@Immutable
data class SignInScreenData(
    val email: TextFiledData = TextFiledData(String()),
    val password: TextFiledData = TextFiledData(String()),
)
