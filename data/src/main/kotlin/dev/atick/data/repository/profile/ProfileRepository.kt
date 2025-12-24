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

package dev.atick.data.repository.profile

import dev.atick.data.model.profile.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing profile-related operations.
 *
 * This repository observes Firebase Auth state to provide profile information and handle
 * sign-out operations. Profile data is cached locally for offline access.
 *
 * @see ProfileRepositoryImpl Implementation class with Firebase Auth integration
 */
interface ProfileRepository {
    /**
     * Retrieves the profile information.
     *
     * @return A Flow emitting the Profile object.
     */
    fun getProfile(): Flow<Profile>

    /**
     * Signs out the current user.
     *
     * @return A Result indicating the success or failure of the operation.
     */
    suspend fun signOut(): Result<Unit>
}
