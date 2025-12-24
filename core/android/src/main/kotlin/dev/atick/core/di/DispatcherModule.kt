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

package dev.atick.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

/**
 * Hilt module providing qualified coroutine dispatchers for dependency injection.
 *
 * ## Why This Module Exists
 *
 * While Kotlin provides `Dispatchers.IO`, `Dispatchers.Default`, and `Dispatchers.Main` directly,
 * injecting them via Hilt offers several advantages:
 *
 * 1. **Testability**: Tests can inject `TestDispatcher` to control coroutine execution
 * 2. **Flexibility**: Can swap dispatcher implementations without changing consuming code
 * 3. **Consistency**: Enforces using the correct dispatcher for each task type
 * 4. **Type Safety**: Qualifiers prevent accidental dispatcher misuse
 *
 * ## Usage Guidelines
 *
 * ### IoDispatcher - For I/O Operations
 * Use for blocking I/O tasks:
 * - Network requests (Retrofit calls, HTTP downloads)
 * - Database operations (Room queries, inserts, updates)
 * - File system operations (reading/writing files)
 * - DataStore reads/writes
 *
 * ```kotlin
 * class Repository @Inject constructor(
 *     @IoDispatcher private val ioDispatcher: CoroutineDispatcher
 * ) {
 *     suspend fun fetchData() = withContext(ioDispatcher) {
 *         database.query() // Runs on IO dispatcher
 *     }
 * }
 * ```
 *
 * ### DefaultDispatcher - For CPU-Intensive Tasks
 * Use for computational work:
 * - JSON parsing (large payloads)
 * - Data transformation (mapping large lists)
 * - Image processing
 * - Complex calculations
 *
 * ```kotlin
 * class DataProcessor @Inject constructor(
 *     @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
 * ) {
 *     suspend fun processLargeDataset() = withContext(defaultDispatcher) {
 *         data.map { /* complex transformation */ }
 *     }
 * }
 * ```
 *
 * ### MainDispatcher - For UI Updates
 * **Rarely needed in this architecture!** Most UI updates happen via:
 * - `StateFlow.collectAsStateWithLifecycle()` (automatically dispatches to Main)
 * - Compose recomposition (automatically on Main thread)
 *
 * Only use when you need explicit main thread dispatch:
 * ```kotlin
 * class LegacyViewHelper @Inject constructor(
 *     @MainDispatcher private val mainDispatcher: CoroutineDispatcher
 * ) {
 *     suspend fun updateView() = withContext(mainDispatcher) {
 *         // Direct view manipulation
 *     }
 * }
 * ```
 *
 * ## Architecture Decision
 *
 * This template uses **injected dispatchers** instead of hardcoded `Dispatchers.IO` because:
 * - Repositories can be unit tested with `StandardTestDispatcher`
 * - Tests run synchronously and deterministically
 * - No need for `runTest` or `advanceUntilIdle()` when dispatchers are injected
 * - Future flexibility to add custom dispatchers (e.g., limited concurrency)
 *
 * @see IoDispatcher
 * @see DefaultDispatcher
 * @see MainDispatcher
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    /**
     * Provides the default coroutine dispatcher for CPU-intensive operations.
     *
     * Backed by a shared pool of threads equal to the number of CPU cores.
     * Optimized for tasks that consume CPU cycles (parsing, transformations, calculations).
     *
     * @return [Dispatchers.Default]
     */
    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    /**
     * Provides the I/O coroutine dispatcher for blocking I/O operations.
     *
     * Backed by a shared pool designed for offloading blocking I/O tasks.
     * Optimized for tasks that spend most of their time waiting (network, disk, database).
     *
     * @return [Dispatchers.IO]
     */
    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Provides the main coroutine dispatcher for UI thread operations.
     *
     * Backed by the main/UI thread. **Use sparingly** - most UI updates in Compose
     * happen automatically via `collectAsStateWithLifecycle()`.
     *
     * @return [Dispatchers.Main]
     */
    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

/**
 * Qualifier annotation for injecting the Default coroutine dispatcher.
 *
 * Use this annotation when you need a dispatcher for CPU-intensive operations
 * like data processing, transformations, or complex calculations.
 *
 * ## Usage Example
 * ```kotlin
 * class DataProcessor @Inject constructor(
 *     @DefaultDispatcher private val dispatcher: CoroutineDispatcher
 * )
 * ```
 *
 * @see DispatcherModule.providesDefaultDispatcher
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

/**
 * Qualifier annotation for injecting the I/O coroutine dispatcher.
 *
 * Use this annotation when you need a dispatcher for blocking I/O operations
 * like network requests, database queries, or file system operations.
 *
 * ## Usage Example
 * ```kotlin
 * class Repository @Inject constructor(
 *     @IoDispatcher private val ioDispatcher: CoroutineDispatcher
 * )
 * ```
 *
 * This is the **most commonly used dispatcher** in repositories and data sources.
 *
 * @see DispatcherModule.providesIoDispatcher
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

/**
 * Qualifier annotation for injecting the Main coroutine dispatcher.
 *
 * Use this annotation when you need explicit main thread dispatch for UI operations.
 * **Note**: In Compose-based apps, this is rarely needed as `collectAsStateWithLifecycle()`
 * automatically dispatches to the main thread.
 *
 * ## Usage Example
 * ```kotlin
 * class ViewHelper @Inject constructor(
 *     @MainDispatcher private val mainDispatcher: CoroutineDispatcher
 * )
 * ```
 *
 * @see DispatcherModule.providesMainDispatcher
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher
