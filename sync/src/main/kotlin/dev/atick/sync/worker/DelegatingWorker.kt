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

package dev.atick.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.reflect.KClass

/**
 * Hilt entry point for accessing [HiltWorkerFactory] at runtime.
 *
 * This entry point is required because WorkManager creates Worker instances itself,
 * outside of Hilt's normal dependency injection flow. By providing this entry point,
 * we can manually retrieve the HiltWorkerFactory and use it to create Workers with
 * injected dependencies.
 *
 * ## Why This Exists
 * - WorkManager instantiates Workers directly using reflection
 * - Hilt cannot automatically inject dependencies into Workers
 * - This entry point bridges the gap by providing access to HiltWorkerFactory
 *
 * @see DelegatingWorker for usage
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltWorkerFactoryEntryPoint {
    fun hiltWorkerFactory(): HiltWorkerFactory
}

private const val WORKER_CLASS_NAME = "JetpackWorkerClassName"

/**
 * Extension function to create [Data] for a [DelegatingWorker] that will delegate to this Worker class.
 *
 * This function packages the Worker's class name along with any input data into a [Data] object
 * that [DelegatingWorker] can use to instantiate the correct Worker at runtime.
 *
 * ## How It Works
 * 1. Stores the Worker's fully qualified class name in the Data object
 * 2. Adds any additional input data provided by the caller
 * 3. DelegatingWorker reads the class name and uses HiltWorkerFactory to create the Worker
 *
 * ## Usage Example
 * ```kotlin
 * // In Sync.kt
 * val syncWorkRequest = OneTimeWorkRequestBuilder<DelegatingWorker>()
 *     .setInputData(SyncWorker::class.delegatedData())  // Delegates to SyncWorker
 *     .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
 *     .setConstraints(SyncConstraints)
 *     .build()
 * ```
 *
 * ## Supported Input Data Types
 * - String
 * - Int
 * - Long
 * - Boolean
 * - Float
 * - Double
 *
 * @param inputData Optional list of key-value pairs to be added to the WorkRequest's input data
 * @return A [Data] object containing the worker class name and any additional input data
 * @throws IllegalArgumentException if an unsupported data type is provided in inputData
 *
 * @see DelegatingWorker
 * @see SyncWorker
 */
internal fun KClass<out CoroutineWorker>.delegatedData(
    inputData: List<Pair<String, Any>> = emptyList(),
) =
    Data.Builder()
        .putString(WORKER_CLASS_NAME, qualifiedName)
        .apply {
            inputData.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                    is Double -> putDouble(key, value)
                    else -> throw IllegalArgumentException("Unsupported type: ${value::class}")
                }
            }
        }
        .build()

/**
 * A proxy Worker that delegates work to another [CoroutineWorker] created via [HiltWorkerFactory].
 *
 * ## Why This Pattern Exists
 *
 * ### Problem
 * WorkManager creates Worker instances using reflection, which prevents Hilt from automatically
 * injecting dependencies. The standard solution requires configuring a custom WorkManager
 * initializer in the app module, which creates tight coupling and violates separation of concerns.
 *
 * ### Solution
 * This delegating pattern allows library modules (like `:sync`) to define Workers with injected
 * dependencies WITHOUT requiring the app module to configure WorkManager. Instead:
 * 1. The app module enqueues a generic `DelegatingWorker`
 * 2. `DelegatingWorker` receives the actual Worker class name via input data
 * 3. `DelegatingWorker` uses Hilt's entry point to get `HiltWorkerFactory`
 * 4. `HiltWorkerFactory` creates the real Worker with all dependencies injected
 * 5. `DelegatingWorker` delegates all work to the real Worker
 *
 * ## Benefits
 * - **Modular**: Library modules can define Workers without app module configuration
 * - **Type-safe**: Compile-time verification of Worker classes via `delegatedData()` extension
 * - **Clean**: App module doesn't need custom WorkManager initialization
 * - **Testable**: Real Workers can be tested independently with mock dependencies
 *
 * ## Usage Pattern
 * ```kotlin
 * // 1. Define a Worker in a library module with dependencies
 * @HiltWorker
 * class SyncWorker @AssistedInject constructor(
 *     @Assisted appContext: Context,
 *     @Assisted params: WorkerParameters,
 *     private val repository: Repository  // Injected by Hilt!
 * ) : CoroutineWorker(appContext, params) {
 *     override suspend fun doWork(): Result { /* ... */ }
 * }
 *
 * // 2. Enqueue DelegatingWorker with metadata pointing to the real Worker
 * val workRequest = OneTimeWorkRequestBuilder<DelegatingWorker>()
 *     .setInputData(SyncWorker::class.delegatedData())
 *     .build()
 *
 * WorkManager.getInstance(context).enqueue(workRequest)
 * ```
 *
 * ## Architecture Decision
 * This pattern was chosen over custom WorkManager configuration because:
 * - It keeps WorkManager initialization in the app module simple
 * - Library modules remain self-contained and reusable
 * - No need for app module to know about library module Workers
 * - Follows separation of concerns principle
 *
 * @param appContext The application context provided by WorkManager
 * @param workerParams The worker parameters containing input data with the delegate Worker class name
 *
 * @see HiltWorkerFactoryEntryPoint
 * @see delegatedData
 * @see SyncWorker
 */
class DelegatingWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    /**
     * The fully qualified class name of the Worker to delegate to.
     *
     * This is extracted from the input data provided via [delegatedData] extension function.
     */
    private val workerClassName =
        workerParams.inputData.getString(WORKER_CLASS_NAME) ?: ""

    /**
     * The actual Worker instance created by [HiltWorkerFactory] with dependencies injected.
     *
     * This Worker is instantiated using Hilt's entry point mechanism, which allows
     * accessing the dependency graph from outside of Hilt's normal injection flow.
     *
     * @throws IllegalArgumentException if the Worker class cannot be found or created
     */
    private val delegateWorker =
        EntryPointAccessors.fromApplication<HiltWorkerFactoryEntryPoint>(appContext)
            .hiltWorkerFactory()
            .createWorker(appContext, workerClassName, workerParams)
            as? CoroutineWorker
            ?: throw IllegalArgumentException("Unable to find appropriate worker")

    /**
     * Delegates foreground info retrieval to the actual Worker.
     *
     * This is required for Workers that use `setForeground()` to display a notification
     * while running (e.g., for long-running sync operations).
     *
     * @return The foreground info from the delegate Worker
     */
    override suspend fun getForegroundInfo(): ForegroundInfo =
        delegateWorker.getForegroundInfo()

    /**
     * Delegates the actual work execution to the real Worker.
     *
     * This is where the proxy pattern happens - all work is forwarded to the
     * Worker instance created with Hilt dependencies.
     *
     * @return The result from the delegate Worker (SUCCESS, RETRY, or FAILURE)
     */
    override suspend fun doWork(): Result =
        delegateWorker.doWork()
}
