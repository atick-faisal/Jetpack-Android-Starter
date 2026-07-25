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
 * Derives a per-module resource prefix from the module path, so `:feature:home` requires its
 * resources to be named `feature_home_*`.
 *
 * Library resources share a single namespace once merged into the app, so without a prefix two
 * modules can silently define the same name and the winner depends on merge order. Lint reports
 * a warning for any resource that does not match.
 */
internal fun Project.configureResourcePrefix(commonExtension: CommonExtension) {
    commonExtension.resourcePrefix = path
        .split("""\W""".toRegex())
        .drop(1)
        .distinct()
        .joinToString(separator = "_")
        .lowercase() + "_"
}
