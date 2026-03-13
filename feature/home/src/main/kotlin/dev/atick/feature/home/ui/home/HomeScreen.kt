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

package dev.atick.feature.home.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.atick.core.extensions.format
import dev.atick.core.ui.components.SwipeToDismiss
import dev.atick.core.ui.utils.PreviewDevices
import dev.atick.core.ui.utils.PreviewThemes
import dev.atick.core.ui.utils.SnackbarAction
import dev.atick.core.ui.utils.StatefulComposable
import dev.atick.data.model.home.Jetpack

/**
 * Home screen.
 *
 * @param onJetpackClick The click listener for jetpacks.
 * @param onShowSnackbar The snackbar callback.
 * @param homeViewModel The [HomeViewModel].
 */
@Composable
internal fun HomeScreen(
    onJetpackClick: (String) -> Unit,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val homeState by homeViewModel.homeUiState.collectAsStateWithLifecycle()

    StatefulComposable(
        state = homeState,
        onShowSnackbar = onShowSnackbar,
    ) { homeScreenData ->
        HomeScreen(
            jetpacks = homeScreenData.jetpacks,
            onJetpackCLick = onJetpackClick,
            onDeleteJetpack = homeViewModel::deleteJetpack,
        )
    }
}

/**
 * Home screen.
 *
 * @param jetpacks The list of jetpacks.
 * @param onJetpackCLick The click listener for jetpacks.
 * @param onDeleteJetpack The delete listener for jetpacks.
 */
@Composable
private fun HomeScreen(
    jetpacks: List<Jetpack>,
    onJetpackCLick: (String) -> Unit,
    onDeleteJetpack: (Jetpack) -> Unit,
) {
    val state = rememberLazyStaggeredGridState()

    Surface(
        modifier = Modifier.padding(horizontal = 2.dp),
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent,
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(150.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalItemSpacing = 2.dp,
            state = state,
        ) {
            items(items = jetpacks, key = { it.id }) { jetpack ->
                SwipeToDismiss(onDelete = { onDeleteJetpack(jetpack) }) {
                    ListItem(
                        onClick = { onJetpackCLick(jetpack.id) },
                        leadingContent = {
                            Icon(
                                Icons.Default.RocketLaunch,
                                contentDescription = jetpack.name,
                            )
                        },
                        overlineContent = { Text(jetpack.formattedDate) },
                        content = { Text(jetpack.name) },
                        supportingContent = { Text(jetpack.price.format(isCurrency = true)) },
                        trailingContent = {
                            if (jetpack.needsSync) {
                                Icon(
                                    Icons.Default.CloudSync,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
@PreviewThemes
@PreviewDevices
private fun HomeScreenPreview() {
    HomeScreen(
        jetpacks = listOf(
            Jetpack(
                id = "1",
                name = "Jetpack 1",
                price = 100.0,
                formattedDate = "JANUARY 3, 2023-10-01 at 8:45 PM",
                needsSync = false,
            ),
            Jetpack(
                id = "2",
                name = "Jetpack 2",
                price = 200.0,
                formattedDate = "FEBRUARY 3, 2023-10-02 at 8:45 PM",
                needsSync = true,
            ),
            Jetpack(
                id = "3",
                name = "Jetpack 3",
                price = 300.0,
                formattedDate = "MARCH 3, 2023-10-03 at 8:45 PM",
                needsSync = false,
            ),
        ),
        onJetpackCLick = {},
        onDeleteJetpack = {},
    )
}
