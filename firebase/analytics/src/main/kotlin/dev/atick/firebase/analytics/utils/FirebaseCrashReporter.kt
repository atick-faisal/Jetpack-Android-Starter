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
 *
 * This implementation reports non-fatal exceptions to Firebase Crashlytics, allowing you to
 * track errors that don't cause crashes but indicate potential issues in your app. Reports
 * are batched and uploaded in the background, with minimal impact on app performance.
 *
 * ## Setup Requirements
 * 1. Add `google-services.json` to the app module
 * 2. Ensure Firebase Crashlytics is initialized in Application.onCreate()
 * 3. Configure ProGuard/R8 mapping file upload for release builds
 *
 * ## Data Collection
 * Each crash report includes:
 * - Full stack trace
 * - Device information (model, OS version, screen size, etc.)
 * - App version and build number
 * - Custom keys and logs (if set via FirebaseCrashlytics API)
 * - User identifier (if set via FirebaseCrashlytics.setUserId())
 *
 * ## Thread Safety
 * This implementation is thread-safe and can be called from any thread.
 *
 * @property crashlytics The Firebase Crashlytics instance, injected via Hilt
 * @see CrashReporter
 */
internal class FirebaseCrashReporter @Inject constructor(
    private val crashlytics: FirebaseCrashlytics,
) : CrashReporter {
    /**
     * Reports a non-fatal exception to Firebase Crashlytics.
     *
     * The exception is recorded asynchronously and uploaded to Firebase Crashlytics servers
     * in the background. Reports are batched to minimize network usage and battery impact.
     *
     * ## Example
     * ```kotlin
     * try {
     *     riskyOperation()
     * } catch (e: Exception) {
     *     crashReporter.reportException(e)
     *     // Handle the error gracefully
     * }
     * ```
     *
     * @param throwable The exception to be reported. The full stack trace and exception
     *                  message will be included in the Crashlytics report.
     */
    override fun reportException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
