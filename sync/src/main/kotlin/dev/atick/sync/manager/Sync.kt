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
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dev.atick.sync.worker.SyncWorker
import timber.log.Timber

/**
 * Entry point for initializing background data synchronization using WorkManager.
 *
 * This object provides a simple API to start the background sync process that keeps
 * local data synchronized with remote Firebase Firestore. The sync runs automatically
 * when the app starts and can be triggered on-demand through [SyncManager].
 *
 * ## Sync Architecture
 * - **WorkManager**: Handles background execution with constraints (network connectivity)
 * - **SyncWorker**: Performs the actual sync logic (pull from Firestore, push to Firestore)
 * - **Foreground Service**: Shows sync progress notification to the user
 * - **Exponential Backoff**: Retries failed syncs up to 3 times with increasing delays
 *
 * ## Initialization
 * Call [initialize] once from your Application's `onCreate()`:
 * ```kotlin
 * class MyApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         Sync.initialize(this)
 *     }
 * }
 * ```
 *
 * ## Sync Triggers
 * 1. **App Startup**: Automatically when [initialize] is called
 * 2. **On-Demand**: Via [SyncManager.requestSync]
 * 3. **Periodic**: Can be configured with WorkManager's PeriodicWorkRequest (not implemented by default)
 *
 * ## Work Policy
 * Uses `ExistingWorkPolicy.KEEP` which means:
 * - If sync is already running, new requests are ignored
 * - If sync is pending, it won't be replaced
 * - Only one sync runs at a time to prevent conflicts
 *
 * ## Constraints
 * The sync worker only runs when:
 * - Network is available (CONNECTED)
 * - Battery is not critically low (handled by WorkManager)
 *
 * ## Observing Sync State
 * Use [SyncManager] to observe when sync is running:
 * ```kotlin
 * @HiltViewModel
 * class HomeViewModel @Inject constructor(
 *     private val syncManager: SyncManager
 * ) : ViewModel() {
 *     val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
 *         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
 * }
 * ```
 *
 * @see SyncWorker
 * @see SyncManager
 */
object Sync {
    /**
     * Initializes the sync process that keeps the app's data synchronized with Firestore.
     *
     * This method enqueues a unique WorkManager work request that:
     * 1. Runs immediately on app startup (if network is available)
     * 2. Ensures only one sync worker runs at any time
     * 3. Retries up to 3 times on failure with exponential backoff
     *
     * ## When to Call
     * Call this method **once** from your Application's `onCreate()` method:
     * ```kotlin
     * class MyApplication : Application() {
     *     override fun onCreate() {
     *         super.onCreate()
     *         Hilt.createAndroidLogger(this)
     *         Sync.initialize(this) // Initialize sync
     *     }
     * }
     * ```
     *
     * ## Behavior
     * - **First Call**: Enqueues the sync worker
     * - **Subsequent Calls**: Ignored due to `ExistingWorkPolicy.KEEP`
     * - **Network Required**: Sync only runs when network is connected
     * - **Expedited**: Runs as expedited work when possible for faster initial sync
     *
     * ## Thread Safety
     * This method is thread-safe and can be called from any thread. WorkManager
     * handles internal synchronization.
     *
     * ## Error Handling
     * - WorkManager automatically retries failed sync operations
     * - Errors are logged via Timber
     * - Failed syncs don't crash the app; they'll be retried later
     *
     * @param context The application context. Use `applicationContext` to avoid memory leaks.
     */
    fun initialize(context: Context) {
        WorkManager.getInstance(context).apply {
            // Run sync on app startup and ensure only one sync worker runs at any time
            Timber.d("Sync: initialize")
            enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                SyncWorker.startUpSyncWork(),
            )
        }
    }
}

/**
 * The unique name identifier for the sync worker.
 *
 * This constant is used to:
 * - Uniquely identify the sync worker in WorkManager's queue
 * - Ensure only one sync worker runs at a time (via `enqueueUniqueWork`)
 * - Query the sync worker's status in [SyncManager]
 *
 * ## Usage
 * ```kotlin
 * // Enqueue sync work
 * WorkManager.getInstance(context).enqueueUniqueWork(
 *     SYNC_WORK_NAME,
 *     ExistingWorkPolicy.KEEP,
 *     syncWorkRequest
 * )
 *
 * // Query sync status
 * val workInfo = WorkManager.getInstance(context)
 *     .getWorkInfosForUniqueWork(SYNC_WORK_NAME)
 * ```
 */
internal const val SYNC_WORK_NAME = "dev.atick.jetpack.sync.worker"
