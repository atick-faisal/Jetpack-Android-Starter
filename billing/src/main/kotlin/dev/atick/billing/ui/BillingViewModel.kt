/*
 * Copyright 2024 Atick Faisal
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

package dev.atick.billing.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atick.billing.models.BillingScreenData
import dev.atick.billing.models.Product
import dev.atick.billing.repository.BillingRepository
import dev.atick.core.extensions.asOneTimeEvent
import dev.atick.core.ui.utils.UiState
import dev.atick.core.ui.utils.updateWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
) : ViewModel() {

    private val _billingUiState = MutableStateFlow(UiState(BillingScreenData()))
    val billingUiState = _billingUiState.asStateFlow()

    init {
        billingRepository.products
            .onEach { products ->
                _billingUiState.update { UiState(it.data.copy(products = products)) }
            }
            .catch { e -> _billingUiState.update { it.copy(error = e.asOneTimeEvent()) } }
            .launchIn(viewModelScope)
    }

    fun updateProductsAndPurchases() {
        _billingUiState.updateWith(viewModelScope) {
            billingRepository.updateProductsAndPurchases()
        }
    }

    fun purchaseProduct(activity: Activity, product: Product) {
        _billingUiState.updateWith(viewModelScope) {
            billingRepository.purchaseProduct(activity, product)
        }
    }
}
