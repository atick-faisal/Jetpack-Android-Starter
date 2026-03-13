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

package dev.atick.core.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Recursively retrieves the ComponentActivity from any Context.
 *
 * This function is useful when you have a Context (e.g., from a composable's LocalContext)
 * and need to access Activity-specific APIs like requesting permissions, accessing the
 * lifecycle, or showing dialogs that require an Activity reference.
 *
 * The function recursively unwraps ContextWrapper instances until it finds a ComponentActivity
 * or reaches a context that is not a wrapper.
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // In a Composable - get activity for permission requests
 * @Composable
 * fun MyScreen() {
 *     val context = LocalContext.current
 *     val activity = context.getActivity()
 *
 *     Button(onClick = {
 *         activity?.let {
 *             // Request permissions using activity
 *             ActivityCompat.requestPermissions(
 *                 it,
 *                 arrayOf(Manifest.permission.CAMERA),
 *                 REQUEST_CODE
 *             )
 *         }
 *     }) {
 *         Text("Request Camera Permission")
 *     }
 * }
 *
 * // Access lifecycle in a utility function
 * fun observeLifecycle(context: Context) {
 *     context.getActivity()?.lifecycle?.addObserver(myObserver)
 * }
 *
 * // Show a dialog that requires Activity
 * fun showDialog(context: Context) {
 *     context.getActivity()?.let { activity ->
 *         AlertDialog.Builder(activity)
 *             .setTitle("Title")
 *             .show()
 *     }
 * }
 * ```
 *
 * @receiver Context Any Android context (Activity, Application, Service context, etc.).
 * @return The ComponentActivity if this context is or wraps an Activity, null otherwise.
 *         Returns null for Application context, Service context, or other non-Activity contexts.
 *
 * @see androidx.activity.ComponentActivity
 * @see android.content.ContextWrapper
 */
fun Context.getActivity(): ComponentActivity? {
    return when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }
}

/**
 * Displays a short toast message.
 *
 * @param message The message to be displayed in the toast.
 */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Checks if the app has a given permission.
 *
 * @param permission The permission to check.
 * @return `true` if the permission is granted, `false` otherwise.
 */
fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * Checks if all the given permissions are granted.
 *
 * @param permissions List of permissions to check.
 * @return `true` if all permissions are granted, `false` otherwise.
 */
fun Context.isAllPermissionsGranted(permissions: List<String>): Boolean {
    return permissions.all { hasPermission(it) }
}

/**
 * Annotation to define the valid options for notification importance levels.
 *
 * @property NotificationManager.IMPORTANCE_DEFAULT Default importance level.
 * @property NotificationManager.IMPORTANCE_HIGH High importance level.
 * @property NotificationManager.IMPORTANCE_LOW Low importance level.
 * @property NotificationManager.IMPORTANCE_MIN Minimum importance level.
 * @property NotificationManager.IMPORTANCE_NONE No importance level.
 * @property NotificationManager.IMPORTANCE_UNSPECIFIED Unspecified importance level.
 */
@IntDef(
    NotificationManager.IMPORTANCE_DEFAULT,
    NotificationManager.IMPORTANCE_HIGH,
    NotificationManager.IMPORTANCE_LOW,
    NotificationManager.IMPORTANCE_MIN,
    NotificationManager.IMPORTANCE_NONE,
    NotificationManager.IMPORTANCE_UNSPECIFIED,
)
@Retention(AnnotationRetention.SOURCE)
annotation class Options

/**
 * Creates a notification channel with the specified channel ID, name, description, and importance.
 *
 * @param channelId The ID of the notification channel.
 * @param channelName The name of the notification channel.
 * @param channelDescription The description of the notification channel.
 * @param importance The importance level of the notification channel.
 */
fun Context.createNotificationChannel(
    channelId: String,
    @StringRes channelName: Int,
    @StringRes channelDescription: Int,
    @Options importance: Int,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            getString(channelName),
            importance,
        ).apply {
            description = getString(channelDescription)
        }
        val notificationManager: NotificationManager? =
            getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        notificationManager?.createNotificationChannel(channel)
    }
}

/**
 * Creates a notification using the specified channel ID, title, content, and icon.
 *
 * @param channelId The ID of the notification channel.
 * @param title The title of the notification.
 * @param content The content of the notification.
 * @param icon The icon of the notification.
 * @return The notification object.
 */
fun Context.createNotification(
    channelId: String,
    @StringRes title: Int,
    @StringRes content: Int,
    icon: Int,
    pendingIntent: PendingIntent? = null,
): Notification {
    return NotificationCompat.Builder(
        this,
        channelId,
    )
        .setSmallIcon(icon)
        .setContentTitle(getString(title))
        .setContentText(getString(content))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setOnlyAlertOnce(true)
        .setContentIntent(pendingIntent)
        .build()
}

/**
 * Creates a notification using the specified channel ID, title, content, and icon.
 *
 * @param channelId The ID of the notification channel.
 * @param title The title of the notification.
 * @param content The content of the notification.
 * @param icon The icon of the notification.
 * @return The notification object.
 */
fun Context.createNotification(
    channelId: String,
    title: String,
    content: String,
    icon: Int,
    pendingIntent: PendingIntent? = null,
): Notification {
    return NotificationCompat.Builder(
        this,
        channelId,
    )
        .setSmallIcon(icon)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setOnlyAlertOnce(true)
        .setContentIntent(pendingIntent)
        .build()
}

/**
 * Creates a progress notification using the specified channel ID, title, total, current, and icon.
 *
 * @param channelId The ID of the notification channel.
 * @param title The title of the notification.
 * @param total The total progress value.
 * @param current The current progress value.
 * @param icon The icon of the notification.
 * @return The notification object.
 */
fun Context.createProgressNotification(
    channelId: String,
    @StringRes title: Int,
    total: Int,
    current: Int,
    @DrawableRes icon: Int,
    pendingIntent: PendingIntent? = null,
): Notification {
    return NotificationCompat.Builder(
        this,
        channelId,
    )
        .setSmallIcon(icon)
        // TODO: Generalize string formatting for title
        .setContentTitle(getString(title, current, total))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setProgress(total, current, false)
        .setContentIntent(pendingIntent)
        .build()
}

/**
 * Shows a notification using the specified notification ID and notification object.
 *
 * @param notificationId The ID of the notification.
 * @param notification The notification object to be shown.
 */
@SuppressLint("MissingPermission")
fun Context.showNotification(
    notificationId: Int,
    notification: Notification,
) {
    if (hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, notification)
        }
    }
}

/**
 * Cancels a previously shown notification.
 *
 * @param notificationId The ID of the notification to be canceled.
 */
fun Context.cancelNotification(notificationId: Int) {
    with(NotificationManagerCompat.from(this)) {
        cancel(notificationId)
    }
}

/**
 * Retrieves a temporary file URI for the specified app ID.
 *
 * @param appId The ID of the app.
 * @return The URI of the temporary file.
 * @throws IllegalAccessException if unable to create or retrieve the temporary file.
 */
@Throws(IllegalAccessException::class)
fun Context.getTmpFileUri(appId: String): Uri {
    val tmpFile = File.createTempFile(
        "tmp_image_file",
        ".png",
        cacheDir,
    ).apply {
        createNewFile()
        deleteOnExit()
    }

    return FileProvider.getUriForFile(
        applicationContext,
        "$appId.provider",
        tmpFile,
    )
}

/**
 * Retrieves a File object from the given content URI.
 *
 * @param contentUri The content URI of the file.
 * @return The File object representing the content URI.
 * @throws FileNotFoundException If the content URI cannot be opened or file cannot be created.
 * @throws IOException If there is an error during file operations.
 * @throws SecurityException If there are insufficient permissions to access the content.
 */
@Throws(FileNotFoundException::class, IOException::class, SecurityException::class)
fun Context.getFileFromContentUri(contentUri: Uri): File {
    val fileExtension = getFileExtension(this, contentUri)
    val fileName = "temp_file" + if (fileExtension != null) ".$fileExtension" else ""
    val tempFile = File(cacheDir, fileName)

    if (!tempFile.createNewFile()) {
        throw IOException("Failed to create temporary file: $fileName")
    }

    FileOutputStream(tempFile).use { outputStream ->
        contentResolver.openInputStream(contentUri)?.use { inputStream ->
            copy(inputStream, outputStream)
            outputStream.flush()
        } ?: throw FileNotFoundException("Failed to open input stream for URI: $contentUri")
    }

    return tempFile
}

/**
 * Retrieves the file extension from the given content URI.
 *
 * @param context The context.
 * @param uri The content URI of the file.
 * @return The file extension, or `null` if the extension could not be determined.
 */
private fun getFileExtension(context: Context, uri: Uri): String? {
    val fileType: String? = context.contentResolver.getType(uri)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
}

/**
 * Copies the data from the input stream to the output stream.
 *
 * @param source The input stream to read from.
 * @param target The output stream to write to.
 * @throws IOException if an I/O error occurs during the copy operation.
 */
@Throws(IOException::class)
private fun copy(source: InputStream, target: OutputStream) {
    val buf = ByteArray(8192)
    var length: Int
    while (source.read(buf).also { length = it } > 0) {
        target.write(buf, 0, length)
    }
}
