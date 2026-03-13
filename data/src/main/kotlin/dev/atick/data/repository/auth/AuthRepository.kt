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

package dev.atick.data.repository.auth

import android.app.Activity

/**
 * Interface defining authentication-related operations.
 *
 * This repository provides authentication methods for Firebase Auth, including email/password
 * and Google Sign-In. It wraps the Firebase Auth SDK to provide a consistent repository pattern
 * with Result-based error handling.
 *
 * @see AuthRepositoryImpl Implementation class with Firebase Auth integration
 */
interface AuthRepository {
    /**
     * Sign in with saved credentials.
     *
     * @param activity The activity instance.
     * @return A [Result] representing the sign-in operation result. It contains [Unit] if
     * the sign-in was successful, or an error if there was a problem.
     */
    suspend fun signInWithSavedCredentials(activity: Activity): Result<Unit>

    /**
     * Sign in with an email and password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @return A [Result] representing the sign-in operation result. It contains [Unit] if
     * the sign-in was successful, or an error if there was a problem.
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit>

    /**
     * Register a new user with an email and password.
     *
     * @param name The user's name.
     * @param email The user's email address.
     * @param password The user's password.
     * @return A [Result] representing the registration operation result. It contains [Unit] if
     * the registration was successful, or an error if there was a problem.
     */
    suspend fun registerWithEmailAndPassword(
        name: String,
        email: String,
        password: String,
        activity: Activity,
    ): Result<Unit>

    /**
     * Sign in with Google.
     *
     * @return A [Result] representing the sign-in operation result. It contains [Unit] if
     * the sign-in was successful, or an error if there was a problem.
     */
    suspend fun signInWithGoogle(activity: Activity): Result<Unit>

    /**
     * Register a new user with Google.
     *
     * @param activity The activity used to launch the Google sign-in intent.
     * @return A [Result] representing the registration operation result. It contains [Unit] if
     * the registration was successful, or an error if there was a problem.
     */
    suspend fun registerWithGoogle(activity: Activity): Result<Unit>
}
