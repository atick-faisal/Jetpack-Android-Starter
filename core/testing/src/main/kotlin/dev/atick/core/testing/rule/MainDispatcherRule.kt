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

package dev.atick.core.testing.rule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps [Dispatchers.Main] for a [TestDispatcher] for the duration of a test.
 *
 * Anything that launches into `viewModelScope` — including the `updateStateWith` and `updateWith`
 * helpers in `:core:ui` — dispatches to [Dispatchers.Main], which has no implementation under a
 * plain JVM unit test. Without this rule those tests fail with
 * `Module with the Main dispatcher had failed to initialize`.
 *
 * Defaults to [UnconfinedTestDispatcher] so coroutines run eagerly and assertions can follow the
 * call directly. Pass a `StandardTestDispatcher` when a test needs to control ordering itself.
 *
 * ## Usage
 * ```kotlin
 * class MyViewModelTest {
 *     @get:Rule
 *     val mainDispatcherRule = MainDispatcherRule()
 *
 *     @Test
 *     fun `does the thing`() = runTest { /* ... */ }
 * }
 * ```
 *
 * @param testDispatcher The dispatcher to install as Main.
 */
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}
