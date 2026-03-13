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

package dev.atick.core.ui.extensions

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.util.Consumer
import dev.atick.core.extensions.isAllPermissionsGranted
import dev.atick.core.extensions.showToast
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Creates an activity result launcher with typed success/failure callbacks.
 *
 * This helper simplifies working with [ActivityResultContracts.StartActivityForResult]
 * by providing a clean callback-based API. The launcher automatically determines
 * success based on [Activity.RESULT_OK].
 *
 * ## Usage
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     private lateinit var settingsLauncher: ActivityResultLauncher<Intent>
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         settingsLauncher = resultLauncher(
 *             onSuccess = {
 *                 showToast("Settings saved!")
 *             },
 *             onFailure = {
 *                 showToast("Settings cancelled")
 *             }
 *         )
 *
 *         // Later, launch the activity
 *         val intent = Intent(this, SettingsActivity::class.java)
 *         settingsLauncher.launch(intent)
 *     }
 * }
 * ```
 *
 * ## With ViewModel Integration
 * ```kotlin
 * class MyViewModel : ViewModel() {
 *     private val _settingsSaved = MutableSharedFlow<Unit>()
 *     val settingsSaved = _settingsSaved.asSharedFlow()
 *
 *     fun onSettingsSaved() {
 *         viewModelScope.launch {
 *             _settingsSaved.emit(Unit)
 *         }
 *     }
 * }
 *
 * // In Activity:
 * val launcher = resultLauncher(
 *     onSuccess = { viewModel.onSettingsSaved() }
 * )
 * ```
 *
 * @param onSuccess Callback invoked when the activity returns [Activity.RESULT_OK]
 * @param onFailure Callback invoked when the activity is cancelled or returns other result
 * @return [ActivityResultLauncher] that can be used to launch intents
 *
 * @see ActivityResultLauncher
 * @see ActivityResultContracts.StartActivityForResult
 */
inline fun ComponentActivity.resultLauncher(
    crossinline onSuccess: () -> Unit = {},
    crossinline onFailure: () -> Unit = {},
): ActivityResultLauncher<Intent> {
    val resultCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val success = (result.resultCode == Activity.RESULT_OK)
        if (success) {
            onSuccess.invoke()
        } else {
            onFailure.invoke()
        }
    }
    return resultCallback
}

/**
 * Creates a permission request launcher with typed success/failure callbacks.
 *
 * This helper simplifies requesting multiple runtime permissions by automatically
 * checking if all requested permissions were granted and invoking the appropriate callback.
 *
 * ## Usage
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         permissionLauncher = permissionLauncher(
 *             onSuccess = {
 *                 // All permissions granted
 *                 startCameraPreview()
 *             },
 *             onFailure = {
 *                 // At least one permission denied
 *                 showToast("Camera permission is required")
 *             }
 *         )
 *
 *         // Later, request permissions
 *         permissionLauncher.launch(
 *             arrayOf(
 *                 Manifest.permission.CAMERA,
 *                 Manifest.permission.RECORD_AUDIO
 *             )
 *         )
 *     }
 * }
 * ```
 *
 * @param onSuccess Callback invoked when ALL requested permissions are granted
 * @param onFailure Callback invoked when ANY permission is denied
 * @return [ActivityResultLauncher] that accepts an array of permission strings
 *
 * @see ActivityResultContracts.RequestMultiplePermissions
 * @see checkForPermissions
 */
inline fun ComponentActivity.permissionLauncher(
    crossinline onSuccess: () -> Unit = {},
    crossinline onFailure: () -> Unit = {},
): ActivityResultLauncher<Array<String>> {
    val resultCallback = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            onSuccess.invoke()
        } else {
            onFailure.invoke()
        }
    }
    return resultCallback
}

/**
 * Checks and requests permissions with automatic settings navigation on denial.
 *
 * This is a high-level permission helper that:
 * 1. Checks if permissions are already granted (and returns early if so)
 * 2. Requests permissions if not granted
 * 3. Automatically opens app settings if permissions are denied
 * 4. Shows a toast message prompting the user to grant permissions
 *
 * ## Usage
 * ```kotlin
 * class CameraActivity : ComponentActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         checkForPermissions(
 *             permissions = listOf(
 *                 Manifest.permission.CAMERA,
 *                 Manifest.permission.WRITE_EXTERNAL_STORAGE
 *             ),
 *             onSuccess = {
 *                 initializeCamera()
 *             }
 *         )
 *     }
 * }
 * ```
 *
 * ## Behavior on Denial
 * If the user denies permissions:
 * - Shows toast: "PLEASE ALLOW ALL PERMISSIONS"
 * - Automatically opens app settings via [openPermissionSettings]
 * - User must manually grant permissions in settings
 *
 * @param permissions List of Android permission strings to request
 * @param onSuccess Callback invoked when all permissions are granted
 *
 * @see permissionLauncher
 * @see openPermissionSettings
 */
inline fun ComponentActivity.checkForPermissions(
    permissions: List<String>,
    crossinline onSuccess: () -> Unit,
) {
    if (isAllPermissionsGranted(permissions)) return
    val launcher = permissionLauncher(
        onSuccess = onSuccess,
        onFailure = {
            showToast("PLEASE ALLOW ALL PERMISSIONS")
            openPermissionSettings()
        },
    )
    launcher.launch(permissions.toTypedArray())
}

/**
 * Opens the system app settings page for this application.
 *
 * This function navigates the user to the app's detailed settings page where they
 * can manually grant permissions, manage storage, clear data, and adjust other
 * app-specific settings.
 *
 * Typically used when permissions are denied and need to be granted manually,
 * or when users need to adjust system-level app configurations.
 *
 * ## Usage
 * ```kotlin
 * Button(onClick = { openPermissionSettings() }) {
 *     Text("Open Settings to Grant Permissions")
 * }
 * ```
 *
 * ## Automatic Usage
 * This function is automatically called by [checkForPermissions] when
 * permissions are denied.
 *
 * @see checkForPermissions
 */
fun ComponentActivity.openPermissionSettings() {
    val intent = Intent(
        ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:$packageName"),
    )
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}

/**
 * Extension property to check if the system is currently in dark theme mode.
 *
 * This property provides a convenient way to check the system's dark mode setting
 * from a [Configuration] object.
 *
 * ## Usage
 * ```kotlin
 * val isDark = resources.configuration.isSystemInDarkTheme
 * if (isDark) {
 *     // Apply dark theme specific logic
 * }
 * ```
 *
 * @return `true` if system is in dark mode, `false` otherwise
 * @see Configuration.UI_MODE_NIGHT_YES
 * @see isSystemInDarkTheme
 */
val Configuration.isSystemInDarkTheme
    get() = (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

/**
 * Observes system dark theme changes as a reactive Flow.
 *
 * This function creates a cold Flow that emits the current dark theme state
 * immediately upon collection, then emits again whenever the system theme changes.
 * The Flow automatically handles listener registration/unregistration and filters
 * out duplicate consecutive values.
 *
 * ## Usage in ViewModel
 * ```kotlin
 * @HiltViewModel
 * class ThemeViewModel @Inject constructor() : ViewModel() {
 *     // This won't work directly - need to pass Activity or use a different approach
 *     // Typically you'd observe this from the Activity and update preferences
 * }
 * ```
 *
 * ## Usage in Activity
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         lifecycleScope.launch {
 *             isSystemInDarkTheme().collect { isDark ->
 *                 Log.d("Theme", "System dark mode: $isDark")
 *                 // Update theme settings
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * ## Usage in Composable
 * ```kotlin
 * @Composable
 * fun ThemeAwareContent() {
 *     val context = LocalContext.current
 *     val activity = context.getActivity() as? ComponentActivity
 *
 *     val isDarkTheme by activity?.isSystemInDarkTheme()
 *         ?.collectAsState(initial = false)
 *         ?: remember { mutableStateOf(false) }
 *
 *     // Use isDarkTheme...
 * }
 * ```
 *
 * ## Flow Characteristics
 * - **Cold Flow**: Listener is registered only when collected
 * - **Distinct**: Filters duplicate consecutive values
 * - **Conflated**: Only keeps the latest value if collector is slow
 * - **Lifecycle-aware**: Must be collected with appropriate scope
 *
 * @return Cold [Flow] that emits `true` when system is in dark mode, `false` otherwise
 *
 * @see Configuration.isSystemInDarkTheme
 * @see callbackFlow
 */
fun ComponentActivity.isSystemInDarkTheme() = callbackFlow {
    channel.trySend(resources.configuration.isSystemInDarkTheme)

    val listener = Consumer<Configuration> {
        channel.trySend(it.isSystemInDarkTheme)
    }

    addOnConfigurationChangedListener(listener)

    awaitClose { removeOnConfigurationChangedListener(listener) }
}
    .distinctUntilChanged()
    .conflate()
