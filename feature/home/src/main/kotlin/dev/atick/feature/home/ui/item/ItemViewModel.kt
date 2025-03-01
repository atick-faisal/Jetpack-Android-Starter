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

package dev.atick.feature.home.ui.item

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atick.core.extensions.asOneTimeEvent
import dev.atick.core.extensions.stateInDelayed
import dev.atick.core.ui.utils.UiState
import dev.atick.core.ui.utils.asUiState
import dev.atick.core.ui.utils.updateState
import dev.atick.core.ui.utils.updateStateWith
import dev.atick.core.utils.OneTimeEvent
import dev.atick.data.model.home.Jetpack
import dev.atick.data.repository.home.HomeRepository
import dev.atick.feature.home.navigation.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import java.util.UUID
import javax.inject.Inject

/**
 * Item view model.
 *
 * @param homeRepository [HomeRepository].
 * @param savedStateHandle [SavedStateHandle].
 */
@HiltViewModel
class ItemViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val existingJetpackId: String? = savedStateHandle.toRoute<Item>().itemId

    private val _itemUiState = MutableStateFlow(UiState(ItemScreenData()))
    val itemUiState = _itemUiState
        .onStart { getJetpack() }
        .stateInDelayed(ItemScreenData::class.asUiState(), viewModelScope)

    fun updateName(name: String) {
        _itemUiState.updateState { copy(jetpackName = name) }
    }

    fun updatePrice(priceString: String) {
        val price = priceString.trim().toDoubleOrNull() ?: return
        _itemUiState.updateState { copy(jetpackPrice = price) }
    }

    fun createOrUpdateJetpack() {
        _itemUiState.updateStateWith(viewModelScope) {
            val jetpack = Jetpack(
                id = jetpackId,
                name = jetpackName.trim(),
                price = jetpackPrice,
            )
            homeRepository.createOrUpdateJetpack(jetpack)
            Result.success(copy(navigateBack = OneTimeEvent(true)))
        }
    }

    private fun getJetpack() {
        existingJetpackId?.let {
            homeRepository.getJetpack(existingJetpackId)
                .onEach { jetpack ->
                    _itemUiState.updateState {
                        copy(
                            jetpackId = jetpack.id,
                            jetpackName = jetpack.name,
                            jetpackPrice = jetpack.price,
                        )
                    }
                }
                .catch { e -> UiState(ItemScreenData(), error = e.asOneTimeEvent()) }
                .launchIn(viewModelScope)
        }
    }
}

/**
 * Item screen data.
 *
 * @param jetpackId The jetpack ID.
 * @param jetpackName The jetpack name.
 * @param jetpackPrice The jetpack price.
 * @param navigateBack The navigate back event.
 */
@Immutable
data class ItemScreenData(
    val jetpackId: String = UUID.randomUUID().toString(),
    val jetpackName: String = "",
    val jetpackPrice: Double = 0.0,
    val navigateBack: OneTimeEvent<Boolean> = OneTimeEvent(false),
)
