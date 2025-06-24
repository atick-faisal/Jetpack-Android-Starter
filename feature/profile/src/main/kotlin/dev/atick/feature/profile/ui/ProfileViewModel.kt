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

package dev.atick.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atick.core.extensions.asOneTimeEvent
import dev.atick.core.extensions.stateInDelayed
import dev.atick.core.ui.utils.UiState
import dev.atick.core.ui.utils.updateWith
import dev.atick.data.model.profile.Profile
import dev.atick.data.repository.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * [ViewModel] for [ProfileScreen].
 *
 * @param profileRepository [ProfileRepository].
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _profileUiState = MutableStateFlow(UiState(Profile()))
    val profileUiState = _profileUiState
        .onStart { updateProfileData() }
        .stateInDelayed(UiState(Profile()), viewModelScope)

    private fun updateProfileData() {
        profileRepository.getProfile()
            .map { profileScreenData -> UiState(profileScreenData) }
            .onEach { data -> _profileUiState.update { data } }
            .catch { e -> UiState(Profile(), error = e.asOneTimeEvent()) }
            .launchIn(viewModelScope)
    }

    fun signOut() {
        _profileUiState.updateWith { profileRepository.signOut() }
    }
}
