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

package dev.atick.firebase.analytics.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

/**
 * Implementation of [CrashReporter] that uses Firebase Crashlytics.
 */
internal class FirebaseCrashReporter @Inject constructor(
    private val crashlytics: FirebaseCrashlytics,
) : CrashReporter {
    /**
     * Reports an exception to Firebase Crashlytics.
     *
     * @param throwable The exception to be reported.
     */
    override fun reportException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
