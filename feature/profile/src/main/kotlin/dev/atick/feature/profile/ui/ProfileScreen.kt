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

package dev.atick.feature.profile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.atick.core.ui.components.JetpackOutlinedButton
import dev.atick.core.ui.utils.PreviewDevices
import dev.atick.core.ui.utils.PreviewThemes
import dev.atick.core.ui.utils.SnackbarAction
import dev.atick.core.ui.utils.StatefulComposable
import dev.atick.data.model.profile.Profile
import dev.atick.feature.profile.R

/**
 * Profile screen.
 *
 * @param onShowSnackbar Lambda function to show a snackbar message.
 * @param profileViewModel [ProfileViewModel].
 */
@Composable
internal fun ProfileScreen(
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val profileState by profileViewModel.profileUiState.collectAsStateWithLifecycle()

    StatefulComposable(
        state = profileState,
        onShowSnackbar = onShowSnackbar,
    ) { profile ->
        ProfileScreen(
            profile = profile,
            onSignOutClick = profileViewModel::signOut,
        )
    }
}

/**
 * Profile screen.
 *
 * @param profile [Profile].
 * @param onSignOutClick Lambda function to sign out.
 */
@Composable
private fun ProfileScreen(
    profile: Profile,
    onSignOutClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        AsyncImage(
            model = profile.profilePictureUri,
            contentDescription = stringResource(R.string.profile_picture),
            placeholder = painterResource(id = R.drawable.ic_avatar),
            fallback = painterResource(id = R.drawable.ic_avatar),
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = profile.userName, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.weight(1f))
        JetpackOutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onSignOutClick) {
            Text(text = stringResource(id = R.string.sign_out))
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
}

@Composable
@PreviewThemes
@PreviewDevices
private fun ProfileScreenPreview() {
    ProfileScreen(
        profile = Profile(
            userName = "Atick Faisal",
            profilePictureUri = "https://example.com/avatar.png",
        ),
        onSignOutClick = {},
    )
}
