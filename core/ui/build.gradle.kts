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
    alias(libs.plugins.jetpack.ui.library)
    alias(libs.plugins.jetpack.dokka)
}

android {
    namespace = "dev.atick.core.ui"
}

dependencies {
    api(projects.core.android)

    // ... AppCompat
    api(libs.androidx.appcompat)

    // ... Fragment
    api(libs.androidx.fragment.ktx)

    // ... Activity
    api(libs.androidx.activity.ktx)
    api(libs.androidx.activity.compose)

    // ... Lifecycle
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.livedata.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.runtimeCompose)
    api(libs.androidx.lifecycle.viewModelCompose)

    // ... Jetpack Compose
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.ui.util)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.navigationSuite)
    api(libs.androidx.compose.material3.adaptive)
    api(libs.androidx.compose.material3.adaptive.layout)
    api(libs.androidx.compose.material3.adaptive.navigation)
    api(libs.androidx.compose.material3.windowSizeClass)
    api(libs.androidx.compose.material.iconsExtended)
    api(libs.androidx.compose.ui.tooling.preview)
    debugApi(libs.androidx.compose.ui.tooling)

    // ... Navigation
    api(libs.androidx.navigation.fragment)
    api(libs.androidx.navigation.compose)
    api(libs.androidx.hilt.navigation.compose)


    // ... Coil
    api(libs.coil.kt)
    api(libs.coil.kt.compose)

    // ... Lottie
    api(libs.lottie.compose)

    testImplementation(projects.core.testing)
}