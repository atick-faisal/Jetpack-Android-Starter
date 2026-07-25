/*
 * Copyright 2026 Atick Faisal
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

package dev.atick.core.testing.data

import android.app.Activity
import dev.atick.firebase.auth.data.AuthDataSource
import dev.atick.firebase.auth.model.AuthUser

/**
 * In-memory [AuthDataSource] for tests, so repository tests do not need Firebase.
 *
 * The `signInWith*` methods take an [Activity], which a unit test has no way to produce. They
 * are left unimplemented on purpose: a test that reaches one is testing something that belongs
 * in an instrumented test, and failing loudly says so.
 *
 * @param initialUser The signed-in user, or null for signed out.
 */
class FakeAuthDataSource(
    initialUser: AuthUser? = null,
) : AuthDataSource {

    private var currentUser: AuthUser? = initialUser

    /** Set to have [signOut] fail, for testing the error path of a repository. */
    var signOutError: Throwable? = null

    /** How many times [signOut] has been called. */
    var signOutCount: Int = 0
        private set

    override fun getCurrentUser(): AuthUser? = currentUser

    override suspend fun signOut() {
        signOutCount++
        signOutError?.let { throw it }
        currentUser = null
    }

    override suspend fun signInWithSavedCredentials(activity: Activity): AuthUser =
        unsupported()

    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthUser =
        unsupported()

    override suspend fun registerWithEmailAndPassword(
        name: String,
        email: String,
        password: String,
        activity: Activity,
    ): AuthUser = unsupported()

    override suspend fun signInWithGoogle(activity: Activity): AuthUser = unsupported()

    override suspend fun registerWithGoogle(activity: Activity): AuthUser = unsupported()

    private fun unsupported(): Nothing = throw UnsupportedOperationException(
        "Activity-based sign-in cannot run in a unit test; cover it with an instrumented test.",
    )
}
