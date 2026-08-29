# Jetpack Android Starter - AI Agent Instructions

This file provides AI coding agents with project-specific instructions, conventions, and boundaries for working with this Android codebase. It is deliberately short — the full rationale for every pattern below lives in `docs/`, linked inline. Read the linked page before touching that part of the codebase; do not treat the snippets here as the complete picture.

## Essential Commands

### Build & Run
```bash
# Build debug variant
./gradlew assembleDebug

# Build and install on connected device
./gradlew installDebug

# Clean build artifacts
./gradlew clean

# Build release (requires keystore.properties)
./gradlew assembleRelease
```

### Code Quality (ALWAYS RUN BEFORE COMMITTING)
```bash
# Auto-format all code with ktlint (Spotless is an init script, not a plugin — the
# --init-script flag is required or the task will not be found; see docs/build-and-tooling.md)
./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache

# Check formatting
./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache

# Run all checks
./gradlew check
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run tests for specific module
./gradlew :feature:home:test

# Run instrumentation tests
./gradlew connectedAndroidTest
```

### Documentation
```bash
# Generate API documentation with Dokka
./gradlew dokkaGeneratePublicationHtml
# Output: build/dokka/html/
```

### Firebase Setup
```bash
# Get SHA-1 fingerprint for Firebase console
./gradlew signingReport
```

### Build guardrails
```bash
# Regenerate the Dependency Guard baseline after an intentional dependency change
./gradlew :app:dependencyGuardBaseline

# Regenerate the APK badging golden file after an intentional manifest/permission change
./gradlew :app:updateReleaseBadging
```

## Project Context

### Tech Stack Overview
- **Language**: Kotlin 2.4.10 with coroutines & Flow
- **UI**: Jetpack Compose with Material3 Expressive (declarative UI)
- **Architecture**: Two-layer MVVM (UI + Data, intentionally no Domain layer)
- **DI**: Dagger Hilt (compile-time injection)
- **Local Storage**: Room (SQL) + DataStore (key-value)
- **Networking**: Retrofit + OkHttp + Kotlinx Serialization
- **Backend**: Firebase (Auth, Firestore, Crashlytics, Analytics)
- **Background Work**: WorkManager, network-constrained two-way sync
- **Navigation**: Jetpack Navigation 3 (`NavKey`, `Navigator`, multi-back-stack) — see [Navigation](docs/navigation.md)
- **Build**: Gradle 9.6.1, AGP 9.3.1, Java 21

### Architecture Pattern
**Two-Layer Architecture** (simplified from Android's three-layer approach):
1. **UI Layer**: `feature/*` modules with Composables + ViewModels (MVVM)
2. **Data Layer**: `data/` module with Repositories + Data Sources

**Why no Domain layer?** Intentionally omitted to reduce complexity. Add it only when you have complex business logic or need to share logic between multiple ViewModels. Full rationale: [Architecture](docs/architecture.md).

### State Management Pattern
Every screen follows the `UiState<T>` pattern defined in `core:ui`:

- `UiState<T>`: wraps `data`, `loading`, `error: OneTimeEvent<Throwable?>`
- `updateState {}`: synchronous state updates
- `updateStateWith {}` / `updateWith {}`: async operations with automatic loading/error handling, reentrancy-guarded (a call while already loading is dropped)
- `StatefulComposable`: renders the loading/error/content UI for a `UiState`

```kotlin
// feature/home/.../HomeViewModel.kt
private val _homeUiState = MutableStateFlow(UiState(HomeScreenData()))
val homeUiState = _homeUiState
    .onStart { getJetpacks() }
    .stateInDelayed(UiState(HomeScreenData()), viewModelScope)

fun deleteJetpack(jetpack: Jetpack) {
    _homeUiState.updateWith { homeRepository.markJetpackAsDeleted(jetpack) }
}
```

Full pattern, including context parameters and `OneTimeEvent`: [State Management](docs/state-management.md).

### Navigation Pattern
Jetpack Navigation 3 — type-safe `NavKey`s, no string routes, no `NavController`/`NavHost`:

```kotlin
// feature/home/.../HomeNavigation.kt
@Serializable
data class ItemNavKey(val itemId: String?) : NavKey

fun Navigator.navigateToItem(itemId: String?) = navigate(ItemNavKey(itemId))

fun EntryProviderScope<NavKey>.homeEntries(navigator: Navigator, ...) {
    entry<ItemNavKey> { key -> ItemScreen(itemId = key.itemId, ...) }
}
```

Multi-back-stack state, `ListDetailSceneStrategy`, and assisted-inject ViewModels for keyed
arguments: [Navigation](docs/navigation.md).

### Data Flow & Dependency Injection
Offline-first: the UI observes a Room `Flow`, a `SyncManager`/WorkManager worker pushes local
changes and pulls remote ones in the background, and the local database is the single source of
truth. Repositories are bound with Hilt `@Binds`; data sources use `@IoDispatcher` and
`suspendRunCatching` to convert exceptions to `Result`. Full pattern, including the sync
contract and `@Binds` vs `@Provides`: [Data](docs/data.md).

### Module Dependencies (Respect These Boundaries)
```
feature/* → data → core:* → firebase:*
                  ↓
                sync
```

**Rules**:
- Features depend on data + core:ui only
- Data depends on core:* + firebase:*
- Core modules are independent (except core:ui can use core:android)
- Firebase modules are independent

## Conventions & Patterns

### File Naming
- **ViewModels**: `{Feature}ViewModel.kt`, co-located with its screen (e.g. `ui/home/HomeViewModel.kt`)
- **Repositories**: `{Feature}Repository.kt` + `{Feature}RepositoryImpl.kt`
- **Data Sources**: `{Type}DataSource.kt` (e.g., `NetworkDataSource.kt`, `LocalDataSource.kt`)
- **Screen Data**: `{Feature}ScreenData.kt` (immutable state, `@Immutable`)
- **Nav keys**: `{Feature}NavKey` (e.g., `@Serializable data class ItemNavKey(...)  : NavKey`)
- **Composables**: One `internal fun {Name}Screen(...)` per destination — no separate stateful/stateless `Route` wrapper

### Code Organization
- **Screen + ViewModel**: `feature/{name}/ui/{screen}/{Name}Screen.kt` + `{Name}ViewModel.kt`
- **Navigation**: `feature/{name}/navigation/{Name}Navigation.kt`
- **Repositories**: `data/repository/{name}/{Name}Repository(Impl).kt`
- **Models**: Network DTOs in `core:network/model/`, domain models in `data/model/{name}/`, Room entities in `core:room/model/`

### Threading & Coroutines
- Use `@IoDispatcher` for IO operations (network, database, file I/O)
- Use `@MainDispatcher` for UI operations
- Use `@DefaultDispatcher` for CPU-intensive work
- Wrap IO operations with `withContext(ioDispatcher)`
- Use `suspendRunCatching` for error handling in repositories

### Error Handling
- Repositories return `Result<T>` for one-time operations
- Repositories return `Flow<T>` for observable data
- Use `suspendRunCatching` to wrap suspend functions and convert exceptions to Results
- UI layer handles errors via `UiState.error: OneTimeEvent<Throwable?>`

### Kotlin Compiler Features
- **Context parameters** — on by default since Kotlin 2.4, no compiler flag needed: `updateStateWith` and `updateWith` use them to access `viewModelScope` without it being passed explicitly
- **Material3 Expressive** theming (`MaterialExpressiveTheme`), experimental APIs opted-in globally
- **RequiresOptIn** enabled

## Known Gotchas & Special Notes

### Build
- **Spotless task discovery**: applied via an init script, not a plugin — tasks are invisible in `./gradlew tasks` but still exist and run; always pass `--init-script gradle/init.gradle.kts`
- **Custom APK output filename**: not implemented on AGP 9 — `androidComponents.onVariants` in `app/build.gradle.kts` is currently a no-op placeholder (tracked as GitHub Issue #579); this is unrelated to APK badging, which does work (see below)
- **Dependency Guard** (`:app:dependencyGuard`) and **APK badging** (`:app:checkReleaseBadging`) run in CI on every PR — a dependency or manifest/permission change that isn't accompanied by a baseline/golden-file update will fail the build
- **Configuration cache**: enabled; may be discarded by `OssLicensesTask` (harmless)
- **JVM heap**: 8GB configured in `gradle.properties`

### Isolated Projects
Not enabled by default yet, but the build is verified against it:

```bash
./gradlew assembleDebug -Dorg.gradle.unsafe.isolated-projects=true
```

The convention plugins and `app/build.gradle.kts` are IP-clean — they resolve root-relative
paths through `isolated.rootProject.projectDirectory` rather than `rootProject.file(...)`.

Two violations remain, both from the third-party `secrets-gradle-plugin` reading
`secrets.defaults.properties` off the root project. They surface on `:core:network` and
`:firebase:auth`, the only two modules that apply it, and cannot be fixed from this repo.
Isolated Projects should not be turned on by default until that plugin is fixed or dropped.

### Resource Prefixes
Every library module sets `resourcePrefix` derived from its Gradle path, so a resource in
`:feature:home` must be named `feature_home_*`. Library resources share one namespace after
merging, so without the prefix two modules can define the same name and the winner depends on
merge order. Lint reports any resource that does not match.

### Convention Plugins
Located in `build-logic/convention/`. Do NOT modify these unless you understand the full impact —
seven plugins (`dev.atick.application`, `dev.atick.library`, `dev.atick.ui.library`, `dev.atick.test`,
`dev.atick.dagger.hilt`, `dev.atick.firebase`, `dev.atick.dokka`), documented in
[Build & Tooling](docs/build-and-tooling.md).

### Version Management
All dependencies in `gradle/libs.versions.toml` (Gradle Version Catalog). Use `libs.{name}` in build
files; never a hardcoded version string.

### Release Builds
- Requires `keystore.properties` in project root (NOT committed to git)
- Keystore file should be in `app/` directory
- Without `keystore.properties`, a release build silently falls back to the **debug signing key** —
  the output is un-shippable and un-upgradable; see the `☠️ CAUTION` in
  [Getting Started](docs/getting-started.md#release-build-setup)
- Never commit keystore files or credentials

### Firebase
- Debug build has template `google-services.json` (features won't work until configured)
- Requires Firebase project setup with package name `dev.atick.compose`
- See [Firebase](docs/firebase.md) for detailed setup

### LeakCanary
- Enabled in debug builds by default
- Comment out in `app/build.gradle.kts` to disable: `debugImplementation(libs.leakcanary.android)`

## Boundaries & Guidelines

### ALWAYS DO
✅ Run `./gradlew spotlessApply --init-script gradle/init.gradle.kts` before any commit
✅ Use existing state management patterns (`UiState<T>`, `updateStateWith`)
✅ Use type-safe Navigation 3 `NavKey`s
✅ Use Hilt for dependency injection (`@HiltViewModel`, `@Inject`)
✅ Use `suspendRunCatching` for error handling in repositories
✅ Use dispatcher qualifiers (`@IoDispatcher`, `@MainDispatcher`)
✅ Return `Flow` for observable data, `Result<T>` for one-time operations
✅ Follow offline-first pattern (local database as source of truth)
✅ Respect module boundaries (features → data → core)
✅ Add unit tests for new ViewModels/repositories using `core:testing`'s `MainDispatcherRule` and fakes

### ASK FIRST
⚠️ Adding new Gradle dependencies
⚠️ Modifying convention plugins in `build-logic/`
⚠️ Changing AGP, Kotlin, or Compose versions
⚠️ Adding new modules
⚠️ Modifying Firebase configuration
⚠️ Changing ProGuard rules
⚠️ Adding domain layer (currently intentionally omitted)
⚠️ Modifying Spotless configuration
⚠️ Changing min/target SDK versions

### NEVER DO
❌ Commit without running `spotlessApply`
❌ Commit keystore files or `keystore.properties`
❌ Use string-based navigation (always use `NavKey`)
❌ Directly access `viewModelScope` in state updates (use `updateStateWith`, which reaches it via context parameters)
❌ Make blocking IO calls on main thread
❌ Use `GlobalScope` or unstructured concurrency
❌ Bypass Hilt and manually create ViewModels or repositories
❌ Mix UI logic in ViewModels (keep ViewModels UI-agnostic)
❌ Ignore module boundaries (e.g., feature modules depending on other features)
❌ Add dependencies without using version catalog (`gradle/libs.versions.toml`)
❌ Force push to main/master branch
❌ Modify existing database entities without migration strategy

## Adding a New Feature

The canonical walkthrough — real code, every step — is [Guide](docs/guide.md), built around the
`home`/`Jetpack` feature (the only one touching every layer). Follow it rather than improvising the
module/file layout; it stays current where a second copy here would drift.

## Additional Resources

- **Comprehensive documentation**: `docs/` directory — [Architecture](docs/architecture.md),
  [State Management](docs/state-management.md), [Navigation](docs/navigation.md),
  [Components](docs/components.md), [Data](docs/data.md), [Build & Tooling](docs/build-and-tooling.md),
  [Firebase](docs/firebase.md), [Troubleshooting](docs/troubleshooting.md)
- **Live documentation**: https://atick.dev/Jetpack-Android-Starter
- **CI/CD workflows**: `.github/workflows/` (`ci.yml`, `cd.yml`, `docs.yml`)

## Questions or Issues?

If you encounter issues or have questions:
1. Check [Troubleshooting](docs/troubleshooting.md) for common problems
2. Review relevant documentation in `docs/`
3. Check GitHub issues for known problems
