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

import com.android.build.api.dsl.TestExtension
import dev.atick.configureGradleManagedDevices
import dev.atick.configureKotlinAndroid
import dev.atick.intVersion
import dev.atick.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention for a `com.android.test` module — currently just `:benchmarks`, which runs
 * macrobenchmarks against `:app` and generates its baseline profile.
 */
class TestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.test")
            }

            extensions.configure<TestExtension> {
                configureKotlinAndroid(this)
                configureGradleManagedDevices(this)

                defaultConfig.targetSdk = libs.intVersion("targetSdk")
            }
        }
    }
}
