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

package dev.atick.data.model.profile

import dev.atick.core.preferences.model.PreferencesUserProfile
import dev.atick.core.preferences.model.UserDataPreferences

/**
 * Domain model representing a user's profile information in the application's data layer.
 *
 * This model represents authenticated user profile data, primarily sourced from Firebase Auth
 * and cached locally in DataStore via [UserDataPreferences]. Unlike [dev.atick.data.model.home.Jetpack],
 * this model doesn't use Room database storage; instead, it relies on DataStore for lightweight
 * preference-based caching.
 *
 * Profile data flow:
 * - Firebase Auth provides initial profile data (name, photo) after sign-in
 * - [dev.atick.data.repository.profile.ProfileRepository] observes Firebase Auth state
 * - Profile is cached in DataStore for offline access and quick app startup
 * - UI layer observes profile via [dev.atick.feature.profile.ui.ProfileViewModel]
 *
 * Mapping extensions are provided for layer conversion:
 * - [UserDataPreferences.toProfile]: Convert from DataStore preferences to domain model
 * - [toPreferencesUserProfile]: Convert from domain model to DataStore preferences
 *
 * Usage context:
 * - Displayed in profile screen showing user name and avatar
 * - Used in settings screen to show current user information
 * - Updated when user changes profile via Firebase Auth methods
 * - Cached locally for offline display and fast app startup
 *
 * @property userName Display name of the authenticated user (default: empty string).
 * @property profilePictureUri Optional URI string pointing to the user's profile picture.
 *
 * @see UserDataPreferences DataStore preferences model for local caching
 * @see PreferencesUserProfile Subset of preferences specifically for profile data
 * @see dev.atick.data.repository.profile.ProfileRepository Repository providing profile operations
 * @see dev.atick.feature.profile.ui.ProfileViewModel ViewModel consuming profile data
 */
data class Profile(
    val userName: String = String(),
    val profilePictureUri: String? = null,
)

/**
 * Extension function to convert UserDataPreferences to Profile.
 *
 * @return A Profile object with data from UserDataPreferences.
 */
fun UserDataPreferences.toProfile(): Profile {
    return Profile(
        userName = userName ?: "Anonymous",
        profilePictureUri = profilePictureUriString,
    )
}

/**
 * Extension function to convert Profile to PreferencesUserProfile.
 *
 * @return A PreferencesUserProfile object with data from Profile.
 */
fun Profile.toPreferencesUserProfile(): PreferencesUserProfile {
    return PreferencesUserProfile(
        userName = userName,
        profilePictureUriString = profilePictureUri,
    )
}
