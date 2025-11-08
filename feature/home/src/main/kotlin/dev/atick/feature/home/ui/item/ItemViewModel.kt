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
 * ViewModel for the create/edit item screen, managing Jetpack library creation and updates.
 *
 * This ViewModel handles both creating new Jetpack items and editing existing ones. It uses
 * [SavedStateHandle] to retrieve the optional item ID from navigation arguments. If an ID is
 * present, the screen operates in edit mode; otherwise, it creates a new item with a generated UUID.
 *
 * The ViewModel demonstrates:
 * - Navigation argument handling via [SavedStateHandle.toRoute]
 * - Form state management with [updateState] for field updates
 * - Navigation events using [OneTimeEvent] to trigger back navigation after save
 * - Async operations with [updateStateWith] for create/update operations
 *
 * @param homeRepository Repository providing Jetpack data operations.
 * @param savedStateHandle Navigation state containing optional item ID from route.
 *
 * @see ItemScreenData Immutable data class representing form state
 * @see UiState State wrapper with loading and error handling
 * @see updateState Extension function for synchronous state updates
 * @see updateStateWith Extension function for async operations with state updates
 * @see OneTimeEvent Ensures navigation events are consumed only once
 * @see HomeRepository Data layer interface for home screen operations
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
        .stateInDelayed(UiState(ItemScreenData()), viewModelScope)

    fun updateName(name: String) {
        _itemUiState.updateState { copy(jetpackName = name) }
    }

    fun updatePrice(priceString: String) {
        val price = priceString.trim().toDoubleOrNull() ?: return
        _itemUiState.updateState { copy(jetpackPrice = price) }
    }

    fun createOrUpdateJetpack() {
        _itemUiState.updateStateWith {
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
 * Immutable data class representing the state of the create/edit item form.
 *
 * This class manages form state for both creating new Jetpack items and editing existing ones.
 * When creating a new item, [jetpackId] is pre-populated with a random UUID. When editing,
 * the ID and other fields are loaded from the repository via [ItemViewModel.getJetpack].
 *
 * The [navigateBack] event is triggered after a successful create/update operation and is
 * consumed by the Route composable to navigate back to the home screen. Being wrapped in
 * [OneTimeEvent] ensures the navigation only happens once, even if the state is re-emitted.
 *
 * Usage context:
 * - Route composable observes [ItemViewModel.itemUiState] which wraps this data class
 * - Form fields bind to [jetpackName] and [jetpackPrice] with two-way data flow
 * - Save button triggers [ItemViewModel.createOrUpdateJetpack] which sets [navigateBack]
 * - Navigation observer consumes [navigateBack] event to pop the back stack
 *
 * @param jetpackId Unique identifier for the Jetpack item (UUID for new items).
 * @param jetpackName Display name of the Jetpack library (e.g., "Compose", "Room").
 * @param jetpackPrice Price value for the Jetpack library.
 * @param navigateBack One-time event to trigger back navigation after successful save.
 *
 * @see ItemViewModel ViewModel that manages this screen data
 * @see UiState Wrapper providing loading and error state
 * @see OneTimeEvent Ensures events are consumed only once
 * @see Jetpack Domain model created from this form data
 */
@Immutable
data class ItemScreenData(
    val jetpackId: String = UUID.randomUUID().toString(),
    val jetpackName: String = "",
    val jetpackPrice: Double = 0.0,
    val navigateBack: OneTimeEvent<Boolean> = OneTimeEvent(false),
)
