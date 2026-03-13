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

package dev.atick.firebase.auth.data

import android.app.Activity
import dev.atick.firebase.auth.model.AuthUser

/**
 * Interface defining data source operations for Firebase Authentication.
 *
 * This interface provides a unified abstraction over Firebase Authentication and Android's
 * Credential Manager API. It supports multiple authentication methods including email/password
 * and Google Sign-In, with built-in credential saving for seamless re-authentication.
 *
 * ## Authentication Flow
 * 1. **First Time Users**: Call [registerWithEmailAndPassword] or [registerWithGoogle]
 * 2. **Returning Users**: Call [signInWithSavedCredentials] or specific sign-in methods
 * 3. **Session Check**: Use [getCurrentUser] to check authentication state
 * 4. **Sign Out**: Call [signOut] to clear authentication state
 *
 * ## Thread Safety
 * All suspend functions should be called from a coroutine context with appropriate dispatcher.
 * The implementation ensures thread safety by using Firebase's built-in thread handling.
 *
 * ## Error Handling
 * All authentication methods can throw exceptions:
 * - `FirebaseAuthInvalidCredentialsException` - Wrong email/password
 * - `FirebaseAuthUserCollisionException` - Email already in use
 * - `FirebaseAuthInvalidUserException` - User account doesn't exist
 * - `FirebaseNetworkException` - Network connectivity issues
 * - `CancellationException` - User cancelled the authentication flow
 *
 * Wrap calls in `suspendRunCatching` for proper error handling.
 *
 * ## Example Usage
 * ```kotlin
 * class AuthRepository @Inject constructor(
 *     private val authDataSource: AuthDataSource
 * ) {
 *     // Check current auth state
 *     fun isSignedIn(): Boolean = authDataSource.getCurrentUser() != null
 *
 *     // Sign in with saved credentials (auto-login)
 *     suspend fun autoSignIn(activity: Activity): Result<AuthUser> =
 *         suspendRunCatching {
 *             authDataSource.signInWithSavedCredentials(activity)
 *         }
 *
 *     // Email/password sign in
 *     suspend fun signIn(email: String, password: String): Result<AuthUser> =
 *         suspendRunCatching {
 *             authDataSource.signInWithEmailAndPassword(email, password)
 *         }
 *
 *     // Google Sign-In
 *     suspend fun signInWithGoogle(activity: Activity): Result<AuthUser> =
 *         suspendRunCatching {
 *             authDataSource.signInWithGoogle(activity)
 *         }
 *
 *     // Sign out
 *     suspend fun signOut(): Result<Unit> =
 *         suspendRunCatching {
 *             authDataSource.signOut()
 *         }
 * }
 * ```
 *
 * @see AuthUser
 * @see FirebaseAuth
 */
interface AuthDataSource {

    /**
     * Gets the currently authenticated user, if any.
     *
     * This is a synchronous operation that returns immediately with the cached authentication
     * state from Firebase. It does not perform any network operations.
     *
     * ## Use Cases
     * - Check if user is signed in before showing authenticated content
     * - Get user ID for database queries
     * - Display user profile information
     * - Initialize app state based on auth status
     *
     * ## Example
     * ```kotlin
     * val currentUser = authDataSource.getCurrentUser()
     * if (currentUser != null) {
     *     // User is signed in, show authenticated content
     *     loadUserData(currentUser.id)
     * } else {
     *     // User is signed out, show login screen
     *     navigateToLogin()
     * }
     * ```
     *
     * @return The currently authenticated [AuthUser], or null if not signed in.
     */
    fun getCurrentUser(): AuthUser?

    /**
     * Attempts to sign in using credentials saved in Android's Credential Manager.
     *
     * This method leverages Android's Credential Manager API to retrieve saved credentials
     * (email/password or Google account) and authenticate the user automatically. This enables
     * seamless "one-tap" sign-in without requiring the user to re-enter credentials.
     *
     * ## Behavior
     * - Shows the Credential Manager UI with saved credentials
     * - User selects a saved credential to sign in
     * - Authenticates with Firebase using the selected credential
     * - Saves/updates the credential in Credential Manager after successful sign-in
     *
     * ## Requirements
     * - Requires an Activity context for showing the credential picker UI
     * - Credentials must have been previously saved via Credential Manager
     * - User must have at least one saved credential
     *
     * ## Exceptions
     * - Throws `CancellationException` if user cancels the credential picker
     * - Throws `NoCredentialException` if no saved credentials are found
     * - Throws `FirebaseAuthException` if authentication with saved credentials fails
     *
     * ## Example
     * ```kotlin
     * // In your login ViewModel
     * fun autoSignIn() {
     *     _uiState.updateStateWith {
     *         authRepository.signInWithSavedCredentials(activity)
     *     }
     * }
     * ```
     *
     * @param activity The activity instance required for showing the credential picker UI
     * @return The authenticated [AuthUser] upon successful sign-in
     * @throws CancellationException if the user cancels the operation
     * @throws Exception for authentication failures
     */
    suspend fun signInWithSavedCredentials(activity: Activity): AuthUser

    /**
     * Signs in a user with email and password using Firebase Authentication.
     *
     * This method authenticates an existing user using their email address and password.
     * The credentials are verified against Firebase Authentication servers.
     *
     * ## Requirements
     * - Email must be a valid, registered email address
     * - Password must match the password set during registration
     * - Network connectivity is required
     *
     * ## Exceptions
     * - `FirebaseAuthInvalidCredentialsException` - Wrong password or malformed email
     * - `FirebaseAuthInvalidUserException` - User account doesn't exist or is disabled
     * - `FirebaseNetworkException` - Network connectivity issues
     *
     * ## Example
     * ```kotlin
     * // In your ViewModel
     * fun signIn(email: String, password: String) {
     *     _uiState.updateStateWith {
     *         authRepository.signIn(email, password)
     *     }
     * }
     * ```
     *
     * @param email The user's email address
     * @param password The user's password
     * @return The authenticated [AuthUser] upon successful sign-in
     * @throws FirebaseAuthException for authentication failures
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthUser

    /**
     * Registers a new user with email and password, then saves credentials to Credential Manager.
     *
     * This method creates a new user account in Firebase Authentication using the provided
     * email and password, sets the user's display name, and automatically saves the credentials
     * to Android's Credential Manager for future one-tap sign-in.
     *
     * ## Process
     * 1. Creates user account in Firebase Authentication
     * 2. Updates the user profile with the provided display name
     * 3. Saves credentials to Android Credential Manager
     * 4. Returns the authenticated user
     *
     * ## Requirements
     * - Email must be a valid, unused email address
     * - Password must meet minimum security requirements (typically 6+ characters)
     * - Requires an Activity context for saving credentials
     * - Network connectivity is required
     *
     * ## Exceptions
     * - `FirebaseAuthUserCollisionException` - Email is already registered
     * - `FirebaseAuthWeakPasswordException` - Password doesn't meet requirements
     * - `FirebaseAuthInvalidCredentialsException` - Email format is invalid
     * - `FirebaseNetworkException` - Network connectivity issues
     *
     * ## Example
     * ```kotlin
     * // In your registration ViewModel
     * fun register(name: String, email: String, password: String) {
     *     _uiState.updateStateWith {
     *         authRepository.register(name, email, password, activity)
     *     }
     * }
     * ```
     *
     * @param name The user's display name (will be set in Firebase user profile)
     * @param email The user's email address
     * @param password The user's password (must meet Firebase security requirements)
     * @param activity The activity instance required for saving credentials
     * @return The authenticated [AuthUser] upon successful registration
     * @throws FirebaseAuthException for authentication/registration failures
     */
    suspend fun registerWithEmailAndPassword(
        name: String,
        email: String,
        password: String,
        activity: Activity,
    ): AuthUser

    /**
     * Signs in an existing user with their Google account using Google Sign-In.
     *
     * This method initiates the Google Sign-In flow, allowing users to authenticate using
     * their Google account. The Google ID token is then used to authenticate with Firebase.
     *
     * ## Process
     * 1. Shows Google account picker UI
     * 2. User selects a Google account
     * 3. Retrieves Google ID token
     * 4. Authenticates with Firebase using the Google credential
     * 5. Returns the authenticated user
     *
     * ## Requirements
     * - Requires an Activity context for showing the Google Sign-In UI
     * - Google Sign-In must be enabled in Firebase Console
     * - SHA-1 certificate fingerprint must be configured in Firebase
     * - `google-services.json` must be properly configured
     * - Network connectivity is required
     *
     * ## Exceptions
     * - `CancellationException` - User cancelled the Google Sign-In flow
     * - `ApiException` - Google Sign-In API errors (invalid configuration, etc.)
     * - `FirebaseAuthException` - Firebase authentication with Google credential failed
     * - `FirebaseNetworkException` - Network connectivity issues
     *
     * ## Example
     * ```kotlin
     * // In your login ViewModel
     * fun signInWithGoogle() {
     *     _uiState.updateStateWith {
     *         authRepository.signInWithGoogle(activity)
     *     }
     * }
     * ```
     *
     * @param activity The activity instance required for showing the Google Sign-In UI
     * @return The authenticated [AuthUser] upon successful sign-in
     * @throws CancellationException if user cancels the operation
     * @throws Exception for Google Sign-In or Firebase authentication failures
     */
    suspend fun signInWithGoogle(activity: Activity): AuthUser

    /**
     * Registers a new user with their Google account using Google Sign-In.
     *
     * This method is functionally identical to [signInWithGoogle] as Firebase automatically
     * creates a new user account if the Google account hasn't been used before. The separation
     * exists to provide semantic clarity in the UI (showing "Register" vs "Sign In" flows).
     *
     * ## Process
     * 1. Shows Google account picker UI
     * 2. User selects a Google account
     * 3. Retrieves Google ID token
     * 4. Authenticates/creates account with Firebase using the Google credential
     * 5. Returns the authenticated user
     *
     * ## Requirements
     * - Same as [signInWithGoogle]
     *
     * ## Exceptions
     * - Same as [signInWithGoogle]
     *
     * ## Example
     * ```kotlin
     * // In your registration ViewModel
     * fun registerWithGoogle() {
     *     _uiState.updateStateWith {
     *         authRepository.registerWithGoogle(activity)
     *     }
     * }
     * ```
     *
     * @param activity The activity instance required for showing the Google Sign-In UI
     * @return The authenticated [AuthUser] upon successful registration/sign-in
     * @throws CancellationException if user cancels the operation
     * @throws Exception for Google Sign-In or Firebase authentication failures
     * @see signInWithGoogle
     */
    suspend fun registerWithGoogle(activity: Activity): AuthUser

    /**
     * Signs out the currently authenticated user from Firebase and clears local session.
     *
     * This method signs the user out from Firebase Authentication and clears the local
     * authentication state. Note that this does NOT clear saved credentials from Android's
     * Credential Manager - the user can still use one-tap sign-in after signing out.
     *
     * ## Behavior
     * - Clears Firebase Authentication session
     * - Clears any cached user data
     * - After sign-out, [getCurrentUser] will return null
     * - Saved credentials in Credential Manager remain available
     *
     * ## Use Cases
     * - User-initiated sign out
     * - Switching accounts
     * - Security-related sign out (e.g., suspicious activity)
     *
     * ## Thread Safety
     * This is a suspend function that performs async operations. Call from a coroutine context.
     *
     * ## Example
     * ```kotlin
     * // In your settings ViewModel
     * fun signOut() {
     *     _uiState.updateWith {
     *         authRepository.signOut()
     *     }
     * }
     * ```
     *
     * ## Note on Credential Manager
     * If you want to also clear saved credentials from Credential Manager, you need to
     * call the Credential Manager API separately:
     * ```kotlin
     * suspend fun signOutAndClearCredentials(context: Context) {
     *     authDataSource.signOut()
     *     // Additional code to clear Credential Manager credentials if needed
     * }
     * ```
     */
    suspend fun signOut()
}
