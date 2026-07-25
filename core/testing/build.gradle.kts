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
    alias(libs.plugins.jetpack.library)
    alias(libs.plugins.jetpack.dokka)
}

android {
    namespace = "dev.atick.core.testing"
}

dependencies {
    implementation(projects.core.android)
    // api: consumers of the fakes need the interfaces and models they implement.
    api(projects.core.preferences)
    api(projects.firebase.auth)

    // Test libraries are api() here: this module exists to be consumed from testImplementation,
    // so anything a test needs to use the rules and fakes below has to come with it.
    api(libs.junit)
    api(libs.kotlin.test)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(libs.truth)
}
