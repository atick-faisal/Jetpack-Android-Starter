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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atick.core.extensions.asOneTimeEvent
import dev.atick.core.extensions.stateInDelayed
import dev.atick.core.ui.utils.UiState
import dev.atick.core.ui.utils.updateState
import dev.atick.core.ui.utils.updateStateWith
import dev.atick.core.utils.OneTimeEvent
import dev.atick.data.model.home.Jetpack
import dev.atick.data.repository.home.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import java.util.UUID

/**
 * ViewModel for the create/edit item screen, managing Jetpack library creation and updates.
 *
 * This ViewModel handles both creating new Jetpack items and editing existing ones. The optional
 * item ID arrives by assisted injection from the navigation key. If an ID is present, the screen
 * operates in edit mode; otherwise, it creates a new item with a generated UUID.
 *
 * The ViewModel demonstrates:
 * - Navigation argument handling by assisted injection from a Navigation 3 key
 * - Form state management with [updateState] for field updates
 * - Navigation events using [OneTimeEvent]; save currently ignores the repository Result
 * - Async operations with [updateStateWith] for create/update operations
 *
 * @param homeRepository Repository providing Jetpack data operations.
 * @param existingJetpackId The item to edit, or null to create a new one.
 *
 * @see ItemScreenData Immutable data class representing form state
 * @see UiState State wrapper with loading and error handling
 * @see updateState Extension function for synchronous state updates
 * @see updateStateWith Extension function for async operations with state updates
 * @see OneTimeEvent Ensures navigation events are consumed only once
 * @see HomeRepository Data layer interface for home screen operations
 */
@HiltViewModel(assistedFactory = ItemViewModel.Factory::class)
class ItemViewModel @AssistedInject constructor(
    private val homeRepository: HomeRepository,
    @Assisted private val existingJetpackId: String?,
) : ViewModel() {

    /**
     * Creates an [ItemViewModel] for a given item.
     *
     * Navigation 3 carries destination arguments on the [dev.atick.feature.home.navigation.ItemNavKey]
     * itself rather than in a SavedStateHandle, so the id is supplied here at the point the entry
     * is composed.
     */
    @AssistedFactory
    interface Factory {
        /**
         * @param existingJetpackId The item to edit, or null to create a new one.
         */
        fun create(existingJetpackId: String?): ItemViewModel
    }

    private val _itemUiState = MutableStateFlow(UiState(ItemScreenData()))

    // As in HomeViewModel, upstream restarts add collectors that live until the ViewModel is cleared.
    val itemUiState = _itemUiState
        .onStart { getJetpack() }
        .stateInDelayed(UiState(ItemScreenData()), viewModelScope)

    fun updateName(name: String) {
        _itemUiState.updateState { copy(jetpackName = name) }
    }

    // Keep raw text, including empty or incomplete input; parse only when saving.
    fun updatePrice(priceString: String) {
        _itemUiState.updateState { copy(jetpackPrice = priceString) }
    }

    /**
     * Saves the form and requests back navigation even when the repository returns failure.
     * The repository Result is currently ignored; this event does not confirm persistence.
     */
    fun createOrUpdateJetpack() {
        _itemUiState.updateStateWith {
            val jetpack = Jetpack(
                id = jetpackId,
                name = jetpackName.trim(),
                price = jetpackPrice.trim().toDoubleOrNull() ?: 0.0,
            )
            homeRepository.createOrUpdateJetpack(jetpack)
            Result.success(copy(navigateBack = OneTimeEvent(true)))
        }
    }

    // The catch below constructs a state without storing it, so load errors are not exposed.
    private fun getJetpack() {
        existingJetpackId?.let {
            homeRepository.getJetpack(existingJetpackId)
                .onEach { jetpack ->
                    _itemUiState.updateState {
                        copy(
                            jetpackId = jetpack.id,
                            jetpackName = jetpack.name,
                            jetpackPrice = jetpack.price.toString(),
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
 * When creating a new item, [ItemScreenData.jetpackId] is pre-populated with a random UUID. When editing,
 * the ID and other fields are loaded from the repository via `getJetpack()`.
 *
 * The [ItemScreenData.navigateBack] event is set after the save call returns, including a returned failure.
 * ItemScreen consumes it inside an effect. Consumption is at most once per event instance and
 * does not acknowledge completed navigation.
 *
 * @param jetpackId Unique identifier for the Jetpack item (UUID for new items).
 * @param jetpackName Display name of the Jetpack library (e.g., "Compose", "Room").
 * @param jetpackPrice Price exactly as typed, kept as text so a partially entered or cleared
 *                     value survives; parsed to a [Double] only when the item is saved.
 * @param navigateBack Consumable request to navigate back after the save call returns.
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
    val jetpackPrice: String = "",
    val navigateBack: OneTimeEvent<Boolean> = OneTimeEvent(false),
)
