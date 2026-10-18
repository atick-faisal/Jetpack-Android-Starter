# Module :core:ui

**Purpose:** Everything shared across screens — the `UiState`/`StatefulComposable` state pattern, the
`Jetpack*` design-system components, `JetpackTheme`, and Compose/Activity extensions.

## Key APIs

| API | What it does | Canonical doc |
|---|---|---|
| `UiState<T>`, `StatefulComposable`, `updateState`/`updateStateWith`/`updateWith` | Loading/data/error wrapper every screen uses, and its update functions | [State Management](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/state-management.md) |
| `Jetpack*` components (`components/`) | The design-system wrappers around Material 3 | [Components](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/components.md) |
| `JetpackTheme` | `MaterialExpressiveTheme`-based theme, dynamic color + fallback | [Components § Theming](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/components.md#theming) |
| `UiText` | Type-safe text (string vs. `@StringRes`) so ViewModels don't need `Context` | [Text and validation](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/components.md#uitext-and-form-errors) |
| `checkForPermissions`, `getActivity()` | Permission helpers (`extensions/ActivityExtensions.kt`); `getActivity()` is a Context extension in `:core:android` | [Permissions](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/components.md#permission-helpers) |

Shared preview annotations and their combinations are covered in [Previews](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/components.md#previews).

## Gotchas

- A raw Material 3 composable that has a `Jetpack*` equivalent (e.g. `Button`, `TextField`,
  `TopAppBar`) is reported by Android lint — `DesignSystemDetector` (`:lint`) is an ERROR-severity check,
  `lintPublish`ed into every module that depends on `core:ui`.
- Destination Screen composables obtain the ViewModel and collect its state. Preview/test their
  presentational children with data and callbacks; see the [feature guide](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/guide.md).
- Async update helpers handle returned `Result` failures; they do not convert thrown exceptions
  into state errors. Their loading guard is not an atomic concurrency lock. Event consumption does
  not guarantee snackbar delivery. See the state guide before reusing these helpers.

## Related Documentation

- [State Management](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/state-management.md) — the full `UiState` deep dive
- [Components](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/components.md) — component catalog, real usage, theming, lint enforcement
- [Architecture](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/docs/architecture.md) — where `core:ui` sits in the module graph
