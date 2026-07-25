/*
 * Copyright 2026 Atick Faisal
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

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * [UriDecoder] is the seam that lets navigation arguments be decoded without a screen or
 * ViewModel touching `android.net.Uri` directly, which is what makes those classes testable.
 *
 * Robolectric supplies the real `Uri` implementation; the plain JVM stub throws.
 */
@RunWith(RobolectricTestRunner::class)
class UriDecoderTest {

    private val decoder = UriDecoder()

    @Test
    fun `decodes percent-encoded characters`() {
        assertThat(decoder.decodeString("hello%20world")).isEqualTo("hello world")
        assertThat(decoder.decodeString("a%2Fb")).isEqualTo("a/b")
    }

    @Test
    fun `leaves unencoded strings untouched`() {
        assertThat(decoder.decodeString("plain")).isEqualTo("plain")
    }

    @Test
    fun `round-trips an encoded value`() {
        val original = "id with spaces/and slashes?and=query"

        assertThat(decoder.decodeString(Uri.encode(original))).isEqualTo(original)
    }

    @Test
    fun `decodes an empty string to an empty string`() {
        assertThat(decoder.decodeString("")).isEmpty()
    }
}
