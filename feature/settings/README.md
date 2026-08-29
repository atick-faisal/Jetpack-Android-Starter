# Module :feature:settings

**Purpose:** Theme (light/dark/system + dynamic color), language, and sign-out, shown as a dialog
rather than a full screen.

## Key APIs

| API | What it does |
|---|---|
| `SettingsDialog` / `SettingsViewModel` | Theme + dynamic-color toggles, language selection, sign-out, open-source licenses |

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

## Related Documentation

- [State Management](../../docs/state-management.md) — `UiState`/`StatefulComposable` pattern used here
- [Core Preferences](../../core/preferences/README.md) — the DataStore preferences this dialog reads/writes
