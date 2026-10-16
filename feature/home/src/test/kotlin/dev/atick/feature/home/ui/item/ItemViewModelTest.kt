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

package dev.atick.feature.home.ui.item

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.atick.core.testing.rule.MainDispatcherRule
import dev.atick.data.model.home.Jetpack
import dev.atick.data.repository.home.HomeRepository
import dev.atick.data.utils.SyncProgress
import dev.atick.feature.home.ui.home.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * The price field is the only text input in the project bound to a non-String model, which is
 * what made it possible for a keystroke to be silently discarded. These tests pin the rule that
 * came out of that: the field holds what was typed, and parsing happens once, at save.
 *
 * Every assertion goes through Turbine rather than reading `itemUiState.value`. The state is a
 * `stateInDelayed`, so with no collector it just replays its initial value and an assertion
 * against it would pass no matter what the ViewModel did.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ItemViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeHomeRepository()

    @Test
    fun `price starts empty for a new item`() = runTest {
        val viewModel = ItemViewModel(repository, existingJetpackId = null)

        viewModel.itemUiState.test {
            assertThat(awaitItem().data.jetpackPrice).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearing the price leaves the field empty`() = runTest {
        val viewModel = ItemViewModel(repository, existingJetpackId = null)

        viewModel.itemUiState.test {
            skipItems(1)

            viewModel.updatePrice("42")
            assertThat(awaitItem().data.jetpackPrice).isEqualTo("42")

            viewModel.updatePrice("")
            assertThat(awaitItem().data.jetpackPrice).isEmpty()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a partially typed decimal is kept exactly as entered`() = runTest {
        val viewModel = ItemViewModel(repository, existingJetpackId = null)

        viewModel.itemUiState.test {
            skipItems(1)

            viewModel.updatePrice("1.")

            assertThat(awaitItem().data.jetpackPrice).isEqualTo("1.")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unparseable input is kept rather than dropped`() = runTest {
        val viewModel = ItemViewModel(repository, existingJetpackId = null)

        viewModel.itemUiState.test {
            skipItems(1)

            viewModel.updatePrice("12")
            assertThat(awaitItem().data.jetpackPrice).isEqualTo("12")

            viewModel.updatePrice("12e")
            assertThat(awaitItem().data.jetpackPrice).isEqualTo("12e")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saving parses the entered price`() = runTest {
        val viewModel = ItemViewModel(repository, existingJetpackId = null)

        viewModel.updateName("Compose")
        viewModel.updatePrice(" 99.99 ")
        viewModel.createOrUpdateJetpack()

        assertThat(repository.saved?.price).isEqualTo(99.99)
        assertThat(repository.saved?.name).isEqualTo("Compose")
    }

    @Test
    fun `saving an unparseable price falls back to zero`() = runTest {
        val viewModel = ItemViewModel(repository, existingJetpackId = null)

        viewModel.updatePrice("")
        viewModel.createOrUpdateJetpack()

        assertThat(repository.saved?.price).isEqualTo(0.0)
    }

    @Test
    fun `failed save currently requests navigation without exposing the failure`() = runTest {
        repository.saveResult = Result.failure(IllegalStateException("write failed"))
        val viewModel = ItemViewModel(repository, existingJetpackId = null)
        viewModel.itemUiState.test {
            awaitItem()
            viewModel.createOrUpdateJetpack()
            val state = expectMostRecentItem()
            // Characterizes a bug: a failed write must not be taught as a successful save.
            assertThat(state.data.navigateBack.getContentIfNotHandled()).isTrue()
            assertThat(state.error.peekContent()).isNull()
            assertThat(state.loading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `failed load currently leaves default form data without an error`() = runTest {
        repository.loadFailure = IllegalStateException("read failed")
        val viewModel = ItemViewModel(repository, existingJetpackId = "missing")
        viewModel.itemUiState.test {
            awaitItem()
            runCurrent()
            assertThat(repository.itemSubscriptions).isEqualTo(1)
            assertThat(viewModel.itemUiState.value.data.jetpackName).isEmpty()
            assertThat(viewModel.itemUiState.value.error.peekContent()).isNull()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `item resubscription after timeout leaves two repository collectors active`() = runTest {
        val viewModel = ItemViewModel(repository, existingJetpackId = "existing")
        try {
            viewModel.itemUiState.test {
                awaitItem()
                runCurrent()
                assertThat(repository.activeItemCollectors).isEqualTo(1)
                cancelAndIgnoreRemainingEvents()
            }
            advanceTimeBy(5_001)
            runCurrent()
            assertThat(repository.activeItemCollectors).isEqualTo(1)
            viewModel.itemUiState.test {
                awaitItem()
                runCurrent()
                assertThat(repository.itemSubscriptions).isEqualTo(2)
                assertThat(repository.activeItemCollectors).isEqualTo(2)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            viewModel.viewModelScope.cancel()
        }
        assertThat(repository.activeItemCollectors).isEqualTo(0)
    }

    @Test
    fun `home resubscription after timeout leaves two repository collectors active`() = runTest {
        val viewModel = HomeViewModel(repository)
        try {
            viewModel.homeUiState.test {
                awaitItem()
                runCurrent()
                assertThat(repository.activeHomeCollectors).isEqualTo(1)
                cancelAndIgnoreRemainingEvents()
            }
            advanceTimeBy(5_001)
            runCurrent()
            assertThat(repository.activeHomeCollectors).isEqualTo(1)
            viewModel.homeUiState.test {
                awaitItem()
                runCurrent()
                assertThat(repository.activeHomeCollectors).isEqualTo(2)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            viewModel.viewModelScope.cancel()
        }
        assertThat(repository.activeHomeCollectors).isEqualTo(0)
    }

    @Test
    fun `an existing item loads its price as text`() = runTest {
        val existing = Jetpack(id = "jetpack-1", name = "Room", price = 12.5)
        val viewModel = ItemViewModel(
            FakeHomeRepository(existing = existing),
            existingJetpackId = existing.id,
        )

        // getJetpack() is launched from onStart, so the item only loads once something collects.
        viewModel.itemUiState.test {
            var state = awaitItem()
            while (state.data.jetpackPrice.isEmpty()) state = awaitItem()

            assertThat(state.data.jetpackPrice).isEqualTo("12.5")
            cancelAndIgnoreRemainingEvents()
        }
    }
}

/**
 * Records the last saved item so a test can assert what the ViewModel parsed out of the form.
 */
private class FakeHomeRepository(
    private val existing: Jetpack? = null,
) : HomeRepository {

    var saved: Jetpack? = null
        private set
    var saveResult: Result<Unit> = Result.success(Unit)
    var loadFailure: Throwable? = null
    var itemSubscriptions = 0
        private set
    var activeItemCollectors = 0
        private set
    var activeHomeCollectors = 0
        private set

    override fun getJetpacks(): Flow<List<Jetpack>> = flow {
        activeHomeCollectors++
        try {
            emit(listOfNotNull(existing))
            awaitCancellation()
        } finally {
            activeHomeCollectors--
        }
    }

    override fun getJetpack(id: String): Flow<Jetpack> = flow {
        itemSubscriptions++
        activeItemCollectors++
        try {
            loadFailure?.let { throw it }
            existing?.let { emit(it) }
            awaitCancellation()
        } finally {
            activeItemCollectors--
        }
    }

    override suspend fun createOrUpdateJetpack(jetpack: Jetpack): Result<Unit> {
        saved = jetpack
        return saveResult
    }

    override suspend fun markJetpackAsDeleted(jetpack: Jetpack): Result<Unit> =
        Result.success(Unit)

    override suspend fun sync(): Flow<SyncProgress> = emptyFlow()
}
