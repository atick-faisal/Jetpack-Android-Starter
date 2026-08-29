# Build & Tooling

How the project is built, kept in style, guarded against regressions, and shipped. For the module
graph and the two-layer design these tools build on top of, see [Architecture](architecture.md).

## Convention plugins

Every module applies build configuration through one of seven convention plugins in
`build-logic/convention/src/main/kotlin/`, registered as `dev.atick.*` Gradle plugin IDs and aliased
in `gradle/libs.versions.toml` (`jetpack-application`, `jetpack-library`, etc.):

| Plugin class | ID | Applies to | What it configures |
|---|---|---|---|
| `ApplicationConventionPlugin` | `dev.atick.application` | `app` | `com.android.application`, Compose compiler, `oss-licenses`, `kotlinx-serialization`, Dependency Guard, APK badging tasks, Kotlin/Compose/lint/test config |
| `LibraryConventionPlugin` | `dev.atick.library` | Non-UI modules (`data`, `sync`, `core/room`, ...) | `com.android.library`, `kotlinx-serialization`, resource prefixing, Kotlin/lint/test config |
| `UiLibraryConventionPlugin` | `dev.atick.ui.library` | Compose modules (`core/ui`, `feature/*`) | Everything `library` does, plus the Compose compiler |
| `TestConventionPlugin` | `dev.atick.test` | `benchmarks` | `com.android.test`, Gradle Managed Devices, Kotlin config |
| `DaggerHiltConventionPlugin` | `dev.atick.dagger.hilt` | Modules using Hilt DI | `com.google.dagger.hilt.android`, KSP |
| `FirebaseConventionPlugin` | `dev.atick.firebase` | `firebase/*` | `google-services`, Crashlytics |
| `DokkaConventionPlugin` | `dev.atick.dokka` | Every documented module | `org.jetbrains.dokka` |

Shared logic each plugin composes lives alongside them as top-level functions, not plugins
themselves: `configureKotlinAndroid`, `configureAndroidCompose`, `configureAndroidLint`,
`configureAndroidTest`, `configureResourcePrefix`, `configureBadgingTasks`,
`configureGradleManagedDevices`.

`gradle/libs.versions.toml` is the single source of truth for every dependency and plugin version in
the project — a new library or plugin gets a `[versions]` entry, a `[libraries]`/`[plugins]` alias,
then a reference in the consuming `build.gradle.kts`, never a hardcoded version string.

To add a new convention plugin: write the class in `build-logic/convention/src/main/kotlin/`,
register it in `build-logic/convention/build.gradle.kts`'s `gradlePlugin { plugins { ... } }` block,
then add a `jetpack-*` alias to `libs.versions.toml` so modules can apply it by ID.

### Compose compiler stability config

`compose_compiler_config.conf` (repo root) lists types the Compose compiler should treat as stable
even though it can't prove it — currently just `dev.atick.data.model.**`, the data layer's immutable
models:

```
// Only add types that really are immutable — an entry here is a promise the compiler trusts
// without checking.
dev.atick.data.model.**
```

Wired into every Compose module via `AndroidCompose.kt:88-90`
(`stabilityConfigurationFiles.add(...)`), so recomposition skipping works for model types the
compiler can't otherwise infer stability for across module boundaries.

## Code quality

### Spotless, via an init script

Spotless is applied from `gradle/init.gradle.kts`, a Gradle **init script**, not a `plugins {}` block
— it's injected into every subproject from `rootProject { subprojects { apply<SpotlessPlugin>() } }`
rather than declared per-module. The file's own comment is the reason on record:

```kotlin
// TODO: Verify Spotless task discoverability in Gradle 9.4.0 (Issue #580)
// Note: Spotless tasks may not appear in standard task listings but still execute correctly
```

> [!WARNING]
> Because Spotless is never applied through `plugins {}`, `./gradlew tasks` won't list
> `spotlessApply`/`spotlessCheck`. Both need the init script passed explicitly:
> `./gradlew spotlessCheck --init-script gradle/init.gradle.kts`.

| Target | Files | License header | Ruleset |
|---|---|---|---|
| `kotlin` | `**/*.kt` | `spotless/copyright.kt` | ktlint 1.6.0, `android=true`, custom rules `io.nlopez.compose.rules:ktlint:0.6.3` |
| `groovy` | `**/*.gradle` | `spotless/copyright.gradle` | — |
| `kts` | `**/*.kts` | `spotless/copyright.kts` | — |
| `xml` | `**/*.xml` | `spotless/copyright.xml` | — |

`./gradlew spotlessApply --init-script gradle/init.gradle.kts` applies fixes; the `.run/` configs
below run both with `--no-configuration-cache` in addition.

### Custom lint (`:lint`)

Two checks, implemented as a plain `java-library` module (not a convention-plugin consumer — lint
tooling targets an older JVM) and shipped to every consumer automatically:

| Check | File | Severity | Flags |
|---|---|---|---|
| `DesignSystemDetector` | `lint/.../designsystem/DesignSystemDetector.kt` | ERROR | Raw Material3 composables where `core/ui` provides a wrapper |
| `TestMethodNameDetector` | `lint/.../TestMethodNameDetector.kt` | WARNING | `@Test` methods named `test*` (the annotation already says it's a test) |

Both are registered in `JetpackIssueRegistry` and reach every module through
`lintPublish(projects.lint)` in `core/ui/build.gradle.kts:78` — a module using the design system gets
the checks without wiring anything up itself.

Run the check's own unit tests: `./gradlew :lint:test`.

## Build guardrails

### APK badging

`Badging.kt` registers three tasks per application variant, comparing `aapt2 dump badging` output
against a committed golden file so a permission, SDK-version, or manifest change is caught in review
instead of at release time:

| Task (release variant) | Does |
|---|---|
| `:app:generateReleaseBadging` | Dumps badging info from the built APK |
| `:app:checkReleaseBadging` | Diffs it against `app/release-badging.txt`, fails the build on drift |
| `:app:updateReleaseBadging` | Overwrites `app/release-badging.txt` with the new output |

CI runs `checkReleaseBadging` on every PR (`.github/workflows/ci.yml`); update the golden file locally
with `updateReleaseBadging` when a change is intentional.

### Dependency Guard

Fails the build when the release runtime classpath drifts from a committed baseline, so a new
transitive dependency has to be reviewed rather than arriving unnoticed:

```kotlin
// app/build.gradle.kts:171-174
// Regenerate with: ./gradlew :app:dependencyGuardBaseline
dependencyGuard {
    configuration("releaseRuntimeClasspath")
}
```

Baseline lives at `app/dependencies/releaseRuntimeClasspath.txt`. `./gradlew :app:dependencyGuard`
checks it (also run in CI); `./gradlew :app:dependencyGuardBaseline` regenerates it.

### Gradle Managed Devices

`GradleManagedDevices.kt` registers two virtual devices Gradle can boot and tear down itself, so
instrumented tests and baseline-profile generation don't depend on whatever's plugged in:

| Device | API | Image | Task prefix |
|---|---|---|---|
| Pixel 6 | 33 | `aosp` | `pixel6Api33` |
| Pixel 4 | 30 | `aosp-atd` (smaller, automated-test-device) | `pixel4Api30` |

Pixel 6's API 33 is required, not arbitrary — the baseline profile format changed and an older image
produces one the installer silently ignores. `dev.atick.test` (`:benchmarks` only) registers these
devices; standard AGP task naming applies, e.g.
`./gradlew :benchmarks:pixel6Api33BenchmarkReleaseAndroidTest`.

### Baseline profiles

`:benchmarks` (`BaselineProfileGenerator.kt`, `StartupBenchmark.kt`) generates and measures the
app's baseline profile, wired via `alias(libs.plugins.baselineprofile)` in `app/build.gradle.kts`:

```kotlin
// app/build.gradle.kts:161-167
baselineProfile {
    automaticGenerationDuringBuild = false
    dexLayoutOptimization = true
}
```

> [!IMPORTANT]
> `automaticGenerationDuringBuild = false` means the profile is **not** regenerated automatically —
> every build would otherwise wait for an emulator. Run `./gradlew :app:generateReleaseBaselineProfile`
> by hand whenever the app's startup path changes.

## Local dev

### `.run/` configurations

13 IntelliJ/Android Studio run configurations, each a thin wrapper over one Gradle task:

| Configuration | Task |
|---|---|
| Run Unit Tests | `test` |
| Run Instrumentation Tests | `connectedAndroidTest` |
| Run Custom Lint Tests | `:lint:test` |
| Check Convention Plugins | `:build-logic:convention:check` |
| Spotless Check | `spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache` |
| Spotless Apply | `spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache` |
| Check APK Badging | `:app:checkReleaseBadging` |
| Update APK Badging | `:app:updateReleaseBadging` |
| Check Dependency Guard | `:app:dependencyGuard` |
| Update Dependency Guard Baseline | `:app:dependencyGuardBaseline` |
| Generate Baseline Profile | `:app:generateReleaseBaselineProfile` |
| Generate Docs | `:dokkaGeneratePublicationHtml` |
| Signing Report | `signingReport` |

### Gradle build performance

Set in `gradle.properties`: `org.gradle.parallel=true`, `org.gradle.caching=true`,
`org.gradle.configuration-cache=true` (with `.parallel=true`), and
`org.gradle.jvmargs=-Xmx8g -XX:+UseParallelGC -XX:MaxMetaspaceSize=2g`.

Develocity build scans (`settings.gradle.kts:47-56`) publish only when a `CI` environment variable is
set, so local builds never prompt for scan publication.

## CI/CD

| Workflow | Trigger | Does |
|---|---|---|
| `ci.yml` (🚀 Build & Validate) | Pull request | `lint` job: Spotless check, `:build-logic:convention:check`. `build` job: Dependency Guard, unit tests, `:lint:test`, project build, Android Lint (uploaded to Code Scanning), APK badging check |
| `cd.yml` (🚀 Release APK) | Tag push `v*.*.*` | `lint` → `build` (assembles the signed release) → `github-release` and `play-store-release` (Fastlane) |
| `docs.yml` (📚 Documentation Deployment) | Push to `main` | Builds and deploys the MkDocs site |
| `claude.yml` (Claude Code) | Issue/PR comments and reviews | Claude Code GitHub Action automation — not part of the build pipeline |

`cd.yml` requires five repository secrets: `GOOGLE_SERVICES`, `KEYSTORE`, `KEYSTORE_PROPERTIES`,
`GOOGLE_WEB_CLIENT_ID`, `PLAY_STORE_JSON`.

## Release

`fastlane/Fastfile` defines three lanes under `platform :android`:

| Lane | Does |
|---|---|
| `test` | `gradle(task: "test")` |
| `beta` | `gradle(task: "assembleRelease")`, uploads to Crashlytics |
| `deploy` | `gradle(task: "bundleRelease")`, builds a changelog from git commits, `upload_to_play_store` |

A tag push (`v*.*.*`) drives the release end to end through `cd.yml`'s `play-store-release` job.
Fastlane's own setup and lane options are documented in the tool-generated
[`fastlane/README.md`](../fastlane/README.md) — not duplicated here.

## Further reading

- [Architecture](architecture.md) — the module graph these plugins build
- [Getting Started](getting-started.md) — first build on a fresh clone
- [Contributing](../CONTRIBUTING.md) — the comment and alert conventions this repo follows
