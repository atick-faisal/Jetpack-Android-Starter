/*
 * Copyright 2024 Atick Faisal
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier annotation for injecting the application-scoped CoroutineScope.
 *
 * Use this annotation when you need a CoroutineScope that lives for the entire
 * application lifecycle. This is typically used for:
 * - Application-wide background operations
 * - Data synchronization that should survive configuration changes
 * - Observing flows that need to be active as long as the app is alive
 *
 * ## Usage Example
 * ```kotlin
 * class DataSyncManager @Inject constructor(
 *     @ApplicationScope private val appScope: CoroutineScope
 * ) {
 *     init {
 *         // This will run for the entire app lifecycle
 *         appScope.launch {
 *             observeDataChanges().collect { /* sync */ }
 *         }
 *     }
 * }
 * ```
 *
 * ## When NOT to Use
 * - **ViewModels**: Use `viewModelScope` instead (automatically cancelled when ViewModel is cleared)
 * - **Composables**: Use `rememberCoroutineScope()` (cancelled when Composable leaves composition)
 * - **Short-lived operations**: Use regular suspend functions
 *
 * @see CoroutineModule.providesCoroutineScope
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

/**
 * Hilt module providing an application-scoped CoroutineScope.
 *
 * ## Why This Module Exists
 *
 * While you could create `CoroutineScope(Dispatchers.Default)` directly, injecting
 * an application scope offers several advantages:
 *
 * 1. **Lifecycle-aware**: Automatically tied to the application lifecycle
 * 2. **Testable**: Tests can inject a TestScope for deterministic execution
 * 3. **Centralized**: Single source of truth for app-wide coroutine operations
 * 4. **Structured Concurrency**: All app-level coroutines are children of this scope
 *
 * ## SupervisorJob Decision
 *
 * This scope uses `SupervisorJob()` instead of a regular `Job`. Here's why:
 *
 * ### With Regular Job (NOT USED)
 * ```
 * Parent Job fails
 *     ↓
 * All children cancelled
 *     ↓
 * Entire scope becomes unusable
 * ```
 *
 * ### With SupervisorJob (USED)
 * ```
 * One child fails
 *     ↓
 * Other children continue running
 *     ↓
 * Scope remains usable
 * ```
 *
 * ## Example Scenario
 * ```kotlin
 * @ApplicationScope scope
 *   ├─ DataSync coroutine (fails due to network error)
 *   ├─ AnalyticsLogger coroutine (keeps running ✓)
 *   └─ CacheCleanup coroutine (keeps running ✓)
 * ```
 *
 * If we used a regular Job, the network error would cancel ALL operations.
 * With SupervisorJob, only the failed coroutine is cancelled.
 *
 * ## Architecture Decision
 *
 * This template provides an ApplicationScope for operations that need to:
 * - Survive configuration changes (screen rotations)
 * - Run independently of any specific screen or ViewModel
 * - Continue even when the user navigates away
 *
 * However, **most operations should use `viewModelScope`** instead, as it's automatically
 * cancelled when the ViewModel is cleared, preventing memory leaks.
 *
 * @see ApplicationScope
 * @see SupervisorJob
 * @see viewModelScope
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {
    /**
     * Provides a singleton CoroutineScope tied to the application lifecycle.
     *
     * This scope:
     * - Lives for the entire application lifecycle
     * - Uses [SupervisorJob] to prevent one failure from cancelling all operations
     * - Uses [DefaultDispatcher] for CPU-bound background tasks
     * - Should be used sparingly - prefer `viewModelScope` for most operations
     *
     * ## When to Inject This
     * - Application-wide data synchronization
     * - Background monitoring that outlives ViewModels
     * - Singleton services that need coroutine support
     *
     * ## When NOT to Inject This
     * - ViewModels → use `viewModelScope`
     * - Composables → use `rememberCoroutineScope()`
     * - Repositories → use injected dispatchers with `withContext()`
     *
     * @param dispatcher The [DefaultDispatcher] for CPU-intensive background tasks
     * @return A singleton [CoroutineScope] with [SupervisorJob]
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun providesCoroutineScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
