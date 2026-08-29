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
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Configures Compose for any module that renders UI: the Compose build feature, the opt-ins the
 * design system relies on, and the compiler metrics, reports and stability configuration.
 *
 * The Compose BOM is applied here so every Compose module resolves the same artifact versions
 * without depending on `:core:ui` re-exporting them.
 */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    commonExtension.buildFeatures.compose = true

    val bom = libs.findLibrary("androidx-compose-bom").get()
    dependencies.add("implementation", dependencies.platform(bom))
    dependencies.add("testImplementation", dependencies.platform(bom))
    dependencies.add("androidTestImplementation", dependencies.platform(bom))

    // Compose tests run on Robolectric alongside the plain unit tests, so every UI module can
    // write one without repeating the setup. ui-test-manifest supplies the activity that
    // createComposeRule launches into.
    dependencies.add("testImplementation", libs.findLibrary("androidx-compose-ui-test").get())
    dependencies.add(
        "debugImplementation",
        libs.findLibrary("androidx-compose-ui-test-manifest").get(),
    )

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
                // Material 3 Expressive
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
                // Adaptive layouts, including the Navigation 3 list-detail scene strategy
                "-opt-in=androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
            )
        }
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        // 💡 flatMap + a nested provider{} is what makes this return an absent Provider when the
        // Gradle property is missing or false, rather than a Provider wrapping null -- Gradle
        // providers can't hold null, so "not set" can only be represented by never populating
        // the provider at all.
        fun Provider<String>.onlyIfTrue() = flatMap { provider { it.takeIf(String::toBoolean) } }

        // isolated.rootProject keeps this compatible with Gradle's Isolated Projects mode,
        // which forbids reaching across to `rootProject` at configuration time.
        fun Provider<*>.relativeToRootProject(dir: String) = map {
            isolated.rootProject.projectDirectory
                .dir("build")
                .dir(projectDir.toRelativeString(rootDir))
                .dir(dir)
        }

        providers.gradleProperty("enableComposeCompilerMetrics")
            .onlyIfTrue()
            .relativeToRootProject("compose-metrics")
            .let(metricsDestination::set)

        providers.gradleProperty("enableComposeCompilerReports")
            .onlyIfTrue()
            .relativeToRootProject("compose-reports")
            .let(reportsDestination::set)

        stabilityConfigurationFiles.add(
            isolated.rootProject.projectDirectory.file("compose_compiler_config.conf"),
        )
    }
}
