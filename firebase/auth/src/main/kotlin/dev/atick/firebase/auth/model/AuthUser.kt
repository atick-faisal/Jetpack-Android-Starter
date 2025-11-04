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

package dev.atick.firebase.auth.model

import android.net.Uri
import com.google.firebase.auth.FirebaseUser

/**
 * Represents an authenticated user with basic profile information.
 *
 * This is a simplified user model extracted from Firebase Authentication that contains
 * only the essential user information needed by the application. It serves as a layer
 * of abstraction between Firebase-specific types and your app's domain.
 *
 * ## Usage in Repository
 * ```kotlin
 * class AuthRepository @Inject constructor(
 *     private val authDataSource: AuthDataSource
 * ) {
 *     suspend fun signIn(email: String, password: String): Result<AuthUser> =
 *         suspendRunCatching {
 *             authDataSource.signInWithEmailAndPassword(email, password)
 *         }
 *
 *     fun getCurrentUser(): AuthUser? = authDataSource.getCurrentUser()
 * }
 * ```
 *
 * ## Usage in ViewModel
 * ```kotlin
 * @HiltViewModel
 * class ProfileViewModel @Inject constructor(
 *     private val authRepository: AuthRepository
 * ) : ViewModel() {
 *     val currentUser: AuthUser? = authRepository.getCurrentUser()
 *
 *     fun loadProfile() {
 *         currentUser?.let { user ->
 *             // Use user.id, user.name, user.profilePictureUri
 *         }
 *     }
 * }
 * ```
 *
 * @property id The unique identifier for the user (Firebase UID). This is stable across
 *              all authentication providers and should be used as the primary key for
 *              user-related data in databases.
 * @property name The user's display name. May be empty if not provided during registration
 *                or if the authentication provider doesn't provide a name.
 * @property profilePictureUri The URI for the user's profile picture, or null if not available.
 *                             For Google Sign-In, this is typically the user's Google profile photo.
 *                             For email/password, this is null unless explicitly set.
 * @see FirebaseUser
 */
data class AuthUser(
    val id: String,
    val name: String,
    val profilePictureUri: Uri?,
)

/**
 * Converts a [FirebaseUser] to an [AuthUser].
 *
 * This extension function extracts the essential user information from Firebase's
 * [FirebaseUser] object into the app's simplified [AuthUser] model. It handles null
 * values safely by using empty strings for missing display names.
 *
 * ## Example
 * ```kotlin
 * val firebaseUser: FirebaseUser = Firebase.auth.currentUser!!
 * val authUser: AuthUser = firebaseUser.asAuthUser()
 * ```
 *
 * ## Field Mapping
 * - `FirebaseUser.uid` → `AuthUser.id`
 * - `FirebaseUser.displayName` → `AuthUser.name` (empty string if null)
 * - `FirebaseUser.photoUrl` → `AuthUser.profilePictureUri` (null if not set)
 *
 * @receiver The Firebase user object to convert
 * @return The corresponding [AuthUser] object with extracted information
 */
fun FirebaseUser.asAuthUser() = AuthUser(
    id = uid,
    name = displayName.orEmpty(),
    profilePictureUri = photoUrl,
)
