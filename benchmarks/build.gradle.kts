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

plugins {
    alias(libs.plugins.jetpack.test)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "dev.atick.benchmarks"

    targetProjectPath = ":app"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // The benchmark instruments itself rather than being packaged into the app under test,
    // which keeps the measured app free of test-only code.
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    // Run on a managed device rather than whatever happens to be connected, so the generated
    // profile is reproducible.
    managedDevices += "pixel6Api33"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.uiautomator)
    implementation(libs.junit)
}
