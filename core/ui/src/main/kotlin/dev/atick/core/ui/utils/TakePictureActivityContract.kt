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

package dev.atick.core.ui.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper

/**
 * Activity result contract for capturing photos with the camera.
 *
 * This contract simplifies camera integration by handling the camera intent and
 * returning both the success status and the URI where the photo was saved.
 * Unlike the standard `TakePicture` contract, this returns the URI in the result.
 *
 * ## Setup in Composable
 * ```kotlin
 * @Composable
 * fun ProfileScreen(
 *     onPhotoTaken: (Uri) -> Unit
 * ) {
 *     val context = LocalContext.current
 *     val photoUri = remember {
 *         FileProvider.getUriForFile(
 *             context,
 *             "${context.packageName}.fileprovider",
 *             File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
 *         )
 *     }
 *
 *     val takePictureLauncher = rememberLauncherForActivityResult(
 *         contract = TakePictureActivityContract()
 *     ) { (success, uri) ->
 *         if (success) {
 *             onPhotoTaken(uri)
 *         }
 *     }
 *
 *     Button(onClick = { takePictureLauncher.launch(photoUri) }) {
 *         Text("Take Photo")
 *     }
 * }
 * ```
 *
 * ## With Permission Handling
 * ```kotlin
 * val cameraPermissionLauncher = rememberLauncherForActivityResult(
 *     contract = ActivityResultContracts.RequestPermission()
 * ) { granted ->
 *     if (granted) {
 *         takePictureLauncher.launch(photoUri)
 *     }
 * }
 *
 * Button(onClick = {
 *     when (PackageManager.PERMISSION_GRANTED) {
 *         ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
 *             takePictureLauncher.launch(photoUri)
 *         }
 *         else -> {
 *             cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
 *         }
 *     }
 * }) {
 *     Text("Take Photo")
 * }
 * ```
 *
 * ## FileProvider Configuration
 * Add to `AndroidManifest.xml`:
 * ```xml
 * <provider
 *     android:name="androidx.core.content.FileProvider"
 *     android:authorities="${applicationId}.fileprovider"
 *     android:exported="false"
 *     android:grantUriPermissions="true">
 *     <meta-data
 *         android:name="android.support.FILE_PROVIDER_PATHS"
 *         android:resource="@xml/file_paths" />
 * </provider>
 * ```
 *
 * Create `res/xml/file_paths.xml`:
 * ```xml
 * <?xml version="1.0" encoding="utf-8"?>
 * <paths>
 *     <cache-path name="camera_photos" path="/" />
 * </paths>
 * ```
 *
 * ## Result Handling
 * The contract returns `Pair<Boolean, Uri>`:
 * - `first`: `true` if photo was captured, `false` if user cancelled
 * - `second`: URI where the photo was saved (same as input URI)
 *
 * @see ActivityResultContract
 */
class TakePictureActivityContract : ActivityResultContract<Uri, Pair<Boolean, Uri>>() {

    private lateinit var imageUri: Uri

    /**
     * Creates an intent to launch the camera for capturing a photo.
     *
     * @param context The context used to create the intent
     * @param input The URI where the captured photo should be saved
     * @return Camera intent configured to save the photo at the specified URI
     */
    @CallSuper
    override fun createIntent(context: Context, input: Uri): Intent {
        imageUri = input
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, input)
    }

    /**
     * Returns null as this contract requires launching an activity.
     *
     * @param context The context
     * @param input The input URI
     * @return Always null (no synchronous result possible)
     */
    override fun getSynchronousResult(
        context: Context,
        input: Uri,
    ): SynchronousResult<Pair<Boolean, Uri>>? = null

    /**
     * Parses the camera activity result.
     *
     * @param resultCode The result code from the camera activity
     * @param intent The result intent (may be null)
     * @return Pair of (success status, photo URI)
     */
    @Suppress("AutoBoxing")
    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Boolean, Uri> {
        return (resultCode == Activity.RESULT_OK) to imageUri
    }
}
