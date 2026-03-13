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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
/**
 * A sealed class that represents the result of a resource operation with loading, success, and error states.
 *
 * This class is particularly useful for offline-first architectures where you want to show cached data
 * while loading fresh data from the network. Each state can optionally include data, allowing you to
 * display stale data while fetching updates.
 *
 * ## States
 *
 * - **Success**: Operation completed successfully with data
 * - **Loading**: Operation in progress, optionally with cached/stale data
 * - **Error**: Operation failed with an error, optionally with cached/stale data
 *
 * ## Usage Example
 *
 * ```kotlin
 * // In a Repository
 * fun getUsers(): Flow<Resource<List<User>>> = networkBoundResource(
 *     query = { localDataSource.observeUsers() },
 *     fetch = { networkDataSource.getUsers() },
 *     saveFetchedResult = { users -> localDataSource.saveUsers(users) },
 *     shouldFetch = { cachedUsers -> cachedUsers.isEmpty() }
 * )
 *
 * // In a ViewModel (if not using UiState wrapper)
 * val users: StateFlow<Resource<List<User>>> = repository.getUsers()
 *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading())
 *
 * // In UI
 * when (val resource = usersState.value) {
 *     is Resource.Loading -> {
 *         // Show loading indicator
 *         // Optionally show cached data: resource.data
 *     }
 *     is Resource.Success -> {
 *         // Show fresh data: resource.data
 *     }
 *     is Resource.Error -> {
 *         // Show error message: resource.error
 *         // Optionally show stale data: resource.data
 *     }
 * }
 * ```
 *
 * ## Offline-First Pattern
 *
 * ```kotlin
 * // Loading with cached data
 * Resource.Loading(cachedData) // Shows cached data while loading
 *
 * // Error with cached data
 * Resource.Error(cachedData, exception) // Shows error but keeps cached data visible
 * ```
 *
 * @param T The type of data.
 * @property data The data result of the operation (can be null for initial loading states).
 * @property error The error that occurred during the operation (only set in Error state).
 *
 * @see networkBoundResource For creating offline-first data flows
 * @see dev.atick.core.ui.utils.UiState For the recommended state wrapper pattern
 */
sealed class Resource<T>(
    val data: T? = null,
    val error: Throwable? = null,
) {
    /**
     * Represents a successful result with data.
     *
     * This state indicates that the operation completed successfully and fresh data is available.
     *
     * @param T The type of data.
     * @param data The successfully fetched data.
     */
    class Success<T>(data: T) : Resource<T>(data)

    /**
     * Represents a loading state with optional cached data.
     *
     * This state indicates that an operation is in progress. The optional data parameter allows
     * you to show cached/stale data while fetching fresh data from the network.
     *
     * @param T The type of data.
     * @param data Optional cached data to display during loading (null for initial load).
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)

    /**
     * Represents an error state with optional cached data and error information.
     *
     * This state indicates that an operation failed. The optional data parameter allows you to
     * keep displaying cached data even when the network request fails, improving user experience
     * in offline scenarios.
     *
     * @param T The type of data.
     * @param data Optional cached data to display despite the error.
     * @param error The error that occurred during the operation.
     */
    class Error<T>(data: T? = null, error: Throwable) : Resource<T>(data, error)
}

/**
 * Creates a network-bound resource flow that implements the offline-first pattern.
 *
 * This function creates a Flow that:
 * 1. First queries the local database and emits cached data
 * 2. Decides whether to fetch from network based on `shouldFetch` predicate
 * 3. If fetching, emits Loading state with cached data
 * 4. Fetches from network, saves to database, and emits updated data
 * 5. On error, emits Error state with cached data still available
 *
 * This pattern ensures your UI always has data to display (when available) even during network
 * requests or when offline.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // In a Repository
 * override fun getUsers(): Flow<Resource<List<User>>> = networkBoundResource(
 *     query = {
 *         // Observe local database
 *         localDataSource.observeUsers()
 *     },
 *     fetch = {
 *         // Fetch from network API
 *         networkDataSource.getUsers()
 *     },
 *     saveFetchedResult = { networkUsers ->
 *         // Save network response to local database
 *         localDataSource.saveUsers(networkUsers.map { it.toEntity() })
 *     },
 *     shouldFetch = { cachedUsers ->
 *         // Fetch if cache is empty or stale
 *         cachedUsers.isEmpty() || isCacheStale()
 *     }
 * )
 * ```
 *
 * ## Advanced Example with Timestamp-Based Caching
 *
 * ```kotlin
 * override fun getArticles(): Flow<Resource<List<Article>>> = networkBoundResource(
 *     query = { localDataSource.observeArticles() },
 *     fetch = { networkDataSource.getArticles() },
 *     saveFetchedResult = { articles ->
 *         localDataSource.saveArticles(articles, fetchedAt = System.currentTimeMillis())
 *     },
 *     shouldFetch = { cachedArticles ->
 *         val cacheAge = System.currentTimeMillis() - cachedArticles.firstOrNull()?.fetchedAt ?: 0
 *         cachedArticles.isEmpty() || cacheAge > 5.minutes.inWholeMilliseconds
 *     }
 * )
 * ```
 *
 * ## Network-Only (No Cache)
 *
 * ```kotlin
 * // If you don't want caching, use empty query and save
 * override fun getCurrentWeather(): Flow<Resource<Weather>> = networkBoundResource(
 *     query = { flowOf(null) },
 *     fetch = { weatherApi.getCurrentWeather() },
 *     saveFetchedResult = { /* no-op */ },
 *     shouldFetch = { true } // Always fetch
 * )
 * ```
 *
 * @param ResultType The type of the data from the local database (what UI consumes).
 * @param RequestType The type of the data from the network API (may differ from ResultType).
 * @param query Function that returns a Flow of cached data from the local database.
 *              This Flow will be observed and emitted throughout the resource lifecycle.
 * @param fetch Suspend function that fetches fresh data from the network.
 *              Should throw exceptions on failure (will be caught and converted to Error state).
 * @param saveFetchedResult Suspend function that saves the fetched network data to local storage.
 *                          Called before emitting the updated query results.
 * @param shouldFetch Predicate that determines whether to fetch from network based on cached data.
 *                    Defaults to `true` (always fetch). Common patterns:
 *                    - `{ it.isEmpty() }` - fetch only if cache is empty
 *                    - `{ it.isEmpty() || isStale(it) }` - fetch if empty or stale
 *                    - `{ true }` - always fetch (refresh on every call)
 *                    - `{ false }` - never fetch (local-only)
 *
 * @return A Flow emitting Resource states throughout the operation lifecycle:
 *         - Resource.Loading(cachedData) - when fetch starts (if shouldFetch returns true)
 *         - Resource.Success(freshData) - when data is successfully fetched and saved
 *         - Resource.Error(cachedData, error) - if fetch fails (cached data still available)
 *         - Resource.Success(cachedData) - if shouldFetch returns false
 *
 * @see Resource For detailed information about Resource states
 */
inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchedResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true },
): Flow<Resource<ResultType>> = flow {
    val data = query().first()

    val flow = if (shouldFetch(data)) {
        emit(Resource.Loading(data))
        try {
            saveFetchedResult(fetch())
            query().map { Resource.Success(it) }
        } catch (throwable: Throwable) {
            query().map { Resource.Error(it, throwable) }
        }
    } else {
        query().map { Resource.Success(it) }
    }

    emitAll(flow)
}
