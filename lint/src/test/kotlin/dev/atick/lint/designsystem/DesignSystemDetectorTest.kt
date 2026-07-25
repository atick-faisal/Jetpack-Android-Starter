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

package dev.atick.lint.designsystem

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import dev.atick.lint.designsystem.DesignSystemDetector.Companion.ISSUE
import dev.atick.lint.designsystem.DesignSystemDetector.Companion.METHOD_NAMES
import org.junit.Test

class DesignSystemDetectorTest {

    @Test
    fun `flags a raw Material composable`() {
        lint()
            .issues(ISSUE)
            .allowMissingSdk()
            .files(
                COMPOSABLE_STUB,
                MATERIAL_STUBS,
                @Suppress("LintImplTrimIndent")
                kotlin(
                    """
                    |import androidx.compose.runtime.Composable
                    |
                    |@Composable
                    |fun Screen() {
                    |    Button()
                    |}
                    """.trimMargin(),
                ).indented(),
            )
            .run()
            .expectContains("Using Button instead of JetpackButton")
    }

    @Test
    fun `flags every mapped Material composable`() {
        lint()
            .issues(ISSUE)
            .allowMissingSdk()
            .files(
                COMPOSABLE_STUB,
                MATERIAL_STUBS,
                @Suppress("LintImplTrimIndent")
                kotlin(
                    """
                    |import androidx.compose.runtime.Composable
                    |
                    |@Composable
                    |fun Screen() {
                    ${METHOD_NAMES.keys.joinToString("\n") { "|    $it()" }}
                    |}
                    """.trimMargin(),
                ).indented(),
            )
            .run()
            // Every entry in the map must be reachable, so the map cannot silently gain a
            // key that the detector never fires on.
            .expectErrorCount(METHOD_NAMES.size)
    }

    @Test
    fun `does not flag the design system wrappers`() {
        lint()
            .issues(ISSUE)
            .allowMissingSdk()
            .files(
                COMPOSABLE_STUB,
                WRAPPER_STUBS,
                @Suppress("LintImplTrimIndent")
                kotlin(
                    """
                    |import androidx.compose.runtime.Composable
                    |
                    |@Composable
                    |fun Screen() {
                    ${METHOD_NAMES.values.distinct().joinToString("\n") { "|    $it()" }}
                    |}
                    """.trimMargin(),
                ).indented(),
            )
            .run()
            .expectClean()
    }

    @Test
    fun `does not flag a wrapper's own definition`() {
        // Button.kt in :core:ui necessarily calls Button. Flagging it would make the rule
        // impossible to satisfy.
        lint()
            .issues(ISSUE)
            .allowMissingSdk()
            .files(
                COMPOSABLE_STUB,
                MATERIAL_STUBS,
                @Suppress("LintImplTrimIndent")
                kotlin(
                    "src/dev/atick/core/ui/components/Button.kt",
                    """
                    |package dev.atick.core.ui.components
                    |
                    |import androidx.compose.runtime.Composable
                    |
                    |@Composable
                    |fun JetpackButton() {
                    |    Button()
                    |}
                    """.trimMargin(),
                ).indented(),
            )
            .run()
            .expectClean()
    }

    companion object {
        private val COMPOSABLE_STUB: TestFile = kotlin(
            """
            package androidx.compose.runtime
            annotation class Composable
            """.trimIndent(),
        ).indented()

        /** Declarations for the Material composables, so the calls under test resolve. */
        private val MATERIAL_STUBS: TestFile = kotlin(
            """
            |import androidx.compose.runtime.Composable
            |
            ${METHOD_NAMES.keys.joinToString("\n") { "|@Composable fun $it() = {}" }}
            |
            """.trimMargin(),
        ).indented()

        /** Declarations for the design system wrappers. */
        private val WRAPPER_STUBS: TestFile = kotlin(
            """
            |import androidx.compose.runtime.Composable
            |
            ${METHOD_NAMES.values.distinct().joinToString("\n") { "|@Composable fun $it() = {}" }}
            |
            """.trimMargin(),
        ).indented()
    }
}
