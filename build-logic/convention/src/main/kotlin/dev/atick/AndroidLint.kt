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

package dev.atick

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

/**
 * Configures lint reporting for every Android module.
 *
 * SARIF output is what lets CI upload findings to GitHub code scanning, so a lint failure shows
 * up as an annotation on the pull request rather than only in a build log.
 *
 * `checkDependencies` makes an app-level lint run analyse the library modules it depends on, so
 * a single `lintRelease` covers the whole project.
 */
internal fun Project.configureAndroidLint(commonExtension: CommonExtension) {
    // AGP 9 exposes lint as a property rather than a script block on the base extension.
    commonExtension.lint.apply {
        xmlReport = true
        sarifReport = true
        checkDependencies = true

        // Renovate already surfaces dependency updates; lint reporting them again adds noise
        // to every build.
        disable += "GradleDependency"
    }
}
