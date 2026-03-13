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

package dev.atick.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.atick.data.repository.auth.AuthRepository
import dev.atick.data.repository.auth.AuthRepositoryImpl
import dev.atick.data.repository.home.HomeRepository
import dev.atick.data.repository.home.HomeRepositoryImpl
import dev.atick.data.repository.profile.ProfileRepository
import dev.atick.data.repository.profile.ProfileRepositoryImpl
import dev.atick.data.repository.settings.SettingsRepository
import dev.atick.data.repository.settings.SettingsRepositoryImpl
import javax.inject.Singleton

/**
 * Hilt module binding repository interfaces to their implementations.
 *
 * ## Why This Module Exists
 *
 * This module follows the **Dependency Inversion Principle** by:
 * 1. Defining repository contracts as interfaces (in `:data/repository`)
 * 2. Implementing those contracts (in `:data/repository/<Repository>Impl`)
 * 3. Binding implementations to interfaces via Hilt
 *
 * ## Benefits of Interface + Implementation Pattern
 *
 * ### 1. Testability
 * ```kotlin
 * // In tests, inject a fake repository
 * class FakeHomeRepository : HomeRepository {
 *     override suspend fun getJetpacks() = flowOf(testData)
 * }
 * ```
 *
 * ### 2. Flexibility
 * Can swap implementations without changing consuming code:
 * ```kotlin
 * // Switch from Firebase to REST API
 * @Binds fun bindHome(impl: HomeRepositoryRestImpl): HomeRepository
 * ```
 *
 * ### 3. Separation of Concerns
 * - Interface defines WHAT operations are available
 * - Implementation defines HOW those operations work
 * - ViewModels depend on WHAT, not HOW
 *
 * ## Why @Binds Instead of @Provides
 *
 * This module uses `@Binds` instead of `@Provides` for performance:
 *
 * ### @Provides (NOT USED)
 * ```kotlin
 * @Provides
 * fun provideRepo(impl: HomeRepositoryImpl): HomeRepository = impl
 * // Generates: return impl (runtime object creation)
 * ```
 *
 * ### @Binds (USED)
 * ```kotlin
 * @Binds
 * abstract fun bindRepo(impl: HomeRepositoryImpl): HomeRepository
 * // Generates: cast at compile-time (no runtime overhead)
 * ```
 *
 * **Result**: `@Binds` is more efficient because it's just a type cast, not object creation.
 *
 * ## Why @Singleton
 *
 * All repositories are annotated with `@Singleton`, which means:
 *
 * ### What It Does
 * - Repository instance is created once and reused across the app
 * - Same instance is injected into all ViewModels
 * - In-memory state is shared (if any)
 *
 * ### Why This Is Correct
 * 1. **Performance**: Avoid creating duplicate repository instances
 * 2. **Consistency**: All ViewModels see the same data source
 * 3. **Resource Management**: Database and network clients are expensive to create
 * 4. **Cache Sharing**: Any in-memory caching is shared across ViewModels
 *
 * ### Example
 * ```kotlin
 * // Both ViewModels get THE SAME repository instance
 * class HomeViewModel @Inject constructor(
 *     private val repo: HomeRepository  // Instance #1
 * )
 *
 * class ItemViewModel @Inject constructor(
 *     private val repo: HomeRepository  // Same instance #1
 * )
 * ```
 *
 * ## Why `internal` Visibility
 *
 * Functions are marked `internal` to:
 * - Prevent external modules from depending on implementation details
 * - Keep implementation details within the `:data` module
 * - Ensure only interfaces are exposed to feature modules
 *
 * ## Architecture Decision
 *
 * This pattern was chosen because:
 * - **Clean Architecture**: Separates contracts from implementation
 * - **Testability**: Easy to mock repositories in unit tests
 * - **Flexibility**: Can swap data sources (Firebase → REST → Mock) without changing ViewModels
 * - **Compile Safety**: ViewModels compile against interfaces, not implementations
 *
 * @see AuthRepository
 * @see HomeRepository
 * @see ProfileRepository
 * @see SettingsRepository
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    /**
     * Binds [AuthRepositoryImpl] to the [AuthRepository] interface.
     *
     * This enables dependency injection of [AuthRepository] throughout the app.
     * The implementation handles Firebase Authentication operations (sign in, sign up, sign out).
     *
     * @param authRepositoryImpl The implementation injected by Hilt (constructor-injected dependencies)
     * @return The bound [AuthRepository] interface
     */
    @Binds
    @Singleton
    internal abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl,
    ): AuthRepository

    /**
     * Binds [HomeRepositoryImpl] to the [HomeRepository] interface.
     *
     * This enables dependency injection of [HomeRepository] throughout the app.
     * The implementation handles Jetpack data operations with offline-first architecture
     * (Room + Firebase Firestore sync).
     *
     * @param homeRepositoryImpl The implementation injected by Hilt (constructor-injected dependencies)
     * @return The bound [HomeRepository] interface
     */
    @Binds
    @Singleton
    internal abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl,
    ): HomeRepository

    /**
     * Binds [ProfileRepositoryImpl] to the [ProfileRepository] interface.
     *
     * This enables dependency injection of [ProfileRepository] throughout the app.
     * The implementation handles user profile operations (Firebase Auth + DataStore caching).
     *
     * @param profileRepositoryImpl The implementation injected by Hilt (constructor-injected dependencies)
     * @return The bound [ProfileRepository] interface
     */
    @Binds
    @Singleton
    internal abstract fun binProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl,
    ): ProfileRepository

    /**
     * Binds [SettingsRepositoryImpl] to the [SettingsRepository] interface.
     *
     * This enables dependency injection of [SettingsRepository] throughout the app.
     * The implementation handles app settings persistence (theme, language) via DataStore.
     *
     * @param settingsRepositoryImpl The implementation injected by Hilt (constructor-injected dependencies)
     * @return The bound [SettingsRepository] interface
     */
    @Binds
    @Singleton
    internal abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl,
    ): SettingsRepository
}
