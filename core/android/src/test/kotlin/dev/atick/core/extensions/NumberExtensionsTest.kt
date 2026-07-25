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

package dev.atick.core.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

/**
 * [format] and [asFormattedDateTime] both read ambient state — the default [Locale] and
 * [TimeZone] — so the tests pin both. Without that they pass on the author's machine and fail
 * in CI, which is the classic way a formatting test earns a reputation for being flaky.
 */
class NumberExtensionsTest {

    private lateinit var defaultLocale: Locale
    private lateinit var defaultTimeZone: TimeZone

    @Before
    fun pinAmbientState() {
        defaultLocale = Locale.getDefault()
        defaultTimeZone = TimeZone.getDefault()
        Locale.setDefault(Locale.US)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun restoreAmbientState() {
        Locale.setDefault(defaultLocale)
        TimeZone.setDefault(defaultTimeZone)
    }

    @Test
    fun `format groups thousands and trims trailing zeros`() {
        assertThat(1234.5.format()).isEqualTo("1,234.5")
        assertThat(1000000.format()).isEqualTo("1,000,000")
        assertThat(0.format()).isEqualTo("0")
    }

    @Test
    fun `format honours the requested number of decimals`() {
        assertThat(3.14159.format(nDecimal = 2)).isEqualTo("3.14")
        assertThat(3.14159.format(nDecimal = 0)).isEqualTo("3")
    }

    @Test
    fun `format as currency always keeps two decimals and appends the symbol`() {
        assertThat(1234.5.format(isCurrency = true)).isEqualTo("1,234.50$")
        assertThat(10.format(isCurrency = true)).isEqualTo("10.00$")
    }

    @Test
    fun `format handles NaN and the infinities`() {
        assertThat(Double.NaN.format()).isEqualTo("NaN")
        assertThat(Double.POSITIVE_INFINITY.format()).isEqualTo("∞")
        assertThat(Double.NEGATIVE_INFINITY.format()).isEqualTo("-∞")
    }

    @Test
    fun `format handles negative values`() {
        assertThat((-1234.5).format()).isEqualTo("-1,234.5")
    }

    @Test
    fun `asFormattedDateTime renders the epoch in twelve hour form`() {
        assertThat(0L.asFormattedDateTime()).isEqualTo("JANUARY 1, 1970 at 12:00 AM")
    }

    @Test
    fun `asFormattedDateTime pads minutes and switches to PM after noon`() {
        // 1970-01-01T13:05:00Z
        val afternoon = (13 * 60 + 5) * 60 * 1000L

        assertThat(afternoon.asFormattedDateTime()).isEqualTo("JANUARY 1, 1970 at 1:05 PM")
    }

    @Test
    fun `asFormattedDateTime renders noon as twelve PM not zero`() {
        val noon = 12 * 60 * 60 * 1000L

        assertThat(noon.asFormattedDateTime()).isEqualTo("JANUARY 1, 1970 at 12:00 PM")
    }
}
