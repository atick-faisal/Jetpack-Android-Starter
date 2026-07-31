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

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.getContainingUFile

/**
 * Flags raw Material 3 composables where the design system in `:core:ui` provides a wrapper.
 *
 * The wrappers exist so theming, sizing and behaviour stay consistent, and so a change in one
 * place reaches every screen. Nothing else enforces that — reviewers have to notice — which is
 * exactly the kind of rule that erodes as a project grows, and erodes faster in a template
 * whose users never read its review history.
 */
class DesignSystemDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(
        UCallExpression::class.java,
    )

    override fun createUastHandler(context: JavaContext): UElementHandler =
        object : UElementHandler() {
            override fun visitCallExpression(node: UCallExpression) {
                val name = node.methodName ?: return
                val preferredName = METHOD_NAMES[name] ?: return

                // Match on where the call resolves to, not just its name. Plenty of libraries
                // declare a Tab, a Button or an Icon, and this issue is an ERROR — flagging
                // one of those would fail a build over code the design system has no claim on.
                if (node.resolvePackageName() != MATERIAL3_PACKAGE) return

                // Do not flag the wrappers' own definitions, which necessarily call the
                // Material composables they wrap.
                if (node.getContainingUFile()?.packageName == DESIGN_SYSTEM_PACKAGE) return

                context.report(
                    ISSUE,
                    node,
                    context.getLocation(node),
                    "Using $name instead of $preferredName",
                )
            }
        }

    companion object {
        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "DesignSystem",
            briefDescription = "Design system",
            explanation = "This check flags calls to Compose Material composables where the " +
                "design system in :core:ui provides an equivalent. Using the wrapper keeps " +
                "theming and behaviour consistent, and means a change in one place reaches " +
                "every screen.",
            category = Category.CUSTOM_LINT_CHECKS,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(
                DesignSystemDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        /**
         * Material composable to the `:core:ui` wrapper that should be used instead.
         *
         * These are string literals rather than function references because `:lint` is a plain
         * JVM module and cannot depend on an Android one. Adding a wrapper to `:core:ui` means
         * adding it here too.
         */
        val METHOD_NAMES: Map<String, String> = mapOf(
            "Button" to "JetpackButton",
            "OutlinedButton" to "JetpackOutlinedButton",
            "TextButton" to "JetpackTextButton",
            "FilterChip" to "JetpackFilterChip",
            "ElevatedFilterChip" to "JetpackFilterChip",
            "NavigationBar" to "JetpackNavigationBar",
            "NavigationBarItem" to "JetpackNavigationBarItem",
            "NavigationRail" to "JetpackNavigationRail",
            "NavigationRailItem" to "JetpackNavigationRailItem",
            "TabRow" to "JetpackTabRow",
            "Tab" to "JetpackTab",
            "IconToggleButton" to "JetpackIconToggleButton",
            "FilledIconToggleButton" to "JetpackIconToggleButton",
            "FilledTonalIconToggleButton" to "JetpackIconToggleButton",
            "OutlinedIconToggleButton" to "JetpackIconToggleButton",
            "TopAppBar" to "JetpackTopAppBar",
            "CenterAlignedTopAppBar" to "JetpackTopAppBar",
            "SmallTopAppBar" to "JetpackTopAppBar",
            "MediumTopAppBar" to "JetpackTopAppBar",
            "LargeTopAppBar" to "JetpackTopAppBar",
            "TextField" to "JetpackTextField",
            "OutlinedTextField" to "JetpackTextField",
            "ExtendedFloatingActionButton" to "JetpackExtendedFab",
        )

        private const val MATERIAL3_PACKAGE = "androidx.compose.material3"

        /** The package holding the wrappers, and so allowed to call Material directly. */
        private const val DESIGN_SYSTEM_PACKAGE = "dev.atick.core.ui.components"
    }
}

/**
 * The package the call resolves to, or null if it cannot be resolved.
 *
 * A top-level Kotlin function compiles to a static method on a file facade class
 * (`androidx.compose.material3.ButtonKt`), so the package is that class' qualified name minus
 * its last segment.
 */
private fun UCallExpression.resolvePackageName(): String? {
    val declaringClass = resolve()?.containingClass?.qualifiedName ?: return null
    return declaringClass.substringBeforeLast('.', missingDelimiterValue = "")
        .takeIf(String::isNotEmpty)
}
