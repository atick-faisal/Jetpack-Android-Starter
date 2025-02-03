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

package dev.atick.compose.data.home

import androidx.compose.runtime.Immutable
import dev.atick.core.extensions.asFormattedDateTime
import dev.atick.core.network.models.NetworkPost
import dev.atick.core.room.models.JetpackEntity
import dev.atick.core.room.models.PostEntity
import java.util.UUID

/**
 * Data class representing the data to be displayed on the home screen.
 *
 * @property posts The list of posts to be displayed on the home screen.
 */
@Immutable
data class HomeScreenData(
    val jetpacks: List<Jetpack> = listOf(),
)

/**
 * Data class representing a post to be shown on the ui.
 *
 * @property id The unique identifier of the network post.
 * @property title The title of the network post.
 * @property url The URL associated with the network post.
 * @property thumbnailUrl The URL of the thumbnail image associated with the network post.
 */
data class UiPost(
    val id: Int = 0,
    val title: String = "",
    val url: String = "",
    val thumbnailUrl: String = "",
)

data class Jetpack(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val price: Double = 0.0,
    val lastUpdated: Long = 0,
    val needsSync: Boolean = false,
    val formattedDate: String = lastUpdated.asFormattedDateTime(),
)

fun JetpackEntity.toJetpack(): Jetpack {
    return Jetpack(
        id = id,
        name = name,
        price = price,
        lastUpdated = lastUpdated,
        needsSync = needsSync,
        formattedDate = lastUpdated.asFormattedDateTime(),
    )
}

fun List<JetpackEntity>.mapToJetpacks(): List<Jetpack> {
    return map(JetpackEntity::toJetpack)
}

fun Jetpack.toJetpackEntity(): JetpackEntity {
    return JetpackEntity(
        id = id,
        name = name,
        price = price,
        lastUpdated = lastUpdated,
        needsSync = needsSync,
    )
}

/**
 * Converts a [PostEntity] object to a [UiPost] object for displaying in the UI.
 *
 * @return The converted [UiPost] object.
 */
fun PostEntity.toUiPost(): UiPost {
    return UiPost(
        id = id,
        title = title,
        url = url,
        thumbnailUrl = thumbnailUrl,
    )
}

/**
 * Converts a list of [PostEntity] objects to a list of [UiPost] objects for displaying in the UI,
 * using the [toUiPost] conversion function.
 *
 * @receiver The list of [PostEntity] objects to be converted.
 * @return A list of converted [UiPost] objects.
 */
fun List<PostEntity>.mapToUiPost(): List<UiPost> {
    return map(PostEntity::toUiPost)
}

/**
 * Converts a [NetworkPost] object to a corresponding [UiPost] object.
 *
 * This extension function facilitates the conversion of a [NetworkPost] object into a [UiPost] object by mapping
 * the properties from the source to the destination.
 *
 * @receiver The [NetworkPost] object to convert to [UiPost].
 * @return A new [UiPost] object with properties mapped from the source [NetworkPost].
 */
fun NetworkPost.toUiPost(): UiPost {
    return UiPost(
        id = id,
        title = title,
        url = url,
        thumbnailUrl = thumbnailUrl,
    )
}

/**
 * Converts a [NetworkPost] object to a [PostEntity] object.
 *
 * @return The converted [PostEntity] object.
 */
fun NetworkPost.toPostEntity(): PostEntity {
    return PostEntity(
        id = id,
        title = title,
        url = url,
        thumbnailUrl = thumbnailUrl,
    )
}

/**
 * Converts a list of [NetworkPost] objects to a list of corresponding [UiPost] objects.
 *
 * This extension function applies the [NetworkPost.toUiPost] conversion function to each element of the source list,
 * effectively mapping the properties from [NetworkPost] objects to [UiPost] objects.
 *
 * @receiver The list of [NetworkPost] objects to convert to a list of [UiPost] objects.
 * @return A new list containing [UiPost] objects with properties mapped from the source [NetworkPost] objects.
 */
fun List<NetworkPost>.mapToUiPosts(): List<UiPost> {
    return map(NetworkPost::toUiPost)
}

/**
 * Converts a list of [NetworkPost] objects to a list of [PostEntity] objects using the
 * [toPostEntity] conversion function.
 *
 * @receiver The list of [NetworkPost] objects to be converted.
 * @return A list of converted [PostEntity] objects.
 */
fun List<NetworkPost>.mapToPostEntities(): List<PostEntity> {
    return map(NetworkPost::toPostEntity)
}
