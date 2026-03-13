/*
 * Copyright 2023 Atick Faisal
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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.atick.core.ui.components.JetpackActionBar
import dev.atick.core.ui.components.JetpackTextFiled
import dev.atick.core.ui.utils.PreviewDevices
import dev.atick.core.ui.utils.PreviewThemes
import dev.atick.core.ui.utils.SnackbarAction
import dev.atick.core.ui.utils.StatefulComposable
import dev.atick.feature.home.R

/**
 * Item screen.
 *
 * @param onBackClick The back click listener.
 * @param onShowSnackbar The snackbar callback.
 * @param itemViewModel The [ItemViewModel].
 */
@Composable
internal fun ItemScreen(
    onBackClick: () -> Unit,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    itemViewModel: ItemViewModel = hiltViewModel(),
) {
    val itemUiState by itemViewModel.itemUiState.collectAsStateWithLifecycle()

    StatefulComposable(
        state = itemUiState,
        onShowSnackbar = onShowSnackbar,
    ) { screenData ->

        LaunchedEffect(screenData.navigateBack, onBackClick) {
            if (screenData.navigateBack.getContentIfNotHandled() == true) {
                onBackClick()
            }
        }

        ItemScreen(
            name = screenData.jetpackName,
            price = screenData.jetpackPrice,
            onUpdateName = itemViewModel::updateName,
            onUpdatePrice = itemViewModel::updatePrice,
            onSaveClick = itemViewModel::createOrUpdateJetpack,
            onBackClick = onBackClick,
        )
    }
}

/**
 * Item screen.
 *
 * @param name The name.
 * @param price The price.
 * @param onUpdateName The name update listener.
 * @param onUpdatePrice The price update listener.
 * @param onSaveClick The save click listener.
 * @param onBackClick The back click listener.
 */
@Composable
private fun ItemScreen(
    name: String,
    price: Double,
    onUpdateName: (String) -> Unit,
    onUpdatePrice: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        JetpackActionBar(
            titleRes = R.string.jetpack,
            actionRes = R.string.save,
            onActionClick = onSaveClick,
            onNavigateBackClick = onBackClick,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
        )
        EditItemForm(
            name = name,
            price = price,
            onUpdateName = onUpdateName,
            onUpdatePrice = onUpdatePrice,
        )
    }
}

/**
 * Edit item form.
 *
 * @param name The name.
 * @param price The price.
 * @param onUpdateName The name update listener.
 * @param onUpdatePrice The price update listener.
 */
@Composable
private fun EditItemForm(
    name: String,
    price: Double,
    onUpdateName: (String) -> Unit,
    onUpdatePrice: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        JetpackTextFiled(
            value = name,
            onValueChange = onUpdateName,
            label = { Text(text = stringResource(id = R.string.jetpack_name)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = stringResource(id = R.string.jetpack_name),
                )
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
        JetpackTextFiled(
            value = price.toString(),
            onValueChange = onUpdatePrice,
            label = { Text(text = stringResource(id = R.string.price)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Wallet,
                    contentDescription = stringResource(id = R.string.price),
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        )
    }
}

@Composable
@PreviewThemes
@PreviewDevices
private fun ItemScreenPreview() {
    ItemScreen(
        name = "Jetpack",
        price = 100.0,
        onUpdateName = {},
        onUpdatePrice = {},
        onSaveClick = {},
        onBackClick = {},
    )
}
