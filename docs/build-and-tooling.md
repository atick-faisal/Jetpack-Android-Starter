# Build & Tooling

How the project is built, kept in style, guarded against regressions, and shipped. For the module
graph and the two-layer design these tools build on top of, see [Architecture](architecture.md).

## Command reference

Run these commands from the repository root. Start with the JDK 21 and Android SDK setup in
[Getting Started](getting-started.md). Gradle downloads missing dependencies on the first run.

| Task | Command | Prerequisite / result |
|---|---|---|
| Build debug APK | `./gradlew assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` |
| Install debug app | `./gradlew installDebug` | Connected device or running emulator; launch the app afterward |
| Unit tests | `./gradlew test` | Module reports under `<module>/build/reports/tests/`; includes Robolectric tests |
| One module's unit tests | `./gradlew :feature:home:testDebugUnitTest` | HTML report in `feature/home/build/reports/tests/testDebugUnitTest/` |
| Device tests | `./gradlew connectedAndroidTest` | Device/emulator visible to `adb devices`; reports under `<module>/build/reports/androidTests/connected/` |
| Custom lint tests | `./gradlew :lint:test` | Report in `lint/build/reports/tests/test/` |
| Android lint | `./gradlew lint` | Reports under `<module>/build/reports/` |
| Convention plugin checks | `./gradlew :build-logic:convention:check` | Checks the included build's own code |
| Format / check format | `./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache` / `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache` | See [Spotless](#spotless-via-an-init-script) for scope |
| API documentation | `./gradlew dokkaGeneratePublicationHtml` | `build/dokka/html/index.html` |
| Dependency baseline check | `./gradlew :app:dependencyGuard` | Compares release runtime dependencies with `app/dependencies/releaseRuntimeClasspath.txt` |
| APK badging check | `./gradlew :app:checkReleaseBadging` | Builds release APK and compares badging with `app/release-badging.txt` |
| Baseline profile / benchmark | See [Baseline profiles](#baseline-profiles) | Managed emulator and SDK system image required |
| Signed release APK / AAB | `./gradlew assembleRelease` / `./gradlew bundleRelease` | Configure [release signing](getting-started.md#release-build-setup); outputs in `app/build/outputs/apk/release/` / `app/build/outputs/bundle/release/` |

Regenerate guard files only after reviewing an intentional dependency or manifest change; the
[guardrail sections](#build-guardrails) explain the update tasks. Use a focused module task while
iterating; a routine build does not require `clean`.

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

### Apply or extend the build configuration

A Compose feature module applies the existing UI, Hilt and documentation aliases, as
`feature/home/build.gradle.kts` does:

```kotlin
plugins {
    alias(libs.plugins.jetpack.ui.library)
    alias(libs.plugins.jetpack.dagger.hilt)
    alias(libs.plugins.jetpack.dokka)
}
```

Use `jetpack.library` for a non-UI Android library. These aliases resolve to the convention plugins
above; module-specific configuration still belongs in the module's `android` and `dependencies`
blocks. Follow the [feature guide](guide.md) for module registration and dependencies.

For an approved new dependency, add its version to `[versions]` and alias to `[libraries]` in
`gradle/libs.versions.toml`, then consume the generated accessor:

```kotlin
// Existing aliases in core/network/build.gradle.kts:
dependencies {
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp.core)
}
```

A BOM supplies versions for its member libraries; do not assign competing member versions.
For a new external Gradle plugin, add a `[plugins]` alias. The catalog owns library and plugin
versions used through these aliases; the Spotless init script and GitHub workflows also contain
explicit tool versions. Editing prose does not move or update them.

`.github/renovate.json` extends `config:recommended`, `group:all`, `:dependencyDashboard`, and
`schedule:daily`. When adopting the template, enable the Renovate GitHub App for your repository
and review its onboarding/dashboard and grouped updates. No Dependabot configuration is supplied.

Discuss new dependencies, modules, convention-plugin changes, SDK/compiler upgrades and formatting
configuration changes before implementing them. To extend a convention plugin after agreement,
edit its class/shared helpers in `build-logic/convention/src/main/kotlin/`; a new plugin also needs
registration in `build-logic/convention/build.gradle.kts` and a catalog alias. Run the convention
checks and affected consumer build afterward. Root-relative paths in shared build logic use
`isolated.rootProject.projectDirectory` to support Isolated Projects.

### Compose compiler stability config

`compose_compiler_config.conf` (repo root) lists types the Compose compiler should treat as stable
even though it can't prove it — currently just `dev.atick.data.model.**`, the data layer's immutable
models:

```
// Only add types that really are immutable — an entry here is a promise the compiler trusts
// without checking.
dev.atick.data.model.**
```

Every Compose module reads this file through `AndroidCompose.kt`
(`stabilityConfigurationFiles.add(...)`). The configuration lets the compiler treat matching types
as stable across module boundaries; it does not verify their immutability.

## Code quality

### Spotless, via an init script

Spotless is applied by `gradle/init.gradle.kts` when that init script is passed explicitly.
Without it, the normal build does not register Spotless tasks. With it, the script applies the
plugin to subprojects and defines these targets relative to each project's directory:

| Target | Files | License header | Ruleset |
|---|---|---|---|
| `kotlin` | `**/*.kt` | `spotless/copyright.kt` | ktlint 1.6.0, `android=true`, Compose rules 0.6.3 |
| `groovy` | `**/*.gradle` | `spotless/copyright.gradle` | Header insertion |
| `kts` | `**/*.kts` | `spotless/copyright.kts` | Header insertion |
| `xml` | `**/*.xml` | `spotless/copyright.xml` | Header insertion |

Each target excludes files under `build/`. Markdown is not a target, and the root project's own
files are outside the `subprojects` block. The root `spotlessCheck` invocation checks main-build
subprojects, including intermediate grouping projects; it does not run the included build's checks.

Task discovery works when the init script is present:
`./gradlew tasks --all --init-script gradle/init.gradle.kts --no-configuration-cache`.
The included build also registers Spotless tasks when addressed explicitly, but its check currently
fails: header paths resolve to the missing `build-logic/spotless/copyright.*` templates. A successful
root check therefore does not establish formatting coverage for convention-plugin sources. Fixing
that init-script limitation is a separate build configuration change.

```bash
./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache
./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache
```

To personalize copyright headers, edit the holder/year in all four `spotless/copyright.*`
templates, run the apply command, and review the resulting source diff before checking it.
The XML template is an XML comment; the Kotlin/Gradle templates are block comments. Preserve those
wrappers and the init script's header delimiters. Changing targets or rules needs separate agreement.

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

`dev.atick.test` registers these devices in `:benchmarks`. Its baseline-profile configuration selects
`pixel6Api33` and disables connected devices. API 33 is the configured generation environment; this
is not a minimum API requirement for apps consuming profiles. See the current
[profile generation requirements](https://developer.android.com/topic/performance/baselineprofiles/create-baselineprofile).
Install the corresponding SDK emulator/system image and enable hardware virtualization. Gradle can
download missing device components; running these tasks boots an emulator.

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

Generate the startup profile, then measure the two compilation modes in `StartupBenchmark`:

```bash
./gradlew :app:generateReleaseBaselineProfile
./gradlew :benchmarks:pixel6Api33BenchmarkReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=dev.atick.benchmarks.StartupBenchmark
```

The class filter keeps the measurement run separate from `BaselineProfileGenerator`. Generation
writes profile sources under `app/src/release/generated/baselineProfiles/`; review those files when
regenerating. Managed-device HTML results are under
`benchmarks/build/reports/androidTests/managedDevice/`; benchmark JSON/traces are in the corresponding
`benchmarks/build/outputs/managed_device_android_test_additional_output/` tree. Compare
`timeToInitialDisplayMs` for `startupNoCompilation` and `startupWithBaselineProfile`; the latter
requires a packaged profile. Emulator results help check the harness; use a physical device for
representative performance measurements. This generator covers app startup only.

### Compose compiler metrics and reports

Compile a UI module with both opt-in properties:

```bash
./gradlew :feature:home:compileReleaseKotlin -PenableComposeCompilerMetrics=true -PenableComposeCompilerReports=true
```

`AndroidCompose.kt` enables each destination only when its property is true. Outputs are relative
to the **root** build directory, retaining the module path: this command writes to
`build/feature/home/compose-metrics/` and `build/feature/home/compose-reports/`, not
`feature/home/build/`. Other UI modules use the same layout. If an up-to-date compile has no output,
repeat the focused command with `--rerun-tasks`.

Read the metrics JSON for aggregate counts, the composables text/CSV reports for inferred stability
and restartable/skippable functions, and the classes report for unstable fields. Compare reports
from the same variant before and after a change. These are compiler decisions, not runtime timings;
use Android Studio's Layout Inspector for recomposition inspection and CPU/System Trace profiling
for measured work. To inspect app size, build a release APK and open it in Studio's APK Analyzer.
Release shrinking is already enabled; investigate release-only failures with Logcat and
`app/build/outputs/mapping/release/mapping.txt` before changing keep rules.

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

### Local build configuration

The REST client reads `BuildConfig.BACKEND_URL`. Supply `BACKEND_URL=https://your-backend.example/`
in root `local.properties` when adopting a backend, with a trailing slash as Retrofit requires.
The secrets plugin uses `secrets.defaults.properties` as its fallback file; see
[Firebase setup](firebase.md) for the separate `GOOGLE_WEB_CLIENT_ID` input. These values become
build configuration, so do not use them to embed server credentials in the app.

### Gradle build performance

Set in `gradle.properties`: `org.gradle.parallel=true`, `org.gradle.caching=true`,
`org.gradle.configuration-cache=true` (with `.parallel=true`), and
`org.gradle.jvmargs=-Xmx8g -XX:+UseParallelGC -XX:MaxMetaspaceSize=2g`.

Develocity build scans (`settings.gradle.kts:47-56`) publish only when a `CI` environment variable is
set, so automatic publication is disabled when `CI` is absent or empty. A nonempty value such as `CI=false`
still satisfies that predicate; an explicit `--scan` request is a separate choice.

## CI/CD

| Workflow | Trigger | Does |
|---|---|---|
| `ci.yml` (🚀 Build & Validate) | Pull request; `repository_dispatch` type `libs-versions-updated` | `lint` job: Spotless check, `:build-logic:convention:check`. `build` job: Dependency Guard, unit tests, `:lint:test`, project build, Android Lint (uploaded to Code Scanning), APK badging check |
| `cd.yml` (🚀 Release APK) | Tag push `v*.*.*` | `lint` → `build` (assembles the signed release) → `github-release` and `play-store-release` (Fastlane) |
| `docs.yml` (📚 Documentation Validation & Deployment) | Pull request; push to `main` | Validates real API/site output; a separate main-only job deploys the validated artifact |
| `claude.yml` (Claude Code) | Issue/PR comments and reviews | Claude Code GitHub Action automation — not part of the build pipeline |

## Release

### Prepare the application and signing

1. Set the adopted application's ID in `app/build.gradle.kts` and match it in Firebase, Play Console
   and `fastlane/Appfile`. The supplied Appfile still names `dev.atick.compose`.
2. Update `majorUpdateVersion`, `minorUpdateVersion`, and `patchVersion` in the app build file.
   The current formula is `major * 10000 + minor * 100 + patch`; verify that the resulting
   `versionCode` exceeds every version already uploaded to Play. A Git tag does not set the version.
3. Follow [release signing setup](getting-started.md#release-build-setup). Keep the established
   signing/upload key for updates. For the workflow's `app/key.jks`, set `storeFile=key.jks` in the
   root signing properties: the build resolves this path relative to `app`, not the repository root.
4. Build and test the signed release locally, review `CHANGELOG.md`, and run the checks listed in
   [Contributing](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/CONTRIBUTING.md).
   Keep signing material and service-account keys out of commits.

> [!CAUTION]
> Without `keystore.properties`, the release build falls back to the debug key. Build success alone
> does not establish that an APK or AAB is correctly signed for publication.

### Play access and Bundler

Use a managed Ruby environment with Bundler; the release workflow currently selects Ruby 4.0.6,
while `Gemfile.lock` records Bundler 2.7.2 and Fastlane 2.237.0. From the repository root:

```bash
bundle install
```

Use `bundle exec` for later Fastlane commands so they use the lockfile. Do not run `fastlane init`
over this repository's existing Appfile/Fastfile. See [Fastlane installation](https://docs.fastlane.tools/getting-started/android/setup/)
for Ruby/Bundler setup.

For Play access, create the app in Play Console and complete an initial manual build upload before
using Supply. Enable the Google Play Developer API in a Cloud project, create a service account,
and invite its email in Play Console's Users and permissions with access to the intended app and
release track. Google no longer requires linking the developer account to a Cloud project; follow
[Google's API setup](https://developers.google.com/android-publisher/getting_started) for current
account steps. This workflow uses a downloaded service-account JSON key at
`fastlane/play-store.json`; Firebase's `google-services.json` is a different file.

The Appfile uses this shape; replace its supplied `dev.atick.compose` package with your adopted ID.
Run Fastlane from the repository root so the key path resolves correctly:

```ruby
json_key_file("fastlane/play-store.json")
package_name("your.application.id")
```

After supplying your key and matching package name, these commands validate access and fetch the
existing listing; they do not publish a release:

```bash
bundle exec fastlane run validate_play_store_json_key json_key:fastlane/play-store.json
bundle exec fastlane supply init
```

Supply downloads metadata into `fastlane/metadata/android/<locale>/`. Review titles/descriptions
there and add images/screenshots explicitly; the initial download does not retrieve existing
screenshots or videos. See [Supply setup and metadata](https://docs.fastlane.tools/actions/supply/).

### Repository secrets

In the adopted GitHub repository's **Settings → Secrets and variables → Actions**, add these
repository secrets. The table follows `.github/workflows/cd.yml`; use your own files and values.

| Secret | Stored format | Workflow destination |
|---|---|---|
| `GOOGLE_SERVICES` | Base64 of Android Firebase configuration JSON | `app/google-services.json` |
| `KEYSTORE` | Base64 of the binary signing keystore | `app/key.jks` |
| `KEYSTORE_PROPERTIES` | Base64 of signing properties (`storeFile=key.jks`, `storePassword`, `keyAlias`, `keyPassword`) | Root `keystore.properties` |
| `GOOGLE_WEB_CLIENT_ID` | Plain web OAuth client ID | `GOOGLE_WEB_CLIENT_ID=…` in root `local.properties` |
| `PLAY_STORE_JSON` | Base64 of the Play service-account JSON key | `fastlane/play-store.json` in the Play job |

To encode a file on macOS or Linux without wrapped lines, use the following pattern locally with
your own input/output paths, then paste the output into the matching secret:

```bash
base64 < /path/to/input-file | tr -d '\r\n' > /path/to/encoded-file
```

Repeat for the four encoded inputs; paste the web client ID directly. Base64 is an encoding, not
encryption: treat encoded copies like the originals. The workflow decodes those four inputs and
writes the client ID as text. Both build jobs reconstruct their own signing/Firebase inputs;
the Play job additionally reconstructs the service-account key.

### Lanes, metadata, and publication

| Lane | Current behavior |
|---|---|
| `test` | Runs Gradle `test` |
| `beta` | Legacy lane calling `assembleRelease` then `crashlytics`; not a working distribution setup for the locked Fastlane version |
| `deploy` | Builds an AAB with `bundleRelease`, generates English release notes from five commits, then calls `upload_to_play_store(validate_only: false)` |

The legacy Crashlytics Beta action is not present in Fastlane 2.237.0. Firebase Crashlytics crash
reporting handles crash reports; a future distribution workflow would need separate
configuration, such as [Firebase App Distribution](https://firebase.google.com/docs/app-distribution/android/distribute-fastlane).
Do not use the supplied `beta` lane as release instructions.

`deploy` overwrites `fastlane/metadata/android/en-US/changelogs/default.txt`. Hand-editing that file
before running the lane will lose the edits. Supply prefers `<versionCode>.txt` over `default.txt`
for that version; other locale notes and listing assets also need review. Changelog files are
currently gitignored, so local notes will not reach a tag checkout unless you deliberately change
how they are supplied. The release checkout also uses the default shallow history: the lane's
five-commit request does not guarantee five commits are available there.

> [!WARNING]
> The supplied deploy lane performs a real upload. With no track/status override, the locked
> Supply defaults are **production** and **completed**. Metadata and images are uploaded too when
> supplied. Choose and configure your release policy before triggering this workflow; the lane is
> not a validation-only or internal-testing lane.

Once the adopted repository is configured and the release is reviewed, create a version tag such
as `v1.3.1` on that revision and push that tag to trigger `cd.yml`. Tag publication is an explicit
release action. The workflow then:

1. Runs formatting validation and builds a signed APK.
2. Starts two separate jobs after the APK build: `github-release` attaches that APK and uses the
   whole root `CHANGELOG.md` as the release body; `play-store-release` rebuilds an AAB with Fastlane
   and uploads it to Play. Neither publication job waits for the other to succeed.

For a deliberate local Play publication with the same configured lane, the command is
`bundle exec fastlane android deploy`. Generated `fastlane/README.md` lists lane commands; this
section supplies the setup and consequences.

If a release fails, inspect the failed job first. Base64/JSON errors point to secret format;
signing errors to the decoded properties, app-relative key path or alias; Play permission errors
to API enablement and app access; duplicate-version errors to `versionCode`. Check listing/draft-app
requirements in Play Console for metadata rejection. The APK build has a 45-minute timeout; a
successful GitHub release does not establish that Play publication succeeded.

## Local documentation preview

Run from the repository root after completing Android/JDK setup. Python 3.14 matches `docs.yml`.
The packages below mirror CI; only the admonitions plugin is pinned.

```bash
python3 -m venv .venv
.venv/bin/python -m pip install mkdocs mkdocs-material mkdocs-github-admonitions-plugin==0.1.1 Pygments
./gradlew dokkaGeneratePublicationHtml -PdocsRevision="$(git rev-parse HEAD)"
.venv/bin/python -m unittest discover -s scripts -p 'test_*.py'
.venv/bin/python scripts/prepare_docs.py
.venv/bin/python -m mkdocs build --strict --config-file build/docs-mkdocs.yml
.venv/bin/python scripts/check_docs.py --site site
.venv/bin/python -m mkdocs serve --config-file build/docs-mkdocs.yml
```

Open the server URL printed in the terminal (normally `http://127.0.0.1:8000/`) and stop it with
Ctrl+C. Home is available at `/`, Dokka at `/api/`, and the benchmark and custom-lint references
under Tooling. Run preparation and the strict build again after changing documentation; `serve`
watches the assembled copy rather than the authored guides.

`prepare_docs.py` requires real `build/dokka/html/index.html`; it does not create placeholder API
pages. It replaces only `build/docs-source/`, copies authored guides and actual API output there,
and generates the README homepage and tooling references. The generated configuration is
`build/docs-mkdocs.yml`. Obsolete API files cannot survive a fresh preparation.

The homepage transformation adjusts Markdown link destinations, preserving external URLs and
fragments. Repository-only links and generated source links point to the documented commit.
Preparation defaults to HEAD; `--revision <full-commit-sha>` and Gradle's matching
`-PdocsRevision=<full-commit-sha>` select a different revision. Uncommitted content can be previewed,
but remote source links describe the selected commit, not local edits. Historical evidence pins
remain unchanged. Module READMEs use absolute `main` URLs because Dokka treats relative Markdown destinations
in those introductions as unresolved references. Preparation rewrites active `main` links to the
selected commit in the assembled site. Use that preview for unmerged documentation; repository
README links follow the released guides until merge.

Dokka publishes seventeen application/support modules, including navigation and testing. Each
module includes its README introduction. API pages show declared members; superclass links lead
to inherited SDK API references. Benchmark and lint READMEs are guide references rather
than extra API modules, so documenting them does not change their build configuration. Dokka
warnings fail generation. MkDocs validates authored page links and anchors; `check_docs.py` also
checks root/module references and assembled HTML destinations, images, assets and fragments.
External URL availability and visual usability still require review.

The thirteen retired guide paths have compatibility pages. Old heading anchors land on those
pages, which link to maintained replacements; they do not restore obsolete instructions. The
historical homepage URL remains `/`. Compatibility pages outside navigation remain published.

Pull requests and every push to main, including workflow-only changes, run the same preparation,
strict build and link checks. CI validates the PR head commit; it uploads the site as an artifact.
A separate deployment job runs only after successful main-push validation and publishes that
artifact without rebuilding. Pull-request validation has read-only repository permissions. Local
preview commands do not deploy anything.

`build/`, `site/` and `.venv/` are ignored. The older generated paths `docs/index.md` and `docs/api/`
also remain ignored but are not inputs to the new preparation process. Authored compatibility
pages in `docs/*.md` remain tracked. Do not commit generated output or local environments.

## Further reading

- [Architecture](architecture.md) — the module graph these plugins build
- [Getting Started](getting-started.md) — first build on a fresh clone
- [Contributing](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/CONTRIBUTING.md) — contribution and verification guidance
