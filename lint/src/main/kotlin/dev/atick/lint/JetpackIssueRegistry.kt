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

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import dev.atick.lint.designsystem.DesignSystemDetector

/**
 * Registers this project's custom lint checks.
 *
 * Published to consumers through `lintPublish(projects.lint)` in `:core:ui`, so every module
 * that uses the design system gets the checks without wiring them up itself.
 */
class JetpackIssueRegistry : IssueRegistry() {

    override val issues = listOf(
        DesignSystemDetector.ISSUE,
        TestMethodNameDetector.PREFIX,
    )

    override val api: Int = CURRENT_API

    override val minApi: Int = 12

    override val vendor: Vendor = Vendor(
        vendorName = "Jetpack Android Starter",
        feedbackUrl = "https://github.com/atick-faisal/Jetpack-Android-Starter/issues",
        contact = "https://github.com/atick-faisal/Jetpack-Android-Starter",
    )
}
