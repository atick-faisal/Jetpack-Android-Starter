# Module :core:ui

**Purpose:** Everything shared across screens — the `UiState`/`StatefulComposable` state pattern, the
`Jetpack*` design-system components, `JetpackTheme`, and Compose/Activity extensions.

## Key APIs

| API | What it does | Canonical doc |
|---|---|---|
| `UiState<T>`, `StatefulComposable`, `updateState`/`updateStateWith`/`updateWith` | Loading/data/error wrapper every screen uses, and its update functions | [State Management](../../docs/state-management.md) |
| `Jetpack*` components (`components/`) | The design-system wrappers around Material 3 | [Components](../../docs/components.md) |
| `JetpackTheme` | `MaterialExpressiveTheme`-based theme, dynamic color + fallback | [Components § Theming](../../docs/components.md#theming) |
| `UiText` | Type-safe text (string vs. `@StringRes`) so ViewModels don't need `Context` | `utils/UiText.kt` |
| `checkForPermissions`, `getActivity()` | Activity result/permission helpers (`extensions/ActivityExtensions.kt`) | — |

## Gotchas

- A raw Material 3 composable that has a `Jetpack*` equivalent (e.g. `Button`, `TextField`,
  `TopAppBar`) fails the build — `DesignSystemDetector` (`:lint`) is an ERROR-severity check,
  `lintPublish`ed into every module that depends on `core:ui`.
- Route composables connect to the ViewModel; Screen composables are pure UI with no ViewModel
  dependency, so they're testable without Hilt.

## Related Documentation

- [State Management](../../docs/state-management.md) — the full `UiState` deep dive
- [Components](../../docs/components.md) — component catalog, real usage, theming, lint enforcement
- [Architecture](../../docs/architecture.md) — where `core:ui` sits in the module graph
