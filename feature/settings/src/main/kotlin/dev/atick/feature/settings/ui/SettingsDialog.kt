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

package dev.atick.feature.settings.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dev.atick.core.ui.components.JetpackOutlinedButton
import dev.atick.core.ui.components.JetpackTextButton
import dev.atick.core.ui.components.JetpackToggleOptions
import dev.atick.core.ui.components.ToggleOption
import dev.atick.core.ui.theme.supportsDynamicTheming
import dev.atick.core.ui.utils.PreviewDevices
import dev.atick.core.ui.utils.PreviewThemes
import dev.atick.core.ui.utils.SnackbarAction
import dev.atick.core.ui.utils.StatefulComposable
import dev.atick.data.model.settings.DarkThemeConfig
import dev.atick.data.model.settings.Language
import dev.atick.data.model.settings.Settings
import dev.atick.data.utils.FEEDBACK_URL
import dev.atick.data.utils.PRIVACY_POLICY_URL
import dev.atick.data.utils.TERMS_OF_SERVICE_URL
import dev.atick.feature.settings.R

/**
 * Settings dialog.
 *
 * @param onDismiss Callback when the dialog is dismissed.
 * @param onShowSnackbar Callback to show a snackbar.
 * @param settingsViewModel [SettingsViewModel].
 */
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val settingsState by settingsViewModel.settingsUiState.collectAsStateWithLifecycle()

    StatefulComposable(
        state = settingsState,
        onShowSnackbar = onShowSnackbar,
    ) { settings ->
        SettingsDialog(
            settings = settings,
            onDismiss = onDismiss,
            onChangeDynamicColorPreference = settingsViewModel::updateDynamicColorPreference,
            onChangeDarkThemeConfig = settingsViewModel::updateDarkThemeConfig,
            onChangeLanguage = settingsViewModel::updateLanguagePreference,
            onSignOut = settingsViewModel::signOut,
        )
    }
}

/**
 * Settings dialog.
 *
 * @param settings [Settings].
 * @param onDismiss Callback when the dialog is dismissed.
 * @param onChangeDynamicColorPreference Callback when the dynamic color preference is changed.
 * @param onChangeDarkThemeConfig Callback when the dark theme config is changed.
 * @param onSignOut Callback when the user signs out.
 * @param supportDynamicColor Whether dynamic color is supported.
 */
@Composable
private fun SettingsDialog(
    settings: Settings,
    onDismiss: () -> Unit,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
    onChangeLanguage: (language: Language) -> Unit,
    onSignOut: () -> Unit,
    supportDynamicColor: Boolean = supportsDynamicTheming(),
) {
    val configuration = LocalConfiguration.current

    /**
     * usePlatformDefaultWidth = false is use as a temporary fix to allow
     * height recalculation during recomposition. This, however, causes
     * Dialog's to occupy full width in Compact mode. Therefore max width
     * is configured below. This should be removed when there's fix to
     * https://issuetracker.google.com/issues/221643630
     */
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.widthIn(max = configuration.screenWidthDp.dp - 80.dp),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = settings.userName ?: stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            HorizontalDivider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SettingsPanel(
                    settings = settings,
                    supportDynamicColor = supportDynamicColor,
                    onChangeDynamicColorPreference = onChangeDynamicColorPreference,
                    onChangeDarkThemeConfig = onChangeDarkThemeConfig,
                    onChangeLanguage = onChangeLanguage,
                    onSignOut = onSignOut,
                    onDismiss = onDismiss,
                )
                HorizontalDivider(Modifier.padding(top = 8.dp))
                LinksPanel()
            }
        },
        confirmButton = {
            Text(
                text = stringResource(R.string.dismiss_dialog_button_text),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { onDismiss() },
            )
        },
    )
}

/**
 * Settings panel.
 * ColumnScope] is used for using the [ColumnScope.AnimatedVisibility] extension overload composable.
 *
 * @param settings [Settings].
 * @param supportDynamicColor Whether dynamic color is supported.
 * @param onChangeDynamicColorPreference Callback when the dynamic color preference is changed.
 * @param onChangeDarkThemeConfig Callback when the dark theme config is changed.
 * @param onSignOut Callback when the user signs out.
 * @param onDismiss Callback when the dialog is dismissed.
 */
@Composable
private fun ColumnScope.SettingsPanel(
    settings: Settings,
    supportDynamicColor: Boolean,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
    onChangeLanguage: (language: Language) -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit,
) {
    var languageSelectedIndex by remember(settings.language) {
        mutableIntStateOf(
            Language.entries.indexOfFirst { it == settings.language },
        )
    }

    SettingsDialogSectionTitle(text = stringResource(R.string.language))
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        JetpackToggleOptions(
            options = getLanguageOptions(),
            selectedIndex = languageSelectedIndex,
            onSelectionChange = {
                languageSelectedIndex = it
                onChangeLanguage(Language.entries[it])
            },
        )
    }
    AnimatedVisibility(visible = supportDynamicColor) {
        Column {
            SettingsDialogSectionTitle(text = stringResource(R.string.dynamic_color_preference))
            Column(Modifier.selectableGroup()) {
                SettingsDialogThemeChooserRow(
                    text = stringResource(R.string.dynamic_color_yes),
                    selected = settings.useDynamicColor,
                    onClick = { onChangeDynamicColorPreference(true) },
                )
                SettingsDialogThemeChooserRow(
                    text = stringResource(R.string.dynamic_color_no),
                    selected = !settings.useDynamicColor,
                    onClick = { onChangeDynamicColorPreference(false) },
                )
            }
        }
    }
    SettingsDialogSectionTitle(text = stringResource(R.string.dark_mode_preference))
    Column(Modifier.selectableGroup()) {
        SettingsDialogThemeChooserRow(
            text = stringResource(R.string.dark_mode_config_system_default),
            selected = settings.darkThemeConfig == DarkThemeConfig.FOLLOW_SYSTEM,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.FOLLOW_SYSTEM) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(R.string.dark_mode_config_light),
            selected = settings.darkThemeConfig == DarkThemeConfig.LIGHT,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.LIGHT) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(R.string.dark_mode_config_dark),
            selected = settings.darkThemeConfig == DarkThemeConfig.DARK,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.DARK) },
        )
    }
    JetpackOutlinedButton(
        onClick = {
            onSignOut()
            onDismiss()
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.sign_out))
    }
}

/**
 * Settings dialog section title.
 *
 * @param text The title text.
 */
@Composable
private fun SettingsDialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

/**
 * Settings dialog theme chooser row.
 *
 * @param text The text to display.
 * @param selected Whether the row is selected.
 * @param onClick Callback when the row is clicked.
 */
@Composable
private fun SettingsDialogThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

/**
 * Links panel.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinksPanel() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterHorizontally,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        val uriHandler = LocalUriHandler.current
        JetpackTextButton(
            onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) },
        ) {
            Text(text = stringResource(R.string.privacy_policy))
        }
        JetpackTextButton(
            onClick = { uriHandler.openUri(TERMS_OF_SERVICE_URL) },
        ) {
            Text(text = stringResource(R.string.terms_of_service))
        }
        val context = LocalContext.current
        JetpackTextButton(
            onClick = {
                context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            },
        ) {
            Text(text = stringResource(R.string.licenses))
        }
        JetpackTextButton(
            onClick = { uriHandler.openUri(FEEDBACK_URL) },
        ) {
            Text(text = stringResource(R.string.feedback))
        }
    }
}

private fun getLanguageOptions(): List<ToggleOption> {
    return Language.entries.map { language: Language ->
        when (language) {
            Language.ENGLISH -> ToggleOption(R.string.en, Icons.Default.Language)
            Language.ARABIC -> ToggleOption(R.string.ar, Icons.Default.Translate)
        }
    }
}

@Composable
@PreviewThemes
@PreviewDevices
private fun SettingsDialogPreview() {
    SettingsDialog(
        settings = Settings(),
        onDismiss = {},
        onChangeDynamicColorPreference = {},
        onChangeDarkThemeConfig = {},
        onChangeLanguage = {},
        onSignOut = {},
        supportDynamicColor = true,
    )
}
