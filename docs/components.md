# Components

The shared components in `:core:ui` provide consistent buttons, inputs, navigation, feedback,
and themed backgrounds. Start with the catalog below, use slots and callbacks to supply content
and behavior, and change the shared theme when a visual choice should apply across screens.

For a destination's ViewModel/state wiring, follow the [feature guide](guide.md). This page covers
component usage, branding, previews, accessibility, localization, and adding reusable UI.

## Catalog

Components live in `core/ui/src/main/kotlin/dev/atick/core/ui/components/`. The file names below
identify their implementations; the [Core UI API](api/core/ui/index.html) has signatures and KDoc.

### Buttons — `Button.kt`

| Component | Purpose |
|---|---|
| `JetpackButton` | Filled button, high emphasis. Container color is `colorScheme.onBackground` |
| `JetpackOutlinedButton` | Outlined button, medium emphasis. 1.dp border in `colorScheme.outline` when enabled |
| `JetpackTextButton` | No background, low emphasis |

Each has two overloads: `text: @Composable () -> Unit` plus an optional `leadingIcon`, or a
bare `content: @Composable RowScope.() -> Unit` slot for anything else.

### Text input — `TextField.kt`

| Component | Purpose |
|---|---|
| `JetpackTextField` | `OutlinedTextField` wrapper, 50%-rounded corners, error-colored border and animated error text when `errorMessage != null` |
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
| `JetpackNavigationSuiteScaffold` | Adaptive shell around `NavigationSuiteScaffold`; delegates layout selection to Material defaults |
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
| `JetpackToggleOptions` (+ `ToggleOption` data class) | Exclusive-selection row with animated background; option count is not enforced |

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
| `DynamicAsyncImage` | Coil painter rendered with `Image`; loading wheel, error/preview placeholder, theme tint |
| `AppBackground` | Theme-driven `Surface`, resets `LocalAbsoluteTonalElevation` for nested surfaces |
| `AppGradientBackground` | Dual-gradient `Surface`, angled 11.06°, colors from `LocalGradientColors` |

Selection state belongs to the caller: pass `selected`/`checked` and update it through the
callback. `JetpackTag`'s follow appearance does not persist a preference. Keep related state in the
screen's existing state model; use local remembered state for purely local UI selection.

The navigation suite calls `NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo`; it does
not implement a compact/medium/expanded mapping of its own. Do not assume an expanded window
selects a drawer. See [Navigation](navigation.md) for the app's adaptive content and back stacks.

## Real usage

The following are shortened excerpts: imports, surrounding state, and unrelated layout are omitted.
The source paths identify the complete screens. Resolve descriptions from string resources;
`...` marks omitted code rather than a value to paste.

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

`JetpackTheme` in `core/ui/.../theme/Theme.kt` supplies `MaterialExpressiveTheme`, the shared
`JetpackTypography`, and the background, gradient and tint locals.

### Brand colors and typography

1. Edit the named color tokens in `theme/Color.kt` and their roles in both
   `LightDefaultColorScheme` and `DarkDefaultColorScheme` in `theme/Theme.kt`.
2. Check paired roles together (`primary`/`onPrimary`, `surface`/`onSurface`, and container/on-container
   pairs). A new primary color alone will not recolor every wrapper: `JetpackButton`, for example,
   uses `onBackground` as its container. It has no public `colors` parameter.
3. Edit `JetpackTypography` in `theme/Type.kt` for shared text roles. It is internal to `:core:ui`;
   feature code reads `MaterialTheme.typography`, rather than importing that declaration.
4. Preview both themes, disabled states, text over images, and the actual button foreground/background
   combinations. Check contrast after changing either role; choosing theme tokens does not establish
   accessibility by itself.

For example, these illustrative arguments replace the matching roles inside a `lightColorScheme`
call in `Theme.kt` (import `androidx.compose.ui.graphics.Color`); choose and check a corresponding
pair for `darkColorScheme` too:

```kotlin
primary = Color(0xFF315DA8),
onPrimary = Color.White,
```

For a local text treatment, use a role or copy it at the point of use. This excerpt belongs inside
an existing composable (imports: Material 3 `Text`/`MaterialTheme`, `androidx.compose.ui.unit.sp`):

```kotlin
Text(
    text = title,
    style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 0.sp),
    color = MaterialTheme.colorScheme.onSurface,
)
```

### Dynamic color

On Android 12+, dynamic color takes precedence unless `disableDynamicTheming` is true. On older
versions the two default schemes are used. To inspect your brand colors consistently, wrap a
preview or local component in `JetpackTheme(disableDynamicTheming = true)`.

The running app maps the settings preference `useDynamicColor` to the theme's inverse
`disableDynamicTheming` argument. Use the existing settings UI to check both choices; changing
only the static palette will not change a device using wallpaper colors.

### Backgrounds, gradients and image tint

`AppBackground` reads `LocalBackgroundTheme`. `AppGradientBackground` accepts `gradientColors`,
whose default comes from `LocalGradientColors`. A local override can use
`LocalGradientColors.current.copy(top = MaterialTheme.colorScheme.primaryContainer)`; pass that
value to `AppGradientBackground(gradientColors = colors) { /* content */ }` inside `JetpackTheme`.
These are `GradientColors` values supplied by `LocalGradientColors`.

Dynamic color supplies an empty gradient over an elevated surface and a primary-colored
`LocalTintTheme`. The static theme supplies a gradient and no icon tint. To show an untinted photo
through `DynamicAsyncImage`, place it inside
`CompositionLocalProvider(LocalTintTheme provides TintTheme()) { /* image */ }` (runtime
`CompositionLocalProvider` and theme `LocalTintTheme`/`TintTheme` imports).

## Images and loading feedback

`DynamicAsyncImage(imageUrl, contentDescription, modifier, placeholder)` uses
`rememberAsyncImagePainter` with `ContentScale.Crop`. It renders a loading wheel during loading and
uses its placeholder on error or in inspection mode. It applies the current tint to the whole
image, so check photos as well as icons. A null description becomes the string `"null"` for its
loading wheel in the current implementation; inspect that announcement before using the wrapper
for decorative images. There is no retry callback or request-builder parameter on this wrapper.

The app's `ImageLoaderFactory` returns the injected Coil loader from `core/network/.../CoilModule.kt`.
That loader adds SVG support, uses the shared HTTP call factory, and sets `respectCacheHeaders(false)`.
Neither the wrapper nor that module enables crossfade. Do not infer freshness or offline guarantees
from the loader's existence. Request-specific sizing, transformations or retry UI require an
explicit extension or a suitable Coil API; keep such additions separate from theme configuration.

`StatefulComposable` renders `ContainedLoadingIndicator`; the two loading-wheel components are
alternatives for custom layouts. `JetpackOverlayLoadingWheel` draws a surface around a loader;
it does not establish a modal interaction barrier. Disable or guard actions explicitly when an
operation must prevent resubmission. See [state contracts](state-management.md) for dropped updates
while loading, errors, and snackbar event delivery.

`SwipeToDismiss` supports end-to-start dismissal. Its effect invokes the latest `onDelete` callback
when the remembered state enters the dismissed value; ordinary recomposition does not repeat the
call. This is not a durable exactly-once guarantee. Keep list identity stable, remove deleted rows
through the observed data, and provide an accessible alternative delete action when extending the
screen. The existing home flow has no undo action.

## Previews

Use the shared annotations from `dev.atick.core.ui.utils` with a parameterless composable that
supplies sample data and callbacks. Preview a presentational child rather than a destination that
requires an injected ViewModel. This does not require adding Route wrappers to the screen pattern.

`@PreviewDevices` defines phone, landscape, foldable and tablet configurations. `@PreviewThemes`
defines light and dark configurations. Applying both adds their independent previews; it does not
create eight device/theme combinations. For a particular combination, declare an explicit
`@Preview(device = "spec:width=1280dp,height=800dp,dpi=480", uiMode = Configuration.UI_MODE_NIGHT_YES)`.
See Android's [multipreview documentation](https://developer.android.com/develop/ui/compose/tooling/previews#preview-multipreview).

The extension example below includes a complete preview. Add explicit previews with `locale = "ar"`
and `fontScale = 1.5f` for language and larger-text checks. Wrap in `JetpackTheme`, use deterministic
sample data, and avoid network/database work. When rendering fails, inspect the preview exception,
module build, resource availability, and any ViewModel or unavailable platform dependency.
Compose formatting rules are separate from rendering failures; follow nearby previews and the
[formatting workflow](build-and-tooling.md).
Previews help inspect layout; exercise interactions and assistive technology in a running app too.

## Accessibility

- Give icon-only actions a localized action description. Use `contentDescription = null` for an
  icon whose adjacent text already conveys the same meaning, or for a decorative image.
- Keep touch targets at least 48.dp and check the focus order. Do not shrink targets to match small
  artwork. Give grouped custom choices selection semantics and an appropriate role; a visual
  background change alone does not expose selection to accessibility services.
- Check TalkBack output for names, roles, selected/disabled state, and validation messages. Use
  Compose `error` semantics for errors and an appropriate `liveRegion` for changing status text
  when needed; avoid repeated announcements on every recomposition. These are extension guidance,
  not guarantees that every current wrapper announces every state.
- Check loading feedback while content remains visible, and check focus after an error or deletion.
  Ensure important actions can be reached without a swipe gesture.
- Check light/dark and dynamic/static contrast, large system fonts, long translations, and RTL.
  Avoid fixed-height text containers that clip enlarged labels. Use Accessibility Scanner and
  manual TalkBack checks on a device/emulator; a successful preview does not prove accessibility.

## Localization

The current picker offers English and Arabic. To add another language, update the whole path:

1. Add translated `values-<language>/strings.xml` files beside the relevant modules' default strings.
   Keep library prefixes, such as `feature_settings_*` or `core_ui_*`; retain default values for
   fallback and preserve formatting placeholders. Use `stringResource` for UI labels.
2. Add a code to `Language` in `data/.../model/settings/Settings.kt`, for example `FRENCH("fr")`.
3. Add the new branch in `getLanguageOptions()` in `SettingsDialog.kt`, with a prefixed string
   resource for the language name and an icon. Options follow `Language.entries`, so keep their
   ordering consistent with the selected-index mapping.
4. Extend `SettingsViewModel.getPreferredLanguage()` to map `"fr"` to the new entry. Currently only
   `"ar"` is recognized explicitly; all other locales map to English. Adding resources alone would
   leave the picker showing English for the new locale.
5. Keep `updateLanguagePreference` calling `setLanguagePreference(language.code)`. That delegates
   to `AppCompatDelegate.setApplicationLocales`; `getPreferredLocale()` reads application locales
   and falls back to the system locale. Settings overlays this language onto repository settings;
   it is not a DataStore language field.
6. Check the app's `generateLocaleConfig = true` and `res/resources.properties`
   (`unqualifiedResLocale=en-US`). Locale configuration is generated from resources; do not add a
   competing handwritten config. The manifest enables RTL and AppCompat's `autoStoreLocales`
   metadata. Preserve that wiring when adopting the template.
7. Build, choose the language in the app and system app-language settings where available, restart,
   and check picker selection and persistence. Inspect Arabic/RTL layout, directional icons,
   start/end padding, long labels and formatted values. Preview locale alone does not test storage
   or activity recreation. Test on pre-33 and 33+ Android when changing locale integration.

## Text, validation and permissions

### UiText and form errors

`UiText` in `dev.atick.core.ui.utils` carries either `DynamicString(value)` or
`StringResource(resId, vararg args)`. Resolve it at the UI boundary with composable `asString()`,
or `asString(context)` outside composition. For example, an adopter can add
`<string name="feature_home_item_count">%1$d items</string>` to the home module and render
`Text(UiText.StringResource(R.string.feature_home_item_count, count).asString())`.
`R` must be that module's resource class. Use plurals for proper quantity-sensitive translations;
this string example demonstrates formatting arguments only.

`TextFieldData.errorMessage` is currently `String?`, not `UiText?`. An auth form passes its
`value`/`errorMessage` to `JetpackTextField` and sends changes back to the ViewModel.
The nullable string extensions in `dev.atick.core.extensions` are `isEmailValid()`,
`isPasswordValid()`, and `isValidFullName()`. This excerpt belongs in an auth state update,
with `invalidEmailMessage` a caller-supplied localized string:

```kotlin
copy(
    email = TextFieldData(
        value = email,
        errorMessage = if (email.isEmailValid()) null else invalidEmailMessage,
    ),
)
```

Use the existing sign-in/sign-up screen and ViewModel for the complete flow; their current validation
messages are hardcoded English strings. Localize those messages separately when adopting the form.
Password validation
requires 8–20 characters, at least one lowercase letter and digit. The full-name check splits on
spaces and tests letters; it is a template policy, not a universal definition of a valid name.
Revisit these rules for your audience and backend. A null error only means no error is stored;
it does not prove that validation ran or the backend will accept the input.

### Permission helpers

The Activity extensions are in `dev.atick.core.ui.extensions`; `getActivity()` is a separate
Context extension in `dev.atick.core.extensions` (`:core:android`). Register `permissionLauncher(onSuccess, onFailure)` during
Activity initialization, before the Activity is started. Launch it in response to the relevant
user action rather than registering a launcher from a click callback or recomposition.

The following complete illustrative Activity adds no manifest permission. It demonstrates the
existing helper using `POST_NOTIFICATIONS`, which the app already declares; keep the existing
`MainActivity` as your app entry point. Imports are shown because launcher timing matters here.

```kotlin
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import dev.atick.core.ui.extensions.permissionLauncher

class NotificationPermissionExampleActivity : ComponentActivity() {
    private lateinit var notifications: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notifications = permissionLauncher(
            onSuccess = { onNotificationsAvailable() },
            onFailure = { onNotificationsDeclined() },
        )
    }

    // Call from your notification opt-in UI after the Activity has been created.
    fun requestNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifications.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        } else {
            onNotificationsAvailable()
        }
    }

    private fun onNotificationsAvailable() { /* Continue your notification flow. */ }
    private fun onNotificationsDeclined() { /* Explain the unavailable feature in your UI. */ }
}
```

The higher-level `checkForPermissions(permissions, onSuccess)` requests missing permissions and
opens app settings on denial. If all permissions are already granted, it returns **without calling
`onSuccess`**. It also registers its launcher internally, so it is not a general-purpose helper to
call from arbitrary later click handlers. Prefer the registered launcher pattern above for a
user-driven flow. Keep denial, already-granted, unsupported-API, and returning-from-settings paths
explicit; do not rely on a success callback after returning from system settings.

## Add a shared component

Keep feature-specific UI next to its screen. Move UI into `:core:ui` when multiple features need
the same contract. Compose existing wrappers first; when adding a new Material wrapper, follow
neighboring component APIs and the [enforcement rules](#enforcement).

This complete illustrative addition belongs in `core/ui/.../components/RetryButton.kt`; it is not
an existing API. The caller owns loading state and the retry operation. `label` lets the caller
supply localized text, so this example adds no resource. If it supplied its own strings, their
names would need the `core_ui_` prefix.

```kotlin
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.atick.core.ui.components.JetpackOutlinedButton
import dev.atick.core.ui.theme.JetpackTheme
import dev.atick.core.ui.utils.PreviewThemes

/** Requests another attempt using caller-owned state and behavior. */
@Composable
fun RetryButton(
    label: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    JetpackOutlinedButton(
        onClick = onRetry,
        text = { Text(label) },
        modifier = modifier,
        enabled = enabled,
    )
}

@PreviewThemes
@Composable
private fun RetryButtonPreview() {
    JetpackTheme(disableDynamicTheming = true) {
        RetryButton(label = "Try again", onRetry = {})
    }
}
```

Use package `dev.atick.core.ui.components` when adding this file; imports are shown for a standalone
example. Keep shared components small and named by behavior. Add meaningful interaction/semantics
coverage when introducing behavior: callback on click, disabled state, and accessible label are
useful checks for an interactive extension. The existing `SwipeToDismissTest` demonstrates Compose
interaction testing in this module.

For local tabs, hoist the selected index; the caller renders the corresponding content. This
excerpt belongs in an existing composable with nonempty `titles`, `selectedIndex` in range, and an
`onSelect: (Int) -> Unit` callback (imports: core UI `JetpackTabRow`/`JetpackTab`, Material 3 `Text`):

```kotlin
JetpackTabRow(selectedTabIndex = selectedIndex) {
    titles.forEachIndexed { index, title ->
        JetpackTab(
            selected = selectedIndex == index,
            onClick = { onSelect(index) },
            text = { Text(title) },
        )
    }
}
```

## Performance checks

Profile before changing composition structure; use the [compiler reports and profiling workflow](build-and-tooling.md).
In lists, use stable item keys as `HomeScreen` does, so remembered state stays with its item when
rows move. For mixed row layouts, consider `contentType` for reuse; that is extension guidance,
not a claim that the current homogeneous list needs it. Move costly sorting/formatting out of
repeated item composition and measure the result. Avoid blanket rules to wrap every callback or
use `derivedStateOf` for every value.

`@Immutable` and stability configuration declare contracts to Compose; they do not freeze mutable
objects. Keep screen data consistent with those contracts and inspect compiler reports when
investigating skips/recomposition. For images, inspect request sizes, cropping, scrolling and
loading/error states with the actual loader. Test on a device with realistic content; a preview
is not a rendering-performance benchmark.

## Enforcement

`DesignSystemDetector` (`lint/src/main/kotlin/dev/atick/lint/designsystem/DesignSystemDetector.kt`) is
an ERROR-severity lint check, published to every module that depends on `core/ui` via
`lintPublish(projects.lint)` (`core/ui/build.gradle.kts`) — no per-module wiring needed.

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

When adding a wrapper for a Material API that should be standardized across features, update
`DesignSystemDetector.METHOD_NAMES` and the detector tests in `:lint`. Cover the mapped Material
call, an unrelated same-named API, and the wrappers' package exemption. Run `./gradlew :lint:test`.
A composite such as the illustrative retry button, built from existing wrappers, does not need a
new substitution merely because it is shared. Do not edit the detector just to make an example pass.

## Further reading

- [State Management](state-management.md) — `StatefulComposable`, the loading/error UI that wraps every screen's content
- [Architecture](architecture.md) — where `core/ui` sits relative to `feature`/`data`
- [Guide](guide.md) — building a screen from these components end to end
- [Core UI Module](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/core/ui/README.md) — the rest of `core/ui`: utilities, previews, testing helpers
