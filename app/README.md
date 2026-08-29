# Module :app

**Purpose:** The application module — wires every `feature`/`core`/`firebase` module together behind
`MainActivity` and `JetpackApp`, and owns app-level config (signing, versioning, ProGuard).

## Key APIs

| API | What it does |
|---|---|
| `MainActivity` | Splash screen, theme resolution, `POST_NOTIFICATIONS` request (Android 13+), edge-to-edge, hosts `JetpackApp` |
| `JetpackApp` / `rememberJetpackAppState` | Root composable; window size class, auth state, profile picture URI, network/crash-reporter access |

```kotlin
// app/src/main/kotlin/dev/atick/compose/MainActivity.kt
enableEdgeToEdge(
    statusBarStyle = SystemBarStyle.auto(
        lightScrim = android.graphics.Color.TRANSPARENT,
        darkScrim = android.graphics.Color.TRANSPARENT,
    ) { darkTheme },
    navigationBarStyle = SystemBarStyle.auto(lightScrim = lightScrim, darkScrim = darkScrim) { darkTheme },
)
```

```kotlin
// app/build.gradle.kts
val majorUpdateVersion = 1
val minorUpdateVersion = 3
val patchVersion = 0
val mVersionCode = majorUpdateVersion.times(10_000).plus(minorUpdateVersion.times(100)).plus(patchVersion)
```

## Gotchas

> [!IMPORTANT]
> `MainActivity` extends `AppCompatActivity`, not `ComponentActivity`, to support
> `setApplicationLocales` for backward-compatible per-app language selection — the manifest sets
> `android:theme="@style/Theme.AppCompat"` to prevent a crash from that combination.

- Theme resolution combines `isSystemInDarkTheme()` with `DarkThemeConfigPreferences`
  (`FOLLOW_SYSTEM`/`LIGHT`/`DARK`) collected via `repeatOnLifecycle(STARTED)`.
- A release build without `keystore.properties` silently falls back to the debug signing key — see
  [Getting Started](../docs/getting-started.md#release-build-setup) for the full trap.

## Related Documentation

- [Architecture](../docs/architecture.md) — the module graph `:app` sits at the top of
- [Navigation](../docs/navigation.md) — `JetpackApp`'s `NavDisplay`/`Navigator` wiring
- [Data Layer](../docs/data.md) — the repositories injected across feature ViewModels
