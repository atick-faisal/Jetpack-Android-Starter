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

package dev.atick.sync.manager

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.atick.data.utils.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of [SyncManager] that manages background data synchronization state.
 *
 * This class provides a reactive interface to:
 * - Observe the current sync status (is sync running or not)
 * - Trigger on-demand sync operations
 * - Query WorkManager for sync worker state
 *
 * ## Usage in Repository
 * ```kotlin
 * class DataRepository @Inject constructor(
 *     private val syncManager: SyncManager
 * ) {
 *     // Observe sync state
 *     val isSyncing: Flow<Boolean> = syncManager.isSyncing
 *
 *     // Trigger sync manually
 *     fun refreshData() {
 *         syncManager.requestSync()
 *     }
 * }
 * ```
 *
 * ## Usage in ViewModel
 * ```kotlin
 * @HiltViewModel
 * class HomeViewModel @Inject constructor(
 *     private val syncManager: SyncManager
 * ) : ViewModel() {
 *     // Expose sync state to UI
 *     val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
 *         .stateIn(
 *             scope = viewModelScope,
 *             started = SharingStarted.WhileSubscribed(5000),
 *             initialValue = false
 *         )
 *
 *     // Pull-to-refresh action
 *     fun onRefresh() {
 *         syncManager.requestSync()
 *     }
 * }
 * ```
 *
 * ## Usage in UI
 * ```kotlin
 * @Composable
 * fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
 *     val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
 *
 *     PullRefreshIndicator(
 *         refreshing = isSyncing,
 *         onRefresh = { viewModel.onRefresh() }
 *     )
 * }
 * ```
 *
 * @param context The application context for accessing WorkManager
 * @see SyncManager
 * @see Sync
 * @see SyncWorker
 */
internal class SyncManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SyncManager {
    /**
     * A reactive Flow that emits the current sync status.
     *
     * Emits `true` when a sync operation is in progress, `false` otherwise.
     * This Flow is backed by WorkManager's `getWorkInfosForUniqueWorkFlow` which
     * automatically updates whenever the sync worker's state changes.
     *
     * ## Behavior
     * - **Initial Value**: Emits immediately with current sync state
     * - **Updates**: Emits whenever sync state changes (ENQUEUED → RUNNING → SUCCEEDED/FAILED)
     * - **Conflated**: Only the latest value is kept (drops intermediate values if consumer is slow)
     * - **Hot Flow**: Backed by WorkManager's LiveData, starts emitting immediately
     *
     * ## Sync States
     * - `true`: Sync worker is in RUNNING state
     * - `false`: Sync worker is ENQUEUED, SUCCEEDED, FAILED, CANCELLED, or doesn't exist
     *
     * ## Example
     * ```kotlin
     * // In ViewModel
     * val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
     *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
     *
     * // In Composable
     * val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
     * if (isSyncing) {
     *     CircularProgressIndicator()
     * }
     * ```
     *
     * ## Thread Safety
     * This Flow is thread-safe and can be collected from any coroutine context.
     * WorkManager handles internal synchronization.
     */
    override val isSyncing: Flow<Boolean> =
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            .map(List<WorkInfo>::anyRunning)
            .conflate()

    /**
     * Requests an on-demand sync operation.
     *
     * This method triggers a background sync by calling [Sync.initialize], which
     * enqueues a unique sync work request. If a sync is already running or pending,
     * the request is ignored due to `ExistingWorkPolicy.KEEP`.
     *
     * ## When to Call
     * - User triggers pull-to-refresh
     * - User manually taps "Sync Now" button
     * - App returns to foreground after being backgrounded
     * - After important data changes that should be synced immediately
     *
     * ## Behavior
     * - **If no sync is running**: Enqueues and starts a new sync
     * - **If sync is already running**: Request is ignored (no duplicate sync)
     * - **If sync is pending**: Existing pending sync is kept (no replacement)
     * - **Network Required**: Sync only runs when network is available
     *
     * ## Example
     * ```kotlin
     * // In ViewModel
     * fun onRefresh() {
     *     syncManager.requestSync()
     *     // UI will automatically show loading via isSyncing Flow
     * }
     *
     * // In Repository
     * suspend fun saveItem(item: Item) {
     *     localDataSource.insert(item)
     *     syncManager.requestSync() // Sync immediately after local change
     * }
     * ```
     *
     * ## Performance
     * This method is lightweight and returns immediately. The actual sync work
     * happens asynchronously in the background via WorkManager.
     *
     * ## Thread Safety
     * This method is thread-safe and can be called from any thread.
     */
    override fun requestSync() {
        Timber.d("Requesting sync")
        Sync.initialize(context)
    }
}

/**
 * Extension function to check if any WorkInfo in the list is currently running.
 *
 * This is a convenience function used to determine sync status from WorkManager's
 * work info list. A sync is considered "running" only if its state is [State.RUNNING].
 *
 * ## WorkInfo States
 * - **ENQUEUED**: Work is scheduled but not yet running
 * - **RUNNING**: Work is actively executing (this function returns `true`)
 * - **SUCCEEDED**: Work completed successfully
 * - **FAILED**: Work failed after all retries
 * - **BLOCKED**: Work is waiting for prerequisites
 * - **CANCELLED**: Work was cancelled
 *
 * @receiver List of [WorkInfo] from WorkManager
 * @return `true` if any work is in RUNNING state, `false` otherwise
 */
private fun List<WorkInfo>.anyRunning() = any { it.state == State.RUNNING }
