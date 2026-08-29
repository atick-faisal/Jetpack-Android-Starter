# Troubleshooting

Collapsible entries, one per symptom — cause and fix only. Deeper mechanics live in the linked canonical doc.

## Build Errors

<details>
<summary>JDK version mismatch</summary>

**Cause:** This template requires JDK 21 (`gradle/libs.versions.toml:4`). `settings.gradle.kts:95-97` only asserts 17+, so a JDK between 17 and 20 passes that check but can still fail elsewhere in the build.
**Fix:** Install JDK 21 and set it as the Gradle JDK (**File → Project Structure → SDK Location → Gradle Settings**).
</details>

<details>
<summary>Could not resolve a dependency (e.g. `firebase-bom`)</summary>

**Cause:** Stale cache, no network, or a proxy blocking `google()`/`mavenCentral()` (`settings.gradle.kts:32-44`).
**Fix:** `./gradlew clean --refresh-dependencies`; configure a proxy in `gradle.properties` if needed.
</details>

<details>
<summary>`[Dagger/MissingBinding]`</summary>

**Cause:** A type has no `@Inject` constructor and no `@Binds`/`@Provides` in a Hilt module.
**Fix:** Add one, and confirm the module applies `alias(libs.plugins.jetpack.dagger.hilt)` (`build-logic/convention/src/main/kotlin/DaggerHiltConventionPlugin.kt`).
</details>

<details>
<summary>Duplicate class errors (e.g. `kotlin.collections.CollectionsKt`)</summary>

**Cause:** Conflicting transitive versions — usually a Firebase library added outside the BOM.
**Fix:** Depend on `platform(libs.firebase.bom)` instead of pinning individual Firebase versions (`build-logic/convention/src/main/kotlin/FirebaseConventionPlugin.kt:35`).
</details>

<details>
<summary>"Configuration cache problems found in this build"</summary>

**Cause:** The `google-services` plugin isn't configuration-cache compatible yet ([google/play-services-plugins#246](https://github.com/google/play-services-plugins/issues/246)).
**Fix:** Nothing to do — `gradle.properties` intentionally sets `org.gradle.configuration-cache.problems=warn`; the build still succeeds.
</details>

## Runtime Errors

<details>
<summary>`FirebaseApp is not initialized`</summary>

**Cause:** `google-services.json` is missing from `app/`, or its package name doesn't match `applicationId`.
**Fix:** See [Firebase: Local Project Setup](firebase.md#local-project-setup).
</details>

<details>
<summary>`Hilt entry point not found` on startup</summary>

**Cause:** The `Application` class or an `Activity` lost its Hilt annotation.
**Fix:** `App` (`app/src/main/kotlin/dev/atick/compose/App.kt`) must carry `@HiltAndroidApp`; `MainActivity` must carry `@AndroidEntryPoint`. Both already do in an unmodified clone — check you didn't rename or duplicate the class.
</details>

<details>
<summary>Navigation destination not found, or arguments arrive null</summary>

**Cause:** This template uses Navigation 3 (`NavKey`/`Navigator`/`NavDisplay`), not `NavController`/`NavHost`. A destination not registered under the right `NavKey`, or a ViewModel not using assisted injection for keyed args, produces this.
**Fix:** See [Navigation: Registering entries](navigation.md#registering-entries) and [Passing arguments to ViewModels](navigation.md#passing-arguments-to-viewmodels).
</details>

<details>
<summary>UI doesn't reflect ViewModel state changes</summary>

**Cause:** Collecting with `collectAsState()` instead of `collectAsStateWithLifecycle()`, or mutating `data` in place instead of `copy()`-ing it.
**Fix:** See [State Management: the `UiState<T>` wrapper](state-management.md#the-uistatet-wrapper).
</details>

<details>
<summary>Error or navigation event fires more than once</summary>

**Cause:** Not wrapping the event in `OneTimeEvent<T>`, so recomposition replays it.
**Fix:** See [State Management: `OneTimeEvent<T>`](state-management.md#onetimeeventt).
</details>

<details>
<summary>`updateStateWith`/`updateWith` won't compile</summary>

**Cause:** Usually a `Result<T>`/`Result<Unit>` mismatch with the repository call — not a missing compiler flag. Context parameters are on by default in Kotlin 2.4.10 (`build-logic/convention/src/main/kotlin/dev/atick/KotlinAndroid.kt:82`); no `-Xcontext-receivers` needed.
**Fix:** See [State Management: Update functions](state-management.md#update-functions).
</details>

<details>
<summary>Compose preview doesn't render, or shows the wrong theme/data</summary>

**Cause:** Missing `JetpackTheme { }` wrapper, using `@Preview` instead of the template's `@PreviewDevices`/`@PreviewThemes`, or a non-`private` preview function.
**Fix:** Wrap content in `JetpackTheme`, use `@PreviewDevices`/`@PreviewThemes` (`core/ui/src/main/kotlin/dev/atick/core/ui/utils/`), keep previews `private`, then **Build → Refresh All Previews**. Preview data is meant to be hardcoded — that's why Screen and Route composables are split; see [Guide: UI layer](guide.md#4-ui-layer).
</details>

<details>
<summary>Excessive recomposition or janky list scrolling</summary>

**Cause:** Missing `key`/`contentType` in a `LazyColumn`, or an unstable lambda/parameter passed down.
**Fix:** Match the real usage in `feature/home/src/main/kotlin/dev/atick/feature/home/ui/home/HomeScreen.kt` (`items(items = jetpacks, key = { it.id })`); use Layout Inspector's "Show Recomposition Counts" to confirm the hotspot before changing anything.
</details>

## Firebase

<details>
<summary>Google Sign-In fails with `ApiException: 10`</summary>

**Cause:** The debug SHA-1 fingerprint isn't registered with the Firebase project.
**Fix:** See [Firebase: Firebase Console Setup](firebase.md#firebase-console-setup).
</details>

<details>
<summary>`CredentialManager is not available`</summary>

**Cause:** Device is below Android 14 without an up-to-date Google Play Services.
**Fix:** Update Play Services — the `androidx.credentials`/`play-services-auth` fallback is already included. See [Firebase: Credential Manager](firebase.md#credential-manager-sign-in-vs-register).
</details>

<details>
<summary>Firestore `PERMISSION_DENIED`</summary>

**Cause:** Security rules reject the request, or the user isn't authenticated yet.
**Fix:** See [Firebase: Firestore Security Rules](firebase.md#firestore-security-rules).
</details>

<details>
<summary>Crashlytics not reporting</summary>

**Cause:** Reporting is a thin wrapper (`CrashReporter`/`FirebaseCrashReporter`) over `crashlytics.recordException()` — it enables automatically once `google-services.json` is in place, so a missing report is almost always a timing issue (can take a few minutes) rather than config.
**Fix:** Call `reportException()` from a debug build and check the console shortly after. See [Firebase: Crashlytics](firebase.md#crashlytics). Note `firebase:analytics` is Crashlytics-only here — there's no event-logging API, so "Analytics events not appearing" isn't a real symptom in this template.
</details>

## Sync

<details>
<summary>Sync never runs, `isSyncing` never emits `true`</summary>

**Cause:** No network (`NetworkType.CONNECTED` is the only default constraint — check logcat for "constraints not met"), or the request was deduped because a sync was already enqueued (`ExistingWorkPolicy.KEEP` in `Sync.kt`).
**Fix:** Confirm connectivity. `Sync.initialize()` is called from `SyncManagerImpl.requestSync()` (`sync/src/main/kotlin/dev/atick/sync/manager/SyncManagerImpl.kt:178`), not from `App.onCreate()` — a "Requesting sync" log line with no follow-up just means one was already running, which is expected.
</details>

<details>
<summary>Sync fails repeatedly, exhausts retries</summary>

**Cause:** The repository's `sync()` swallows an exception instead of rethrowing it, so `SyncWorker` can't apply its retry/backoff — or it's a Firestore permission error.
**Fix:** Ensure `sync()` rethrows on failure; check Firestore rules per the Firebase entry above.
</details>

<details>
<summary>No foreground sync notification on Android 13+/14+</summary>

**Cause:** `POST_NOTIFICATIONS` runtime permission not requested (Android 13+), or the manifest is missing a `foregroundServiceType="dataSync"` declaration for the sync work (Android 14+).
**Fix:** Request `Manifest.permission.POST_NOTIFICATIONS` at runtime; declare `android:foregroundServiceType="dataSync"` on the relevant service entry in `AndroidManifest.xml`.
</details>

<details>
<summary>Customizing sync constraints or resolving sync conflicts</summary>

**Cause:** The default `SyncConstraints` (`sync/src/main/kotlin/dev/atick/sync/worker/SyncWorker.kt:202-205`) only requires `NetworkType.CONNECTED` — no battery/storage/idle constraints, and no conflict-resolution strategy beyond whatever your repository's `sync()` implements.
**Fix:** Tighten `Constraints.Builder()` in `SyncWorker.kt` (e.g. `setRequiresBatteryNotLow(true)`) if needed. For conflicts, pick a strategy inside `sync()` before saving — last-write-wins (compare `lastUpdated`), server-wins, or client-wins.

> [!TIP]
> `adb shell dumpsys jobscheduler | grep -A20 androidx.work` and reading `/data/data/<applicationId>/databases/androidx.work.workdb` directly (via `adb exec-out run-as <applicationId> sqlite3 ...`) are the fastest ways to see what WorkManager actually scheduled.
</details>

## Code Quality & CI

<details>
<summary>Spotless license-header or ktlint failures, incl. CI `spotlessCheck`</summary>

**Cause:** A new file is missing the header from `spotless/copyright.kt`, or violates a rule from `io.nlopez.compose.rules:ktlint`.
**Fix:** `./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache`, then push. See [Build & Tooling: Spotless, via an init script](build-and-tooling.md#spotless-via-an-init-script) — note `spotlessApply` won't show up in `./gradlew tasks` since it's an init-script plugin, not a project one.
</details>

## Build Configuration

<details>
<summary>"keystore.properties file not found. Using debug key."</summary>

**Cause:** Expected on a fresh clone — no signing config is checked in.
**Fix:** See [Getting Started: Release Build Setup](getting-started.md#release-build-setup).

> [!CAUTION]
> This message is easy to miss in build output. A release build made without `keystore.properties` is signed with the debug key and cannot be upgraded or published on Play — see the CAUTION in [Getting Started](getting-started.md#release-build-setup).
</details>

<details>
<summary>R8 strips a class needed at runtime (release build only)</summary>

**Cause:** A missing keep rule. `app/proguard-rules.pro` already keeps `@kotlinx.serialization.Serializable` classes and the attributes Crashlytics needs to de-obfuscate stack traces.
**Fix:** Add a targeted `-keep` rule rather than widening an existing one, and test the actual release build, not just debug.
</details>

## Data Layer

<details>
<summary>A repository error crashes the app instead of surfacing in the UI</summary>

**Cause:** The repository method isn't wrapped in `suspendRunCatching`, so the ViewModel's `updateStateWith`/`updateWith` never receives a `Result` to report through `StatefulComposable`.
**Fix:** See [Data: Result via `suspendRunCatching`](data.md#result-via-suspendruncatching).
</details>

<details>
<summary>`Room cannot verify the data integrity` after a schema change</summary>

**Cause:** The database version wasn't bumped, or there's no `Migration` for the version jump.
**Fix:** For local development, `fallbackToDestructiveMigration()` (`core/room/src/main/kotlin/dev/atick/core/room/di/DatabaseModule.kt`) clears local data; write a real `Migration` before shipping.
</details>

## Memory

<details>
<summary>LeakCanary reports a leak</summary>

**Cause:** Usually a `Context`/`Activity` reference held past its lifecycle, or a Flow collected without a lifecycle-aware collector.
**Fix:** Use `collectAsStateWithLifecycle()`; never hold `Activity`/`Context` in a ViewModel. LeakCanary is pulled in via `debugImplementation` only (`app/build.gradle.kts`), so it's absent from release builds entirely.
</details>

<details>
<summary>`OutOfMemoryError` loading images</summary>

**Cause:** Loading many full-resolution images at once.
**Fix:** Use `DynamicAsyncImage` (`core/ui/src/main/kotlin/dev/atick/core/ui/components/DynamicAsyncImage.kt`), which goes through the shared Coil `ImageLoader` (`core/network/src/main/kotlin/dev/atick/core/network/di/coil/CoilModule.kt`); constrain `.size(...)` on the request for large images.
</details>

## Testing

<details>
<summary>"How do I run tests?"</summary>

**Cause:** N/A — the template ships a real test harness: `./gradlew test` runs across ~16 files, including a Robolectric + Compose UI harness wired into every module by its `AndroidTest.kt` convention.
**Fix:** Fakes and `MainDispatcherRule` live in `:core:testing` (`core/testing/src/main/kotlin/dev/atick/core/testing/`). See the Testing sections in [Guide](guide.md#testing), [Navigation](navigation.md#testing), and [State Management](state-management.md#testing) for worked examples.
</details>

## Getting Additional Help

- [Getting Started](getting-started.md) · [Architecture](architecture.md) · [State Management](state-management.md) · [Navigation](navigation.md) · [Data](data.md) · [Build & Tooling](build-and-tooling.md) · [Firebase](firebase.md)
- [GitHub Issues](https://github.com/atick-faisal/Jetpack-Android-Starter/issues) — search before filing a new one
- Timber is wired in throughout — `Timber.d(...)`/`Timber.e(throwable, ...)` is the fastest way to narrow down a runtime issue
- `./gradlew clean build --refresh-dependencies` resolves most "mysterious" build states
