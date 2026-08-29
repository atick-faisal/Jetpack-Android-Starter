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

package dev.atick.core.preferences.utils

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import dev.atick.core.preferences.model.UserDataPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer implementation for serializing and deserializing [UserDataPreferences] objects.
 */
object UserDataSerializer : Serializer<UserDataPreferences> {

    /**
     * The default value of [UserDataPreferences] to be used when deserialization fails.
     */
    override val defaultValue: UserDataPreferences = UserDataPreferences()

    /**
     * Reads a [UserDataPreferences] object from the provided [InputStream].
     *
     * @param input The input stream to read data from.
     * @return The deserialized [UserDataPreferences] object.
     * @throws CorruptionException if there's an issue with deserialization.
     */
    override suspend fun readFrom(input: InputStream): UserDataPreferences {
        try {
            return Json.decodeFromString(
                UserDataPreferences.serializer(),
                input.readBytes().decodeToString(),
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read UserPrefs", serialization)
        }
    }

    /**
     * Writes a [UserDataPreferences] object to the provided [OutputStream].
     *
     * @param t The [UserDataPreferences] object to be serialized.
     * @param output The output stream to write data to.
     */
    override suspend fun writeTo(t: UserDataPreferences, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(UserDataPreferences.serializer(), t)
                    .encodeToByteArray(),
            )
        }
    }
}
