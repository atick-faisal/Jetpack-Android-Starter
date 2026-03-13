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

package dev.atick.feature.home.ui.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atick.core.extensions.asOneTimeEvent
import dev.atick.core.extensions.stateInDelayed
import dev.atick.core.ui.utils.UiState
import dev.atick.core.ui.utils.updateWith
import dev.atick.data.model.home.Jetpack
import dev.atick.data.repository.home.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the home screen, managing the list of Jetpack libraries.
 *
 * This ViewModel follows the standard state management pattern using [UiState] to wrap
 * [HomeScreenData]. It observes the repository's Flow and automatically updates the UI state.
 * The [deleteJetpack] method uses [updateWith] for async operations with automatic error handling.
 *
 * @param homeRepository Repository providing Jetpack data and operations.
 *
 * @see HomeScreenData Immutable data class representing the screen state
 * @see UiState State wrapper with loading and error handling
 * @see updateWith Extension function for async operations that don't return new data
 * @see HomeRepository Data layer interface for home screen operations
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
) : ViewModel() {
    private val _homeUiState = MutableStateFlow(UiState(HomeScreenData()))
    val homeUiState = _homeUiState
        .onStart { getJetpacks() }
        .stateInDelayed(UiState(HomeScreenData()), viewModelScope)

    private fun getJetpacks() {
        homeRepository.getJetpacks()
            .map(::HomeScreenData)
            .onEach { homeScreenData -> _homeUiState.update { UiState(homeScreenData) } }
            .catch { e -> _homeUiState.update { it.copy(error = e.asOneTimeEvent()) } }
            .launchIn(viewModelScope)
    }

    fun deleteJetpack(jetpack: Jetpack) {
        _homeUiState.updateWith {
            homeRepository.markJetpackAsDeleted(jetpack)
        }
    }
}

/**
 * Immutable data class representing the state of the home screen.
 *
 * This class is wrapped in [UiState] by [HomeViewModel] to provide loading and error handling.
 * Being immutable (via @Immutable annotation) enables Compose to skip recomposition when the
 * data hasn't changed.
 *
 * @param jetpacks List of Jetpack libraries to display on the home screen.
 *
 * @see HomeViewModel ViewModel that manages this screen data
 * @see UiState Wrapper providing loading and error state
 * @see Jetpack Domain model for Jetpack library items
 */
@Immutable
data class HomeScreenData(
    val jetpacks: List<Jetpack> = emptyList(),
)
