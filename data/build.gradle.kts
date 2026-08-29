/*
 * Copyright 2025 Atick Faisal
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
    namespace = "dev.atick.data"
}

dependencies {
    // ... Core
    implementation(projects.core.android)
    implementation(projects.core.network)
    implementation(projects.core.preferences)
    implementation(projects.core.room)

    // ... Firebase
    implementation(projects.firebase.auth)
    implementation(projects.firebase.firestore)

    testImplementation(projects.core.testing)

    // The Firestore SDK is an implementation detail of :firebase:firestore, so `Timestamp` is not
    // on this module's compile classpath -- deliberately. Tests still need it to build the remote
    // fixtures a fake FirebaseDataSource returns.
    testImplementation(platform(libs.firebase.bom))
    testImplementation(libs.firebase.firestore)
}
