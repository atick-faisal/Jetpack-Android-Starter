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

package dev.atick.feature.settings.ui

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.atick.core.testing.rule.MainDispatcherRule
import dev.atick.data.model.settings.DarkThemeConfig
import dev.atick.data.model.settings.Settings
import dev.atick.data.repository.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Checks collected settings, returned write failures, load-error loss, and collector lifetime.
 * Runs under Robolectric because the ViewModel reads the app locale through AppCompatDelegate.
 * These state tests do not establish snackbar delivery.
 */
@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository()
    private val viewModel = SettingsViewModel(repository)

    @Test
    fun `starts with default settings before the repository emits`() = runTest {
        assertThat(viewModel.settingsUiState.value.data).isEqualTo(Settings())
    }

    @Test
    fun `emits repository settings once collected`() = runTest {
        repository.emit(Settings(userName = "Ada", useDynamicColor = false))

        viewModel.settingsUiState.test {
            // onStart wires the repository up on first collection, so the initial value may
            // arrive before the repository's.
            var state = awaitItem()
            while (state.data.userName == null) state = awaitItem()

            assertThat(state.data.userName).isEqualTo("Ada")
            assertThat(state.data.useDynamicColor).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateDarkThemeConfig writes through to the repository`() = runTest {
        viewModel.settingsUiState.test {
            awaitItem()

            viewModel.updateDarkThemeConfig(DarkThemeConfig.DARK)

            assertThat(repository.darkThemeConfig).isEqualTo(DarkThemeConfig.DARK)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateDynamicColorPreference writes through to the repository`() = runTest {
        viewModel.settingsUiState.test {
            awaitItem()

            viewModel.updateDynamicColorPreference(false)

            assertThat(repository.useDynamicColor).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signOut calls the repository`() = runTest {
        viewModel.settingsUiState.test {
            awaitItem()

            viewModel.signOut()

            assertThat(repository.signOutCount).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failing write surfaces the error in UiState rather than throwing`() = runTest {
        val boom = IllegalStateException("write failed")
        repository.failWith = boom

        viewModel.settingsUiState.test {
            awaitItem()

            viewModel.updateDarkThemeConfig(DarkThemeConfig.DARK)

            // A returned failure becomes an error event; this test does not render a snackbar.
            val errored = expectMostRecentItem()
            assertThat(errored.loading).isFalse()
            assertThat(errored.error.getContentIfNotHandled()).isSameInstanceAs(boom)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `settings resubscription immediately adds another repository collector`() = runTest {
        try {
            viewModel.settingsUiState.test {
                awaitItem()
                runCurrent()
                assertThat(repository.activeCollectors).isEqualTo(1)
                cancelAndIgnoreRemainingEvents()
            }
            // Settings uses WhileSubscribed() with a zero stop timeout.
            runCurrent()
            assertThat(repository.activeCollectors).isEqualTo(1)
            viewModel.settingsUiState.test {
                awaitItem()
                runCurrent()
                assertThat(repository.activeCollectors).isEqualTo(2)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            viewModel.viewModelScope.cancel()
        }
        assertThat(repository.activeCollectors).isEqualTo(0)
    }

    private class FakeSettingsRepository : SettingsRepository {
        private val settings = MutableStateFlow(Settings())

        var darkThemeConfig: DarkThemeConfig? = null
            private set
        var useDynamicColor: Boolean? = null
            private set
        var signOutCount: Int = 0
            private set

        /** Set to have every write fail, for exercising the error path. */
        var failWith: Throwable? = null
        var activeCollectors = 0
            private set

        fun emit(value: Settings) = settings.update { value }

        override fun getSettings(): Flow<Settings> = settings.asStateFlow()
            .onStart { activeCollectors++ }
            .onCompletion { activeCollectors-- }

        override suspend fun setDarkThemeConfig(
            darkThemeConfig: DarkThemeConfig,
        ): Result<Unit> = failWith?.let { Result.failure(it) } ?: run {
            this.darkThemeConfig = darkThemeConfig
            settings.update { it.copy(darkThemeConfig = darkThemeConfig) }
            Result.success(Unit)
        }

        override suspend fun setDynamicColorPreference(
            useDynamicColor: Boolean,
        ): Result<Unit> = failWith?.let { Result.failure(it) } ?: run {
            this.useDynamicColor = useDynamicColor
            settings.update { it.copy(useDynamicColor = useDynamicColor) }
            Result.success(Unit)
        }

        override suspend fun signOut(): Result<Unit> =
            failWith?.let { Result.failure(it) } ?: run {
                signOutCount++
                Result.success(Unit)
            }
    }
}
