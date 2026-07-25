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

package dev.atick.compose.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.atick.compose.R
import dev.atick.compose.navigation.TopLevelDestination
import dev.atick.core.navigation.Navigator
import dev.atick.core.navigation.backStack
import dev.atick.core.navigation.rememberDefaultEntryDecorators
import dev.atick.core.navigation.rememberNavigationState
import dev.atick.core.ui.components.AppBackground
import dev.atick.core.ui.components.AppGradientBackground
import dev.atick.core.ui.components.JetpackExtendedFab
import dev.atick.core.ui.components.JetpackNavigationSuiteScaffold
import dev.atick.core.ui.components.JetpackNavigationSuiteScope
import dev.atick.core.ui.components.JetpackTopAppBarWithAvatar
import dev.atick.core.ui.theme.GradientColors
import dev.atick.core.ui.theme.LocalGradientColors
import dev.atick.core.ui.utils.SnackbarAction
import dev.atick.feature.auth.navigation.SignInNavKey
import dev.atick.feature.auth.navigation.authEntries
import dev.atick.feature.home.navigation.homeEntries
import dev.atick.feature.home.navigation.navigateToItem
import dev.atick.feature.profile.navigation.profileEntries
import dev.atick.feature.settings.ui.SettingsDialog

/**
 * Root of the app UI.
 *
 * Chooses between the signed-out and signed-in navigation graphs. Each branch builds its own
 * navigation state, so signing in or out discards the other branch's back stack rather than
 * leaving history the user could return to.
 *
 * @param appState App-wide state.
 * @param modifier Modifier for the root layout.
 * @param windowAdaptiveInfo Drives whether navigation shows as a bar, rail or drawer.
 */
@Composable
fun JetpackApp(
    appState: JetpackAppState,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    AppBackground(modifier = modifier) {
        val snackbarHostState = remember { SnackbarHostState() }

        val isOffline by appState.isOffline.collectAsStateWithLifecycle()
        val notConnectedMessage = stringResource(R.string.not_connected)
        LaunchedEffect(isOffline) {
            if (isOffline) {
                snackbarHostState.showSnackbar(
                    message = notConnectedMessage,
                    duration = SnackbarDuration.Indefinite,
                )
            }
        }

        val context = LocalContext.current

        // Resolved here because the callback runs outside composition and so cannot read
        // resources itself.
        @Suppress("LocalContextGetResourceValueCall")
        val onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean =
            { message, action, throwable ->
                val actionPerformed = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = context.getString(action.actionText),
                    duration = SnackbarDuration.Short,
                ) == SnackbarResult.ActionPerformed
                if (actionPerformed && action == SnackbarAction.REPORT) {
                    throwable?.let(appState.crashReporter::reportException)
                }
                actionPerformed
            }

        if (appState.isUserLoggedIn) {
            SignedInNavigation(
                appState = appState,
                snackbarHostState = snackbarHostState,
                onShowSnackbar = onShowSnackbar,
                windowAdaptiveInfo = windowAdaptiveInfo,
            )
        } else {
            SignedOutNavigation(
                snackbarHostState = snackbarHostState,
                onShowSnackbar = onShowSnackbar,
            )
        }
    }
}

/**
 * The signed-out graph: sign in and sign up, with no navigation bar and no top app bar.
 */
@Composable
private fun SignedOutNavigation(
    snackbarHostState: SnackbarHostState,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val navigationState = rememberNavigationState(
        startKey = SignInNavKey,
        topLevelKeys = setOf(SignInNavKey),
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    BackHandler(enabled = navigator.canGoBack()) { navigator.goBack() }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        NavDisplay(
            backStack = navigationState.backStack,
            entryDecorators = rememberDefaultEntryDecorators(),
            onBack = { if (navigator.canGoBack()) navigator.goBack() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            entryProvider = entryProvider {
                authEntries(navigator = navigator, onShowSnackbar = onShowSnackbar)
            },
        )
    }
}

/**
 * The signed-in graph: the top-level destinations, their nested screens, and the chrome around
 * them.
 */
@Composable
private fun SignedInNavigation(
    appState: JetpackAppState,
    snackbarHostState: SnackbarHostState,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    windowAdaptiveInfo: WindowAdaptiveInfo,
    modifier: Modifier = Modifier,
) {
    val navigationState = rememberNavigationState(
        startKey = TopLevelDestination.START.key,
        topLevelKeys = TopLevelDestination.keys,
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val currentTopLevelDestination =
        TopLevelDestination.fromKey(navigationState.currentTopLevelKey)
    val isAtTopLevel = navigationState.currentKey == navigationState.currentTopLevelKey

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onShowSnackbar = onShowSnackbar,
        )
    }

    // Only handle back while there is somewhere to go. At the start destination this stays
    // disabled so the system finishes the activity, rather than Navigator.goBack() throwing.
    BackHandler(enabled = navigator.canGoBack()) { navigator.goBack() }

    val shouldShowGradientBackground = currentTopLevelDestination == TopLevelDestination.HOME

    AppGradientBackground(
        gradientColors = if (shouldShowGradientBackground) {
            LocalGradientColors.current
        } else {
            GradientColors()
        },
    ) {
        JetpackNavigationSuiteScaffold(
            navigationSuiteItems = {
                navigationItems(
                    currentDestination = currentTopLevelDestination,
                    onNavigate = navigator::navigate,
                )
            },
            windowAdaptiveInfo = windowAdaptiveInfo,
        ) {
            Scaffold(
                modifier = modifier,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = {
                    if (currentTopLevelDestination == TopLevelDestination.HOME && isAtTopLevel) {
                        JetpackExtendedFab(
                            icon = Icons.Default.RocketLaunch,
                            text = R.string.add,
                            onClick = { navigator.navigateToItem(null) },
                        )
                    }
                },
            ) { padding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                        ),
                ) {
                    // The top app bar belongs to top-level destinations. Nested screens bring
                    // their own, so showing both would stack two bars.
                    val showTopAppBar = currentTopLevelDestination != null && isAtTopLevel
                    if (showTopAppBar) {
                        JetpackTopAppBarWithAvatar(
                            titleRes = currentTopLevelDestination.titleTextId,
                            avatarUri = appState.userProfilePictureUri,
                            avatarContentDescription = stringResource(id = R.string.settings),
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                            ),
                            onAvatarClick = { showSettingsDialog = true },
                        )
                    }

                    Box(
                        // Workaround for https://issuetracker.google.com/338478720
                        modifier = Modifier.consumeWindowInsets(
                            if (showTopAppBar) {
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                            } else {
                                WindowInsets(0, 0, 0, 0)
                            },
                        ),
                    ) {
                        NavDisplay(
                            backStack = navigationState.backStack,
                            entryDecorators = rememberDefaultEntryDecorators(),
                            // Renders list and detail side by side when the window is wide
                            // enough, and as separate screens when it is not.
                            sceneStrategies = listOf(rememberListDetailSceneStrategy<NavKey>()),
                            onBack = { if (navigator.canGoBack()) navigator.goBack() },
                            entryProvider = entryProvider {
                                homeEntries(navigator, onShowSnackbar)
                                profileEntries(onShowSnackbar)
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun JetpackNavigationSuiteScope.navigationItems(
    currentDestination: TopLevelDestination?,
    onNavigate: (NavKey) -> Unit,
) {
    TopLevelDestination.entries.forEach { destination ->
        item(
            selected = destination == currentDestination,
            onClick = { onNavigate(destination.key) },
            icon = {
                Icon(
                    imageVector = destination.unselectedIcon,
                    contentDescription = null,
                )
            },
            selectedIcon = {
                Icon(
                    imageVector = destination.selectedIcon,
                    contentDescription = null,
                )
            },
            label = { Text(stringResource(destination.iconTextId)) },
        )
    }
}
