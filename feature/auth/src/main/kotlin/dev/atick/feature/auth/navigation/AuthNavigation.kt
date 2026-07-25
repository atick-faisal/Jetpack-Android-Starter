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

package dev.atick.feature.auth.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.atick.core.navigation.Navigator
import dev.atick.core.ui.utils.SnackbarAction
import dev.atick.feature.auth.ui.signin.SignInScreen
import dev.atick.feature.auth.ui.signup.SignUpScreen
import kotlinx.serialization.Serializable

/**
 * Sign-in destination, and the entry point of the signed-out flow.
 */
@Serializable
data object SignInNavKey : NavKey

/**
 * Sign-up destination.
 */
@Serializable
data object SignUpNavKey : NavKey

/**
 * Navigates to sign in.
 */
fun Navigator.navigateToSignIn() = navigate(SignInNavKey)

/**
 * Navigates to sign up.
 */
fun Navigator.navigateToSignUp() = navigate(SignUpNavKey)

/**
 * Registers the signed-out destinations.
 *
 * These live in a navigation graph of their own, shown only while the user is signed out.
 * Nothing navigates from here into the signed-in graph: the app swaps graphs when the auth state
 * changes, so no stale back stack survives signing in or out.
 *
 * @param navigator Used to move between sign in and sign up.
 * @param onShowSnackbar Shows a message, returning true if the action was taken.
 */
fun EntryProviderScope<NavKey>.authEntries(
    navigator: Navigator,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
) {
    entry<SignInNavKey> {
        SignInScreen(
            onSignUpClick = navigator::navigateToSignUp,
            onShowSnackbar = onShowSnackbar,
        )
    }

    entry<SignUpNavKey> {
        SignUpScreen(
            onSignInClick = navigator::navigateToSignIn,
            onShowSnackbar = onShowSnackbar,
        )
    }
}
