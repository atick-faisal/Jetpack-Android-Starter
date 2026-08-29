# Components

The design system in `core/ui/src/main/kotlin/dev/atick/core/ui/components/` is not an optional
convention — a lint check enforces it. Calling a raw Material 3 composable that has a `Jetpack*`
equivalent fails the build with an **ERROR**, not a warning (see [Enforcement](#enforcement)). This
page catalogs what exists, shows how it's actually used in the app, and covers the theme it renders
against.

## Catalog

24 composables across 12 files, all in `core/ui/src/main/kotlin/dev/atick/core/ui/components/`.

### Buttons — `Button.kt`

| Component | Purpose |
|---|---|
| `JetpackButton` | Filled button, high emphasis. Container color is `colorScheme.onBackground` |
| `JetpackOutlinedButton` | Outlined button, medium emphasis. 1.dp border in `colorScheme.outline` |
| `JetpackTextButton` | No background, low emphasis |

Each has two overloads: `text: @Composable RowScope.() -> Unit` plus an optional `leadingIcon`, or a
bare `content: @Composable RowScope.() -> Unit` slot for anything else.

### Text input — `TextField.kt`

| Component | Purpose |
|---|---|
| `JetpackTextField` | `OutlinedTextField` wrapper, 50%-rounded corners, animated red border/error text when `errorMessage != null` |
| `JetpackPasswordField` | Wraps `JetpackTextField`, adds a visibility-toggle trailing icon; masking state survives config changes via `rememberSaveable` |

### App bars — `TopAppBar.kt`

| Component | Purpose |
|---|---|
| `JetpackTopAppBar` | `CenterAlignedTopAppBar` wrapper — nav icon + action icon, or action icon only |
| `JetpackTopAppBarWithAvatar` | Same, with a circular Coil avatar in place of the action icon |
| `JetpackActionBar` | Left-aligned title with a `JetpackButton` text action instead of an icon |

### Navigation — `Navigation.kt`

| Component | Purpose |
|---|---|
| `JetpackNavigationSuiteScaffold` | Adaptive shell — bottom bar (compact), rail (medium), drawer (expanded) — around `NavigationSuiteScaffold` |
| `JetpackNavigationBar` / `JetpackNavigationBarItem` | Bottom bar and its items |
| `JetpackNavigationRail` / `JetpackNavigationRailItem` | Side rail and its items, with an optional `header` slot |

### Tabs — `Tabs.kt`

| Component | Purpose |
|---|---|
| `JetpackTab` / `JetpackTabRow` | `Tab`/`SecondaryTabRow` wrappers with custom top padding for label alignment |

### Selection — `Chip.kt`, `Tag.kt`, `IconButton.kt`, `ToggleButton.kt`

| Component | Purpose |
|---|---|
| `JetpackFilterChip` | Circular `FilterChip`, checkmark leading icon when selected |
| `JetpackTag` | `TextButton`-based follow/unfollow pill — solid when followed, translucent otherwise |
| `JetpackIconToggleButton` | `FilledIconToggleButton` wrapper; `primaryContainer` when checked |
| `JetpackToggleOptions` (+ `ToggleOption` data class) | Segmented-button row for 2-4 mutually exclusive options, animated background, 56.dp height |

### Feedback — `LoadingWheel.kt`

| Component | Purpose |
|---|---|
| `JetpackLoadingWheel` | Custom-drawn 12-line rotating loader, 48.dp default |
| `JetpackOverlayLoadingWheel` | `JetpackLoadingWheel` inside an elevated, 83%-opacity `Surface` |

> [!NOTE]
> The app's actual loading UI doesn't use either of these — `StatefulComposable` renders Material 3's
> own `ContainedLoadingIndicator` directly. See [State Management](state-management.md#the-uistatet-wrapper).

### FAB — `Fab.kt`

| Component | Purpose |
|---|---|
| `JetpackExtendedFab` | `ExtendedFloatingActionButton` wrapper — one `@StringRes` supplies both the label and the icon's content description |

### Layout & media — `Divider.kt`, `SwipeToDismiss.kt`, `DynamicAsyncImage.kt`, `Background.kt`

| Component | Purpose |
|---|---|
| `DividerWithText` | `HorizontalDivider` with centered text (e.g. "OR" between sign-in methods) |
| `SwipeToDismiss` | End-to-start-only `SwipeToDismissBox`, delete icon + `errorContainer` background |
| `DynamicAsyncImage` | Coil `AsyncImage` wrapper — shows `JetpackLoadingWheel` while loading, placeholder in Compose preview mode |
| `AppBackground` | Theme-driven `Surface`, resets `LocalAbsoluteTonalElevation` for nested surfaces |
| `AppGradientBackground` | Dual-gradient `Surface`, angled 11.06°, colors from `LocalGradientColors` |

**Wired into a real screen today:** `JetpackButton`/`JetpackOutlinedButton`/`JetpackTextButton`,
`JetpackTextField`/`JetpackPasswordField`, `JetpackTopAppBarWithAvatar`, `JetpackActionBar`,
`JetpackToggleOptions`, `JetpackExtendedFab`, `JetpackNavigationSuiteScaffold`, `DividerWithText`,
`SwipeToDismiss`, `AppBackground`, `AppGradientBackground`.

**Real, but not yet used by any screen:** `JetpackTopAppBar`, `JetpackNavigationBar(Item)`,
`JetpackNavigationRail(Item)`, `JetpackTab(Row)`, `JetpackFilterChip`, `JetpackTag`,
`JetpackIconToggleButton`, `JetpackLoadingWheel`, `JetpackOverlayLoadingWheel`, `DynamicAsyncImage`.
Still real, still lint-enforced if you use their Material equivalent instead — just not wired into a
screen in this template yet.

## Real usage

```kotlin
// feature/auth/src/main/kotlin/dev/atick/feature/auth/ui/signin/SignInScreen.kt
JetpackTextField(
    value = screenData.email.value,
    errorMessage = screenData.email.errorMessage,
    onValueChange = onEmailChange,
    label = { Text(stringResource(R.string.feature_auth_email)) },
    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
    leadingIcon = {
        Icon(imageVector = Icons.Default.Email, contentDescription = ...)
    },
)

JetpackButton(
    onClick = { focusManager.clearFocus(); onSignInClick.invoke() },
    modifier = Modifier.fillMaxWidth(),
    text = { Text(stringResource(R.string.feature_auth_sign_in)) },
)

DividerWithText(text = R.string.feature_auth_or, modifier = Modifier.padding(vertical = 16.dp))
```

```kotlin
// feature/home/src/main/kotlin/dev/atick/feature/home/ui/home/HomeScreen.kt
SwipeToDismiss(onDelete = { onDeleteJetpack(jetpack) }) {
    ListItem(
        onClick = { onJetpackCLick(jetpack.id) },
        leadingContent = { Icon(Icons.Default.RocketLaunch, contentDescription = jetpack.name) },
        overlineContent = { Text(jetpack.formattedDate) },
        content = { Text(jetpack.name) },
    )
}
```

```kotlin
// app/src/main/kotlin/dev/atick/compose/ui/JetpackApp.kt
AppGradientBackground(
    modifier = modifier,
    gradientColors = if (shouldShowGradientBackground) LocalGradientColors.current else GradientColors(),
) {
    JetpackNavigationSuiteScaffold(
        navigationSuiteItems = { navigationItems(currentTopLevelDestination, navigator::navigate) },
        windowAdaptiveInfo = windowAdaptiveInfo,
    ) {
        Scaffold(
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
            // ...
            JetpackTopAppBarWithAvatar(
                titleRes = currentTopLevelDestination.titleTextId,
                avatarUri = appState.userProfilePictureUri,
                avatarContentDescription = stringResource(id = R.string.settings),
                onAvatarClick = { showSettingsDialog = true },
            )
        }
    }
}
```

```kotlin
// feature/settings/src/main/kotlin/dev/atick/feature/settings/ui/SettingsDialog.kt
JetpackToggleOptions(
    options = getLanguageOptions(),
    selectedIndex = languageSelectedIndex,
    onSelectionChange = {
        languageSelectedIndex = it
        onChangeLanguage(Language.entries[it])
    },
)
```

## Theming

`JetpackTheme` (`core/ui/src/main/kotlin/dev/atick/core/ui/theme/Theme.kt:106-154`) wraps Material 3's
**`MaterialExpressiveTheme`**, not the plain `MaterialTheme`:

```kotlin
@Composable
fun JetpackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    disableDynamicTheming: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        !disableDynamicTheming && supportsDynamicTheming() -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> if (darkTheme) DarkDefaultColorScheme else LightDefaultColorScheme
    }
    // ...
    CompositionLocalProvider(
        LocalGradientColors provides gradientColors,
        LocalBackgroundTheme provides defaultBackgroundTheme,
        LocalTintTheme provides tintTheme,
    ) {
        MaterialExpressiveTheme(colorScheme = colorScheme, typography = JetpackTypography, content = content)
    }
}
```

`supportsDynamicTheming()` is `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S` (Android 12+). When it's
false, or `disableDynamicTheming = true`, the theme falls back to `DarkDefaultColorScheme`/
`LightDefaultColorScheme` — hand-authored `darkColorScheme`/`lightColorScheme` instances in the same
file, built from named tokens (`Purple40`, `Orange80`, …) defined in the sibling `Color.kt`.

Three composition locals ride alongside the Material color scheme, each with its own fallback when
dynamic theming is off: `LocalGradientColors` (`Gradient.kt`), `LocalBackgroundTheme` (`Background.kt`),
`LocalTintTheme` (`Tint.kt`). `AppGradientBackground` and `DynamicAsyncImage`'s tint both read these
directly rather than deriving from `colorScheme`, which is how the app renders a decorative gradient
that dynamic theming would otherwise flatten to a single surface color.

## Enforcement

`DesignSystemDetector` (`lint/src/main/kotlin/dev/atick/lint/designsystem/DesignSystemDetector.kt`) is
an ERROR-severity lint check, published to every module that depends on `core/ui` via
`lintPublish(projects.lint)` (`core/ui/build.gradle.kts:78`) — no per-module wiring needed.

It flags a call by where it *resolves*, not by name (`UCallExpression.resolvePackageName()`), so a
same-named `Tab` or `Icon` from another library isn't misflagged. Only calls resolving to
`androidx.compose.material3` and matching an entry in `DesignSystemDetector.METHOD_NAMES` trigger it —
`Button` → `JetpackButton`, `TextField`/`OutlinedTextField` → `JetpackTextField`, all five
`*TopAppBar` variants → `JetpackTopAppBar`, `FilterChip`/`ElevatedFilterChip` → `JetpackFilterChip`,
`NavigationBar(Item)`/`NavigationRail(Item)` → their `Jetpack*` equivalents, `TabRow`/`Tab` →
`JetpackTabRow`/`JetpackTab`, the four icon-toggle-button variants → `JetpackIconToggleButton`, and
`ExtendedFloatingActionButton` → `JetpackExtendedFab`. Calls inside
`dev.atick.core.ui.components` itself — the wrappers' own definitions — are exempt, since they must
call the Material composables they wrap.

## Further reading

- [State Management](state-management.md) — `StatefulComposable`, the loading/error UI that wraps every screen's content
- [Architecture](architecture.md) — where `core/ui` sits relative to `feature`/`data`
- [Guide](guide.md) — building a screen from these components end to end
- [Core UI Module](../core/ui/README.md) — the rest of `core/ui`: utilities, previews, testing helpers
