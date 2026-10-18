# Module :feature:settings

**Purpose:** Theme (light/dark/system + dynamic color), language, and sign-out, shown as a dialog
rather than a full screen. `SettingsViewModel` uses `SettingsRepository` in `:data`;
theme/color/profile values come from JSON DataStore, while language uses app locales.

## Key APIs

| API | What it does |
|---|---|
| `SettingsDialog` / `SettingsViewModel` | Theme + dynamic-color toggles, language selection, sign-out, open-source licenses |

Destination signature excerpt; imports and function body are omitted.

```kotlin
// feature/settings/src/main/kotlin/dev/atick/feature/settings/ui/SettingsDialog.kt
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onShowSnackbar: suspend (String, SnackbarAction, Throwable?) -> Boolean,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
)
```

## Gotchas

- The language picker's `when` maps every non-Arabic locale to English — adding a new
  `values-<lang>/` resource directory changes what `generateLocaleConfig` offers the system without
  updating this mapping.

- Sign-out calls Firebase Auth, then resets all preferences, including theme/color settings.
  It does not delete Room or Firestore records.
- Resubscription can add repository collectors while the ViewModel remains alive; the settings
  load catch currently constructs a state without storing its error. See
  [subscription lifetimes](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/state-management.md#subscription-lifetimes).
- Follow [localization](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/components.md#localization) when adding a language; resource
  files alone do not update the in-app picker/mapping.

## Related Documentation

- [State Management](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/state-management.md) — `UiState`/`StatefulComposable` pattern used here
- [JSON DataStore](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/data.md#typed-json-datastore) — the DataStore preferences this dialog reads/writes
