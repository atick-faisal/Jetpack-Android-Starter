/*
 * Copyright 2024 Atick Faisal
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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters


class DokkaConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply("org.jetbrains.dokka")
            }

            // TODO: AGP 9 Migration - Dokka Configuration Workaround
            afterEvaluate {
                extensions.configure<DokkaExtension> {
                    moduleName.set(path)
                    dokkaSourceSets.named("main") {
                        includes.from("README.md")
                        suppressGeneratedFiles.set(true)
                    }
                    pluginsConfiguration.withType<DokkaHtmlPluginParameters> {
                        footerMessage.set("Made with ❤\uFE0F by Atick Faisal")
                    }
                }
            }

            dependencies {
                "dokkaPlugin"(libs.findLibrary("dokka.android.plugin").get())
                "dokkaPlugin"(libs.findLibrary("dokka.mermaid.plugin").get())
            }
        }
    }
}