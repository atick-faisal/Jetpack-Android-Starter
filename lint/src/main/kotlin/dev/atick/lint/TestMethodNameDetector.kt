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

package dev.atick.lint

import com.android.tools.lint.detector.api.AnnotationInfo
import com.android.tools.lint.detector.api.AnnotationUsageInfo
import com.android.tools.lint.detector.api.Category.Companion.TESTING
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope.JAVA_FILE
import com.android.tools.lint.detector.api.Scope.TEST_SOURCES
import com.android.tools.lint.detector.api.Severity.WARNING
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.TextFormat.RAW
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UElement
import java.util.EnumSet

/**
 * Flags test methods whose name starts with `test`.
 *
 * The `@Test` annotation already says what the method is, so the prefix costs four characters
 * of the part of the name that should describe the behaviour under test.
 *
 * Note this deliberately does not port Now in Android's companion `given_when_then` format
 * check. That rule assumes underscore-separated names, and this project uses Kotlin's backtick
 * syntax for full sentences instead — enforcing NIA's convention here would flag every test in
 * the repo.
 */
class TestMethodNameDetector : Detector(), SourceCodeScanner {

    override fun applicableAnnotations(): List<String> = listOf("org.junit.Test")

    override fun visitAnnotationUsage(
        context: JavaContext,
        element: UElement,
        annotationInfo: AnnotationInfo,
        usageInfo: AnnotationUsageInfo,
    ) {
        val method = usageInfo.referenced as? PsiMethod ?: return
        if (!method.name.startsWith("test")) return

        context.report(
            issue = PREFIX,
            scope = usageInfo.usage,
            location = context.getNameLocation(method),
            message = PREFIX.getBriefDescription(RAW),
            quickfixData = LintFix.create()
                .name("Remove prefix")
                .replace().pattern("""test[\s_]*""")
                .with("")
                .autoFix()
                .build(),
        )
    }

    companion object {
        @JvmField
        val PREFIX: Issue = Issue.create(
            id = "TestMethodPrefix",
            briefDescription = "Test method starts with `test`",
            explanation = "Test method should not start with `test`. The @Test annotation " +
                "already identifies the method; the name should describe the behaviour.",
            category = TESTING,
            priority = 5,
            severity = WARNING,
            implementation = Implementation(
                TestMethodNameDetector::class.java,
                EnumSet.of(JAVA_FILE, TEST_SOURCES),
            ),
        )
    }
}
