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

package dev.atick.core.utils

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.Continuation
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

/**
 * Suspends the current coroutine with a timeout, useful for operations that need time-based cancellation.
 *
 * This function combines `suspendCancellableCoroutine` with `withTimeout` to create a coroutine
 * suspension point that automatically cancels if not resumed within the specified duration.
 *
 * ## Usage Example
 * ```kotlin
 * suspend fun waitForBluetoothConnection(): BluetoothDevice {
 *     return suspendCoroutineWithTimeout(30.seconds) { continuation ->
 *         bluetoothManager.connect { device ->
 *             continuation.resume(device)
 *         }
 *     }
 * }
 * ```
 *
 * @param T The type of the result value.
 * @param timeout The maximum duration to wait before throwing [kotlinx.coroutines.TimeoutCancellationException].
 * @param block Lambda that receives a [Continuation] to resume with the result.
 * @return The result provided to the continuation's resume function.
 * @throws kotlinx.coroutines.TimeoutCancellationException if the timeout is reached.
 */
suspend inline fun <T> suspendCoroutineWithTimeout(
    timeout: Duration,
    crossinline block: (Continuation<T>) -> Unit,
): T {
    return withTimeout(timeout) {
        suspendCancellableCoroutine(block)
    }
}

/**
 * Suspends the current coroutine with a timeout in milliseconds, useful for operations that need time-based cancellation.
 *
 * This function combines `suspendCancellableCoroutine` with `withTimeout` to create a coroutine
 * suspension point that automatically cancels if not resumed within the specified time.
 *
 * ## Usage Example
 * ```kotlin
 * suspend fun waitForSensorData(): SensorData {
 *     return suspendCoroutineWithTimeout(5000L) { continuation ->
 *         sensorManager.requestData { data ->
 *             continuation.resume(data)
 *         }
 *     }
 * }
 * ```
 *
 * @param T The type of the result value.
 * @param timeMillis The maximum time in milliseconds to wait before throwing [kotlinx.coroutines.TimeoutCancellationException].
 * @param block Lambda that receives a [CancellableContinuation] to resume with the result.
 * @return The result provided to the continuation's resume function.
 * @throws kotlinx.coroutines.TimeoutCancellationException if the timeout is reached.
 */
suspend inline fun <T> suspendCoroutineWithTimeout(
    timeMillis: Long,
    crossinline block: (CancellableContinuation<T>) -> Unit,
): T {
    return withTimeout(timeMillis) {
        suspendCancellableCoroutine(block)
    }
}

/**
 * Executes a suspending operation and returns the result wrapped in [Result], with proper coroutine cancellation handling.
 *
 * This is the standard error handling utility used across all repository methods in the application.
 * It's similar to Kotlin's `runCatching` but properly handles coroutine cancellation by re-throwing
 * [CancellationException] instead of catching it.
 *
 * ## Why Use This Instead of runCatching?
 * The standard `runCatching` catches ALL exceptions, including `CancellationException`, which breaks
 * coroutine cancellation. This function preserves cancellation behavior while catching other exceptions.
 *
 * ## Usage in Repository Layer
 * ```kotlin
 * class UserRepositoryImpl @Inject constructor(
 *     private val networkDataSource: NetworkDataSource,
 *     private val localDataSource: LocalDataSource,
 *     @IoDispatcher private val ioDispatcher: CoroutineDispatcher
 * ) : UserRepository {
 *
 *     override suspend fun getUser(id: String): Result<User> = suspendRunCatching {
 *         withContext(ioDispatcher) {
 *             networkDataSource.getUser(id)
 *         }
 *     }
 *
 *     override suspend fun syncUsers(): Result<Unit> = suspendRunCatching {
 *         withContext(ioDispatcher) {
 *             val users = networkDataSource.getUsers()
 *             localDataSource.saveUsers(users)
 *         }
 *     }
 * }
 * ```
 *
 * ## Integration with State Management
 * This function is designed to work seamlessly with [updateStateWith] and [updateWith]:
 * ```kotlin
 * @HiltViewModel
 * class UserViewModel @Inject constructor(
 *     private val repository: UserRepository
 * ) : ViewModel() {
 *     fun loadUser(id: String) {
 *         _uiState.updateStateWith {
 *             repository.getUser(id) // Already wrapped in Result via suspendRunCatching
 *         }
 *     }
 * }
 * ```
 *
 * ## Exception Handling
 * - **Success**: Returns `Result.success(value)`
 * - **CancellationException**: Re-thrown to preserve coroutine cancellation
 * - **Other exceptions**: Returns `Result.failure(exception)`
 *
 * ## Best Practices
 * - Use at the repository boundary (not in ViewModels or UI)
 * - Combine with `withContext(ioDispatcher)` for IO operations
 * - Let exceptions propagate from data sources unchanged
 * - Don't nest multiple `suspendRunCatching` calls
 *
 * @param T The type of the result value.
 * @param block The suspending operation to execute.
 * @return [Result.success] with the value if successful, [Result.failure] if an exception occurred.
 * @throws CancellationException if the coroutine is cancelled (not caught).
 *
 * @see updateStateWith
 * @see updateWith
 */
suspend inline fun <T> suspendRunCatching(crossinline block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
