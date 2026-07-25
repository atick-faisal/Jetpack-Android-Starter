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

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import org.junit.Rule
import org.junit.Test

/**
 * Measures cold start with and without the baseline profile, so the profile's effect is a number
 * rather than an assumption.
 *
 * Run both and compare `timeToInitialDisplayMs`:
 * ```
 * ./gradlew :benchmarks:pixel6Api33BenchmarkAndroidTest
 * ```
 *
 * Like [BaselineProfileGenerator] this covers startup only, so it keeps working after the demo
 * feature is deleted.
 */
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /** Baseline: no ahead-of-time compilation at all. */
    @Test
    fun startupNoCompilation() = startup(CompilationMode.None())

    /** What a user gets from the Play Store, with the profile applied at install. */
    @Test
    fun startupWithBaselineProfile() = startup(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require),
    )

    private fun startup(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        // Cold start is the case the profile exists to improve.
        startupMode = StartupMode.COLD,
        iterations = ITERATIONS,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
    }

    private companion object {
        const val PACKAGE_NAME = "dev.atick.compose"

        // Enough to average out scheduling noise without making a CI run unreasonably slow.
        const val ITERATIONS = 10
    }
}
