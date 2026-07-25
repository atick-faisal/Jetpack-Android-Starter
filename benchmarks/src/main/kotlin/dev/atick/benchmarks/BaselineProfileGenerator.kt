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

package dev.atick.benchmarks

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

/**
 * Generates the baseline profile shipped with the release build.
 *
 * The profile lists the classes and methods executed here so ART can compile them ahead of time
 * at install, which is worth roughly a 20-30% improvement in cold start.
 *
 * ## Why this only exercises startup
 *
 * This is a template. Most projects delete `:feature:home` and its item screens in their first
 * hour, and a profile generator that drives those screens breaks the moment they do — or worse,
 * keeps compiling and silently profiles nothing useful.
 *
 * So this deliberately covers only what survives: process start, the theme, Compose setup,
 * navigation, and the first frame. Those are the expensive parts of a cold start anyway. Once
 * you have real screens, add journeys through them here.
 *
 * Regenerate with:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 */
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = PACKAGE_NAME,
        // A fresh install is signed out, so this covers app startup through to the first
        // interactive frame of the signed-out flow.
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()

        // The splash screen holds the first frame until the auth state is known, so waiting for
        // idle is what makes sure the profile covers the real first screen rather than the
        // splash.
        device.waitForIdle()
    }

    private companion object {
        const val PACKAGE_NAME = "dev.atick.compose"
    }
}
