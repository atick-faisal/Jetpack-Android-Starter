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

/**
 * Interface for reporting exceptions to a crash reporting service.
 *
 * This abstraction allows for flexible crash reporting implementations while keeping
 * the application code decoupled from specific crash reporting libraries. The primary
 * implementation uses Firebase Crashlytics, but alternative services can be easily
 * substituted through dependency injection.
 *
 * ## Thread Safety
 * Implementations must be thread-safe as this interface may be called from any thread.
 *
 * ## Usage
 *
 * ### In Repository Layer
 * ```kotlin
 * class UserRepository @Inject constructor(
 *     private val crashReporter: CrashReporter
 * ) {
 *     suspend fun fetchUser(): Result<User> = suspendRunCatching {
 *         api.getUser()
 *     }.onFailure { error ->
 *         crashReporter.reportException(error)
 *     }
 * }
 * ```
 *
 * ### In ViewModel (via updateStateWith/updateWith)
 * ```kotlin
 * @HiltViewModel
 * class HomeViewModel @Inject constructor(
 *     private val repository: HomeRepository,
 *     private val crashReporter: CrashReporter
 * ) : ViewModel() {
 *     fun loadData() {
 *         _uiState.updateStateWith {
 *             repository.getData().onFailure { error ->
 *                 // Report non-fatal errors while still showing error to user
 *                 crashReporter.reportException(error)
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * ### In Global Exception Handler
 * ```kotlin
 * class MyApplication : Application() {
 *     @Inject lateinit var crashReporter: CrashReporter
 *
 *     override fun onCreate() {
 *         super.onCreate()
 *         Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
 *             crashReporter.reportException(throwable)
 *         }
 *     }
 * }
 * ```
 *
 * @see FirebaseCrashReporter
 */
interface CrashReporter {
    /**
     * Reports a non-fatal exception to the crash reporting service.
     *
     * Non-fatal exceptions are logged and aggregated but do not cause the app to crash.
     * Use this to track errors that were caught and handled gracefully, but that you
     * still want visibility into for debugging and monitoring purposes.
     *
     * ## When to Use
     * - Network failures that fall back to cached data
     * - Failed background sync operations
     * - Validation errors that shouldn't crash the app
     * - Unexpected API responses that can be handled
     * - Any caught exception that indicates a problem worth tracking
     *
     * ## When NOT to Use
     * - Expected errors (e.g., user enters invalid email format)
     * - User-triggered cancellations
     * - Normal flow control exceptions
     *
     * ## Performance
     * This call is non-blocking and performs I/O operations asynchronously. It's safe
     * to call from the main thread, though the actual reporting happens in the background.
     *
     * @param throwable The exception to be reported. Should include a meaningful message
     *                  and stack trace for debugging.
     */
    fun reportException(throwable: Throwable)
}
