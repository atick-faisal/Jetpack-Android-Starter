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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * These validators back the sign-in and sign-up forms, so a change here silently changes what
 * the app accepts as a credential.
 *
 * Runs under Robolectric because [isEmailValid] delegates to `android.util.Patterns`, which is
 * stubbed out under a plain JVM unit test and would return false for everything.
 */
@RunWith(RobolectricTestRunner::class)
class StringExtensionsTest {

    @Test
    fun `isEmailValid accepts ordinary addresses`() {
        assertThat("user@example.com".isEmailValid()).isTrue()
        assertThat("first.last+tag@sub.example.co.uk".isEmailValid()).isTrue()
    }

    @Test
    fun `isEmailValid rejects malformed addresses`() {
        assertThat("not-an-email".isEmailValid()).isFalse()
        assertThat("missing@domain".isEmailValid()).isFalse()
        assertThat("@example.com".isEmailValid()).isFalse()
        assertThat("spaces in@example.com".isEmailValid()).isFalse()
    }

    @Test
    fun `isEmailValid rejects null and empty`() {
        assertThat(null.isEmailValid()).isFalse()
        assertThat("".isEmailValid()).isFalse()
    }

    @Test
    fun `isPasswordValid requires a digit, a lowercase letter and 8 to 20 characters`() {
        assertThat("password1".isPasswordValid()).isTrue()
        assertThat("abcdefg1".isPasswordValid()).isTrue()
    }

    @Test
    fun `isPasswordValid rejects passwords that miss a rule`() {
        // No digit.
        assertThat("passwordd".isPasswordValid()).isFalse()
        // No lowercase letter.
        assertThat("PASSWORD1".isPasswordValid()).isFalse()
        // Seven characters, one short.
        assertThat("passwo1".isPasswordValid()).isFalse()
        // Twenty-one characters, one over.
        assertThat("passwordpasswordpass1".isPasswordValid()).isFalse()
    }

    @Test
    fun `isPasswordValid rejects null and empty`() {
        assertThat(null.isPasswordValid()).isFalse()
        assertThat("".isPasswordValid()).isFalse()
    }

    @Test
    fun `isValidFullName requires at least two all-letter parts`() {
        assertThat("Ada Lovelace".isValidFullName()).isTrue()
        assertThat("Ada Byron Lovelace".isValidFullName()).isTrue()
    }

    @Test
    fun `isValidFullName rejects single names, digits and null`() {
        assertThat("Ada".isValidFullName()).isFalse()
        assertThat("Ada L0velace".isValidFullName()).isFalse()
        assertThat("Ada Lovelace-Byron".isValidFullName()).isFalse()
        assertThat(null.isValidFullName()).isFalse()
    }
}
