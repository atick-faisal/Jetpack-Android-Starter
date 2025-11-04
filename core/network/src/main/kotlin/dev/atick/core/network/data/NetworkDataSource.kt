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

package dev.atick.core.network.data

import dev.atick.core.network.model.NetworkPost

/**
 * Network data source for fetching remote data via REST API.
 *
 * This interface defines the contract for making network requests to retrieve data from remote
 * endpoints. It serves as an abstraction layer over the underlying REST API implementation,
 * allowing repositories to fetch network data without depending on specific HTTP client details.
 *
 * ## Design Pattern
 *
 * This follows the **Data Source Pattern**:
 * - Encapsulates all network communication logic
 * - Returns domain-agnostic network models ([NetworkPost])
 * - Throws exceptions on network errors (handled by repositories with [suspendRunCatching])
 * - Executes on IO dispatcher (automatically handled by implementation)
 *
 * ## Usage in Repositories
 *
 * ```kotlin
 * class PostsRepository @Inject constructor(
 *     private val networkDataSource: NetworkDataSource,
 *     private val localDataSource: LocalDataSource
 * ) {
 *     suspend fun syncPosts(): Result<Unit> = suspendRunCatching {
 *         val networkPosts = networkDataSource.getPosts()
 *         localDataSource.savePosts(networkPosts.map { it.toEntity() })
 *     }
 *
 *     suspend fun getPostById(id: Int): Result<Post> = suspendRunCatching {
 *         networkDataSource.getPost(id).toDomainModel()
 *     }
 * }
 * ```
 *
 * ## Error Handling
 *
 * This interface does not return [Result] types. Instead:
 * - Network errors throw [IOException]
 * - HTTP errors throw [HttpException]
 * - Repositories should wrap calls with [suspendRunCatching]
 *
 * ## Threading
 *
 * All suspend functions execute on the IO dispatcher automatically. Callers should **not**
 * wrap calls with [withContext] - the implementation handles dispatching.
 *
 * @see NetworkPost
 * @see dev.atick.core.utils.suspendRunCatching
 */
interface NetworkDataSource {

    /**
     * Fetches all posts from the remote API.
     *
     * This function makes an HTTP GET request to retrieve a list of posts. The request
     * executes on the IO dispatcher and may throw network-related exceptions.
     *
     * ## When to Use
     *
     * - Initial data sync
     * - Pull-to-refresh operations
     * - Periodic background sync
     *
     * ## Error Handling
     *
     * ```kotlin
     * suspend fun fetchPosts(): Result<List<NetworkPost>> = suspendRunCatching {
     *     networkDataSource.getPosts()
     * }
     * ```
     *
     * @return A [List] of [NetworkPost] objects from the remote server.
     * @throws IOException if network communication fails
     * @throws HttpException if the server returns an error response
     * @throws kotlinx.serialization.SerializationException if response parsing fails
     */
    suspend fun getPosts(): List<NetworkPost>

    /**
     * Fetches a single post by ID from the remote API.
     *
     * This function makes an HTTP GET request to retrieve a specific post identified by [id].
     * The request executes on the IO dispatcher and may throw network-related exceptions.
     *
     * ## When to Use
     *
     * - Loading detailed post data
     * - Refreshing a single post
     * - Deep link navigation requiring fresh data
     *
     * ## Error Handling
     *
     * ```kotlin
     * suspend fun fetchPost(id: Int): Result<NetworkPost> = suspendRunCatching {
     *     networkDataSource.getPost(id)
     * }
     * ```
     *
     * @param id The unique identifier of the post to retrieve. Must be a positive integer.
     * @return A [NetworkPost] object representing the requested post.
     * @throws IOException if network communication fails
     * @throws HttpException if the server returns an error response (e.g., 404 if post not found)
     * @throws kotlinx.serialization.SerializationException if response parsing fails
     */
    suspend fun getPost(id: Int): NetworkPost
}
