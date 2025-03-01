/*
 * Copyright 2023 Atick Faisal
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
    alias(libs.plugins.jetpack.dagger.hilt)
    alias(libs.plugins.jetpack.dokka)
}

android {
    namespace = "dev.atick.core.android"
}

dependencies {
    // ... Core Android
    api(libs.androidx.core.ktx)

    // ... Kotlin Reflect
    api(libs.kotlin.reflect)

    // ... Coroutines
    api(libs.kotlinx.coroutines.android)

    // ... Serialization
    api(libs.kotlinx.serialization.json)

    // ... Date-Time
    api(libs.kotlinx.datetime)

    // ... Dagger-Hilt
    api(libs.dagger.hilt.android)

    // ... Logger
    api(libs.timber.logging)
}