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

package dev.atick.core.ui.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.atick.core.ui.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * [UiText] lets a ViewModel name a message without holding a Context, which is what keeps
 * ViewModels unit-testable. Robolectric supplies the real Context for resource resolution.
 */
@RunWith(RobolectricTestRunner::class)
class UiTextTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `DynamicString returns its literal value`() {
        val text = UiText.DynamicString("hello")

        assertThat(text.asString(context)).isEqualTo("hello")
    }

    @Test
    fun `DynamicString handles an empty value`() {
        assertThat(UiText.DynamicString("").asString(context)).isEmpty()
    }

    @Test
    fun `StringResource resolves against the context`() {
        val text = UiText.StringResource(R.string.core_ui_report)

        assertThat(text.asString(context)).isEqualTo("Report")
    }

    @Test
    fun `StringResource resolves an intentionally empty resource`() {
        val text = UiText.StringResource(R.string.core_ui_empty)

        assertThat(text.asString(context)).isEmpty()
    }

    @Test
    fun `DynamicString is a value type but StringResource is not`() {
        // DynamicString is a data class, so structurally equal instances compare equal.
        assertThat(UiText.DynamicString("same")).isEqualTo(UiText.DynamicString("same"))

        // StringResource is a plain class holding a vararg array, so it uses identity equality.
        // Anything that puts a UiText in Compose state and relies on equality to skip
        // recomposition will always see StringResource as changed.
        val first = UiText.StringResource(R.string.core_ui_report)
        val second = UiText.StringResource(R.string.core_ui_report)
        assertThat(first).isNotEqualTo(second)
        assertThat(first).isEqualTo(first)
    }
}
