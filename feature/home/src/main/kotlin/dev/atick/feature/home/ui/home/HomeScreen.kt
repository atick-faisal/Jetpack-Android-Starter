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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(300.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp,
        state = state,
    ) {
        items(items = jetpacks, key = { it.id }) { jetpack ->
            SwipeToDismiss(onDelete = { onDeleteJetpack(jetpack) }) {
                JetpackCard(
                    jetpack = jetpack,
                    onClick = { onJetpackCLick(jetpack.id) },
                )
            }
        }
    }
}

/**
 * Jetpack card.
 *
 * @param jetpack The jetpack.
 * @param modifier The modifier.
 * @param onClick The click listener.
 */
@Composable
fun JetpackCard(
    jetpack: Jetpack,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = jetpack.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Sync status indicator
                if (jetpack.needsSync) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = "Needs sync",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ID: ${jetpack.id}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$ ${jetpack.price.format(2)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = jetpack.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
