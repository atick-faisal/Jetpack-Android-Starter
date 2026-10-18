# Troubleshooting

Start with the first failing task or runtime exception, not the last cascade of errors. Each entry
below gives checks to distinguish causes, a targeted correction, and a way to confirm recovery.
Run Gradle commands from the repository root. Keep credentials and user data out of shared logs.

## Build Errors

<details markdown="1">
<summary>JDK version mismatch</summary>

**Check:** Run `./gradlew --version` and compare the launcher/daemon JVMs with Android Studio's
Gradle JDK. The build uses Java 21; `gradle/gradle-daemon-jvm.properties` requests a JetBrains 21
toolchain. The settings check accepts 17+, which does not establish compatibility with the full build.

**Fix and verify:** Follow [JDK setup](getting-started.md#prerequisites), align the IDE and terminal
configuration, then repeat the failing task. A missing toolchain download is a resolution/network
problem, distinct from running an incompatible JVM.
</details>

<details markdown="1">
<summary>Could not resolve a dependency, or a <code>libs</code> alias is unresolved</summary>

**Check:** Read the first resolution error: an unknown alias points to the version catalog or
accessor spelling; a missing artifact points to its coordinates/repository; HTTP, TLS or timeout
errors point to access. Check Gradle offline mode and the repositories in `settings.gradle.kts`.

**Fix and verify:** Correct the alias in `gradle/libs.versions.toml` or restore access to the named
repository, then rerun the failing task. Use `--refresh-dependencies` only when evidence points to
stale dependency metadata; `clean` does not repair network access or incorrect coordinates.
See [build configuration](build-and-tooling.md#apply-or-extend-the-build-configuration).
</details>

<details markdown="1">
<summary><code>[Dagger/MissingBinding]</code>, duplicate bindings, or an injection cycle</summary>

**Check:** Follow the diagnostic's dependency path to the first unresolved or duplicated type.
Compare qualifiers at the request and binding, the installed component/scope, and the consuming
module's dependencies. A binding in an unrelated module may not be visible. An interface needs a
binding to its implementation; a third-party constructor usually needs a provider.

**Fix and verify:** Use the existing `@Inject`, `@Binds` and `@Provides` patterns in
[Dependency injection](data.md#dependency-injection). Remove duplicate ownership or break an actual
construction cycle; adding `Lazy` without understanding the cycle is not a general repair.
Check the Hilt convention plugin is applied where required, then rerun the failing compile task.
</details>

<details markdown="1">
<summary>KSP fails, or generated Hilt/Room symbols are missing</summary>

**Check:** Find the first processor diagnostic before the unresolved generated references. Inspect
Room query/entity errors separately from Hilt bindings. This project uses KSP; adding Kapt is not
a repair. Check the relevant convention plugin and module dependency wiring after module changes.

**Fix and verify:** Correct the reported query, annotation, type or binding and rerun that module's
failing KSP/compile task. For Room registration see [persistence wiring](guide.md#register-persistence-before-injecting-it).
If processing succeeds but the IDE remains stale, sync Gradle and rebuild the affected module before
considering an IDE cache reset.
</details>

<details markdown="1">
<summary>Duplicate class errors</summary>

**Check:** Identify both artifacts named in the error. For app debug runtime dependencies, run:

```bash
./gradlew :app:dependencyInsight --configuration debugRuntimeClasspath --dependency kotlin-stdlib
```

Replace `kotlin-stdlib` with the conflicting dependency; use the failing module/configuration when
the problem is elsewhere. Two libraries packaging the same classes can fail even with aligned versions.

**Fix and verify:** Remove the redundant artifact or correct the dependency declaration and rerun
the failing task. Firebase versions are coordinated by the existing BOM convention, but a BOM does
not resolve every duplicate-class problem. Dependency changes require review and may change the
[Dependency Guard baseline](build-and-tooling.md#dependency-guard).
</details>

<details markdown="1">
<summary>"Configuration cache problems found in this build"</summary>

**Check:** Open the configuration-cache HTML report linked in Gradle output and identify the task,
plugin and unsupported access. `gradle.properties` uses `problems=warn`; that setting does not make
reported problems correct or guarantee reuse. Do not assign every warning to Google Services.

**Fix and verify:** As a diagnostic comparison, repeat the same task with `--no-configuration-cache`.
For project-owned access, follow [Gradle's report and debugging guidance](https://docs.gradle.org/current/userguide/configuration_cache_debugging.html).
For third-party access, check the installed plugin version and its upstream issue before choosing a
workaround. Rerun normally to check whether the report persists and whether the cache is reused.
</details>

<details markdown="1">
<summary>Gradle/KSP is unexpectedly slow, or the daemon runs out of memory</summary>

**Check:** Separate dependency download, configuration, compilation and processor time using the
task output and a local `./gradlew assembleDebug --profile` report. Compare the same task twice
without cleaning. Check daemon errors and available machine memory; the configured heap is 8 GB.

**Fix and verify:** Address the measured phase, then repeat the same workload. Avoid increasing heap
or disabling incremental processing without evidence. See [Gradle build performance](build-and-tooling.md#gradle-build-performance);
build scans can upload build information, so use local reports when sharing is unnecessary.
</details>

## Runtime Errors

<details markdown="1">
<summary><code>FirebaseApp is not initialized</code></summary>

**Check:** Inspect the startup exception and selected build variant. Confirm that your Firebase
configuration belongs to that variant's application ID and the Google Services plugin processes it.
Renaming the package without updating Firebase configuration can break initialization.

**Fix and verify:** Complete [Local Project Setup](firebase.md#local-project-setup), rebuild and
launch that variant. Confirm initialization succeeds before diagnosing auth or Firestore requests.
</details>

<details markdown="1">
<summary><code>Hilt entry point not found</code> on startup</summary>

**Check:** Check the merged manifest's Application class and the first injection exception. `App`
has `@HiltAndroidApp` and `MainActivity` has `@AndroidEntryPoint` in the template. Renames, alternate
manifests, missing generated code or the wrong hosting Activity can change that path.

**Fix and verify:** Restore the matching manifest/class annotations and Hilt wiring, rebuild, then
launch the affected Activity. See [dependency injection](guide.md#6-dependency-injection).
</details>

<details markdown="1">
<summary>Navigation destination not found, or arguments arrive null</summary>

**Check:** Compare the navigated `NavKey` type with its registered entry and confirm the feature's
entry provider is included in the app. Trace the actual key value into the assisted ViewModel factory;
a nullable item ID intentionally represents creation of a new item.

**Fix and verify:** Follow [Registering entries](navigation.md#registering-entries) and
[Passing arguments to ViewModels](navigation.md#passing-arguments-to-viewmodels). Open both a new
item and an existing item and confirm each receives the intended ID.
</details>

<details markdown="1">
<summary>Back goes to an unexpected screen, state resets, or restoration fails</summary>

**Check:** Distinguish tab selection from pushing an entry, repeated destination identity from a
new entry, and Activity recreation from process death. Check serializable keys, the app's registered
entry decorators, and whether chrome visibility is derived from the intended current destination.

**Fix and verify:** Use the existing [multi-back-stack wiring](navigation.md#multi-back-stack-state)
and [NavDisplay setup](navigation.md#wiring-navdisplay). Check back behavior across tabs and item
destinations, then recreation/restoration separately. `remember` and a ViewModel do not by themselves
persist arbitrary state across process death; effect execution is tied to composition and keys.
</details>

<details markdown="1">
<summary>UI doesn't reflect ViewModel state changes</summary>

**Check:** Confirm the repository emits, the ViewModel stores the new value, and the screen collects
the same state. In-place mutation or an equal StateFlow value can suppress an observable change.
Reading `.value` alone does not start a `stateInDelayed` upstream. `collectAsState()` can receive
updates; its difference from lifecycle-aware collection is not a blanket explanation for stale UI.

**Fix and verify:** Use immutable `copy` updates and the existing `collectAsStateWithLifecycle`
pattern. Observe an actual changed value while the screen is active, then leave and return.
See [state contracts](state-management.md#the-uistatet-wrapper) and
[subscription lifetimes](state-management.md#subscription-lifetimes), including the current separate-collector limitation.
</details>

<details markdown="1">
<summary>Error or navigation event repeats, disappears, or never completes</summary>

**Check:** Trace where the event wrapper is created/consumed and the effect's keys and lifetime.
Recreating a wrapper permits another consumption. Consumption does not acknowledge successful
delivery. Current `StatefulComposable` can consume an error without completing or starting its callback.

**Fix and verify:** Follow [OneTimeEvent contracts and limitations](state-management.md#onetimeeventt).
Keep actions out of the composition body and use the established effect pattern for navigation.
Exercise recomposition and two consecutive errors; do not assume wrapping an event fixes the recorded
snackbar delivery bug. That runtime repair remains a separate follow-up.
</details>

<details markdown="1">
<summary>Loading remains active, an operation is ignored, or a save appears successful after failure</summary>

**Check:** Determine whether the operation is still suspended, returned a failed `Result`, threw,
or was called while loading. Async update helpers drop calls observed while loading and do not
catch arbitrary thrown exceptions. A single state wrapper shares one loading flag across its operations.
The current Item save discards the repository result, so navigation is not proof of persistence.

**Fix and verify:** Follow [update contracts](state-management.md#exceptions-cancellation-and-concurrent-calls)
and [Item save limitations](state-management.md#updatestatewith). Keep independent operation state
separate when designing an extension. Test success and failure explicitly; do not turn these known
runtime defects into a documented guarantee or silently patch them as part of a docs change.
</details>

<details markdown="1">
<summary><code>updateStateWith</code>/<code>updateWith</code> won't compile</summary>

**Check:** Read the compiler's expected and actual types. `updateStateWith` expects `Result<T>`;
`updateWith` expects `Result<Unit>`. Both require the enclosing ViewModel context and appropriate imports.

**Fix and verify:** Match the helper to the operation's return type and call it in the existing
ViewModel context, then compile the feature. Follow [Context parameters](state-management.md#context-parameters);
the current build needs no extra context-receiver flag.
</details>

<details markdown="1">
<summary>Compose preview doesn't render, or shows the wrong theme/data</summary>

**Check:** Read the preview render error and first compile failure. A destination requiring Hilt,
navigation or live backend data is different from preview-safe content. Check the theme wrapper and
explicit sample data. Ordinary `@Preview` and non-private functions do not inherently fail.

**Fix and verify:** Preview content with sample values and `JetpackTheme`, following
[Previews](components.md#previews) and the [UI walkthrough](guide.md#4-ui-layer). Refresh the preview
after compiling and check the intended theme/device. This project injects ViewModels inside Screen
composables; it does not require a separate Route wrapper.
</details>

<details markdown="1">
<summary>Excessive recomposition or janky list scrolling</summary>

**Check:** Use Layout Inspector counts to locate repeated work, then profile scrolling; a count
alone does not establish expensive rendering. Check stable item identity, allocations, image sizes
and work done in composition before changing lambdas or annotations.

**Fix and verify:** Follow [Performance checks](components.md#performance-checks). Use stable item
keys as HomeScreen does; add `contentType` when different item types benefit from reuse. Measure the
same interaction afterward. `@Immutable` cannot make mutable data safe, and `derivedStateOf` is not
a universal recomposition fix.
</details>

<details markdown="1">
<summary>App won't install, or runs on the wrong device</summary>

**Check:** Run `adb devices -l` and read the installation error. Resolve an offline/unauthorized
device before building again. A signature mismatch, version downgrade or insufficient storage needs
a different correction from device selection.

**Fix and verify:** Select the intended device in Android Studio, or use `adb -s SERIAL install -r`
with the actual built APK path. Replace `SERIAL` with a connected device identifier. Preserve the
existing signing identity/version for upgrades; uninstalling deletes app data and is not a default
repair. Launch the installed variant on that device and confirm its version.
</details>

## Firebase

<details markdown="1">
<summary>Google Sign-In fails, including <code>ApiException: 10</code></summary>

**Check:** Check the underlying exception, application ID, signing certificate for the installed
variant, enabled Google provider and web client ID. A debug SHA-1 alone does not cover release signing
or a mismatched OAuth client.

**Fix and verify:** Complete [Firebase Console Setup](firebase.md#firebase-console-setup) and
[Local Project Setup](firebase.md#local-project-setup). Rebuild if local configuration changed and
confirm the expected Firebase user signs in on that variant.
</details>

<details markdown="1">
<summary>Credential Manager is unavailable, or offers no matching credential</summary>

**Check:** Preserve the exception type: provider availability, cancellation and no matching
credential are different outcomes. Check the device's credential provider/Play Services support.
The sign-in request filters Google accounts to previously authorized ones; registration removes that filter.

**Fix and verify:** Follow [Credential Manager setup and account filtering](firebase.md#credential-manager-sign-in-vs-register).
Use registration for first Google authorization and verify sign-in afterward. Update/configure a
supported provider when availability is the failure; Android version alone does not identify it.
</details>

<details markdown="1">
<summary>Firestore <code>PERMISSION_DENIED</code>, or old documents don't appear</summary>

**Check:** Compare authenticated UID, document path, submitted fields and deployed rules. For missing
documents, check whether they have `serverUpdatedAt`; the range query excludes documents without it.
A permission failure differs from an empty successful query or cached result.

**Fix and verify:** Follow [Firestore rules](firebase.md#firestore-security-rules) and
[Existing installs and documents](firebase.md#existing-installs-and-documents). Test an allowed own-user
operation and a denied cross-user operation in a test environment. Do not loosen rules globally or
assume changing the Markdown deploys them.
</details>

<details markdown="1">
<summary>Crashlytics not reporting, or release traces are unreadable</summary>

**Check:** Check the configured Firebase app, collection/upload logs and whether the test was a fatal
crash or a non-fatal `reportException` call. For obfuscated releases, inspect mapping-upload results.
Delay is only one possible cause.

**Fix and verify:** Follow [Crashlytics verification](firebase.md#crashlytics), including restart after
a test crash and debug logging for absent reports. Confirm the report appears in the intended project
and release frames are readable. Event instrumentation scope is explained there too.
</details>

## Sync

<details markdown="1">
<summary>Sync never runs, <code>isSyncing</code> never emits <code>true</code></summary>

**Check:** Inspect WorkManager state using Android Studio's Background Task Inspector and worker
logs. The unique name is `dev.atick.jetpack.sync.worker`. `isSyncing` only reflects `RUNNING`;
waiting constraints, retry backoff, completion and a short run missed by the observer are distinct.
A request log does not prove enqueue acceptance or execution. The network capability used by the
app does not establish validated internet or Firestore reachability.

**Fix and verify:** Check that the repository reaches `requestSync()`, then distinguish pending
connected-network constraints from existing unfinished work kept by `KEEP`. A dropped request does
not queue a later pass. See [progress and retries](data.md#progress-retries-and-errors).
After correcting the cause, observe a new request reach execution and inspect the resulting data.

For device diagnostics, `adb shell dumpsys jobscheduler` lists scheduled jobs; select the intended
device if several are attached. Use [WorkManager's diagnostics broadcast](https://developer.android.com/develop/background-work/background-tasks/testing/persistent/debug)
for a debug build, replacing the package if customized:

```bash
adb shell am broadcast -a androidx.work.diagnostics.REQUEST_DIAGNOSTICS -p dev.atick.compose
```

Inspect Logcat for the resulting diagnostics. Do not assume a device includes `sqlite3`; use the
Inspector or an authorized debug database inspection/export workflow when database evidence is needed.
</details>

<details markdown="1">
<summary>Sync fails repeatedly, exhausts retries, or succeeds without delivering every edit</summary>

**Check:** Read the first `SyncWorker` exception, including foreground setup before repository
collection. Exceptions at attempt counts 0, 1 and 2 request retry; count 3 fails: four attempts total.
Default exponential delays start at 30 seconds and scheduling can take longer. A swallowed exception
can produce apparent success, not explain retry exhaustion.

**Fix and verify:** Correct authentication, rules, reachability or foreground setup according to the
actual failure, then request another sync through an existing trigger. Inspect worker completion
and local/remote content. Success does not establish complete server delivery: concurrent edits,
partial pulls and cached reads have [recorded limitations](data.md#delivery-and-conflict-limitations).
Do not compare device clocks as a general conflict solution. The worker's broad cancellation catch
is also a separate follow-up, not a guarantee that cancelled work retries.
</details>

<details markdown="1">
<summary>No foreground sync notification on Android 13+/14+, or foreground setup fails</summary>

**Check:** Separate a hidden notification from a worker exception. `MainActivity` already requests
notification permission on API 33+, and the app declares it; inspect the user's grant and the
`SyncNotificationChannel` settings. Denial can hide drawer notifications without prohibiting foreground
service launch; see [Android notification permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission).

The worker calls `setForeground` and creates a two-argument `ForegroundInfo` without a service type.
Inspect the selected variant's merged manifest for WorkManager's `SystemForegroundService`, its type
and permissions. The authored manifests do not supply a `dataSync` override. For apps targeting 34+,
foreground worker execution requires matching type configuration; permission alone does not supply it.

**Fix and verify:** For visibility, use the [permission flow](components.md#permission-helpers) and
system notification settings. For a foreground exception, follow [WorkManager service-type setup](https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/long-running#add-a-foreground-service-type-to-a-long-running-worker):
align the merged service declaration, type-specific permissions and runtime `ForegroundInfo` type.
This requires a separately reviewed runtime/configuration change. Test foreground execution and drawer
visibility separately on affected API levels; this documentation repair does not establish either.
</details>

<details markdown="1">
<summary>Customizing sync constraints or resolving sync conflicts</summary>

This is extension guidance rather than a failure diagnosis. Current work requires a connected network,
requests expedited execution with ordinary-work fallback, and has no periodic scheduling or general
conflict-resolution policy. See [Two-way sync](data.md#two-way-sync) before changing constraints or
protocols. Verify pending writes, concurrent edits/deletes and retry behavior in a separately scoped
implementation; tightening constraints cannot repair data consistency.
</details>

## Code Quality & CI

<details markdown="1">
<summary>Spotless license-header or ktlint failures, including CI <code>spotlessCheck</code></summary>

**Check:** Read the affected file and rule in the report. Distinguish missing headers from Kotlin
formatting and Compose rules; templates live in `spotless/`.

**Fix and verify:** Run `./gradlew spotlessApply --init-script gradle/init.gradle.kts --no-configuration-cache`,
review its diff, then run `spotlessCheck` with the same flags. The init script is required.
See [Spotless](build-and-tooling.md#spotless-via-an-init-script).
</details>

<details markdown="1">
<summary>Dependency Guard, APK badging, or custom lint fails in CI</summary>

**Check:** Read the failing task and diff/report. Dependency Guard checks release dependencies;
badging checks package/version/SDK/permission output; custom lint reports specific repository rules.
Compare the failure with the intended code, dependency or manifest change.

**Fix and verify:** Revert unintended drift or, after approving an intentional change, follow
[Dependency Guard](build-and-tooling.md#dependency-guard) or [APK badging](build-and-tooling.md#apk-badging)
to update its baseline/golden file and inspect the diff. Follow [custom lint](build-and-tooling.md#custom-lint-lint)
for reported resource/API rules. Rerun the failing gate; regenerating a baseline is not diagnosis.

**Renovate PRs:** CI commits the Dependency Guard baseline itself, so review that bot commit's
diff instead of regenerating it. The bot commit shows no checks; that is expected. Badging still
fails until updated by hand. See [Renovate PRs](build-and-tooling.md#renovate-prs).
</details>

## Build Configuration

<details markdown="1">
<summary>"keystore.properties file not found. Using debug key."</summary>

**Check:** Determine whether this is a disposable local build or an intended release. Missing signing
configuration makes the template fall back to debug signing.

**Fix and verify:** Follow [Release Build Setup](getting-started.md#release-build-setup) and verify
the built artifact's signing certificate against the intended distribution identity.

> [!CAUTION]
> A release artifact signed with the debug key is unsuitable for publication. An existing install
> also needs a compatible signing identity for upgrade; a successful build does not establish that.
</details>

<details markdown="1">
<summary>R8 strips a class needed at runtime (release build only)</summary>

**Check:** Reproduce on the affected minified variant and inspect the exception and mapping output.
Obfuscated names alone do not prove a class was removed. Check reflection/serialization requirements
against the library's consumer rules before proposing an app rule.

**Fix and verify:** Follow [release preparation](build-and-tooling.md#prepare-the-application-and-signing).
If a keep rule is needed, propose the narrow rule with evidence for review; ProGuard changes require
approval. Exercise the failing path in the minified build and check readable Crashlytics traces.
</details>

## Data Layer

<details markdown="1">
<summary>A repository error crashes the app, disappears, or is reported as success</summary>

**Check:** Trace whether the operation returns `Result`, emits a Flow failure or throws outside the
helper's handling boundary. Async helpers inspect returned results; they do not catch arbitrary throws.
A `catch` that constructs but never emits/stores state does not update the UI. Item load/save has
these [known limitations](state-management.md#updatestatewith).

**Fix and verify:** Use [suspendRunCatching](data.md#result-via-suspendruncatching) for one-time operations
and explicit Flow error handling that stores/emits the intended state. Preserve cancellation.
Test returned failure, thrown failure and actual error delivery separately. Known helper/Item/event
runtime repairs remain follow-ups; adding a wrapper alone does not establish end-to-end handling.
</details>

<details markdown="1">
<summary><code>Room cannot verify the data integrity</code>, a missing migration, or lost local data</summary>

**Check:** Compare the installed schema/version with the current entity definitions. A same-version
identity mismatch differs from a version upgrade without a migration. The template is version 2 and
uses `fallbackToDestructiveMigration(true)` without a registered 1 → 2 migration.

**Fix and verify:** Follow [Room persistence and schema changes](data.md#room-persistence-and-schema-changes).
For retained data, implement and test a preserving migration before distribution. Destructive fallback
can erase pending edits on an upgrade and is not a general repair for same-version integrity errors.
Verify a file-backed upgrade with existing/pending rows; current in-memory DAO tests do not prove it.
Do not clear app storage as the default diagnostic step.
</details>

## Memory

<details markdown="1">
<summary>LeakCanary reports a leak</summary>

**Check:** Read the retained-object trace and the first unexpected reference path from a GC root.
Reproduce the navigation/rotation that retained the object and distinguish temporary retention from
a repeated leak. LeakCanary is a debug-only dependency.

**Fix and verify:** Remove or shorten the lifetime of the retaining reference/registration. For
collectors, check [subscription lifetimes](state-management.md#subscription-lifetimes); lifecycle-aware
screen collection does not stop separately launched ViewModel jobs. Application context and Activity
context have different lifetimes, so banning every Context reference is not a diagnosis.
Repeat the same interaction and confirm the object is released in a new LeakCanary analysis.
</details>

<details markdown="1">
<summary><code>OutOfMemoryError</code> loading images</summary>

**Check:** Inspect the allocation/heap evidence, decoded image dimensions, simultaneous requests
and retained screens. `DynamicAsyncImage` is not an automatic repair: its current painter is not
wired to the DI-provided loader merely by using the wrapper.

**Fix and verify:** Follow [Images and loading feedback](components.md#images-and-loading-feedback),
constrain request/decode size where appropriate and address retained allocations. Repeat the same
image workload and compare memory use; check visual quality and scrolling as well as absence of a crash.
</details>

## Testing

Test commands, helpers and worked examples live in the [command reference](build-and-tooling.md#command-reference)
and [Guide: Testing](guide.md#testing). For a test failure, start with the first failing assertion or
setup exception and the selected module/variant. When testing shared Flow state, collect it; reading
an unstarted `stateInDelayed` value can test only the initial state. See [state tests](state-management.md#testing).

## Getting Additional Help

Include the failing task/variant or device API, first relevant exception, minimal reproduction,
expected result and checks already tried. Use Timber/Logcat for runtime failures and `--stacktrace`
for Gradle failures. Remove credentials and personal data before sharing logs.

- [Getting Started](getting-started.md) · [Architecture](architecture.md) · [State Management](state-management.md) · [Navigation](navigation.md) · [Data](data.md) · [Build & Tooling](build-and-tooling.md) · [Firebase](firebase.md)
- [GitHub Issues](https://github.com/atick-faisal/Jetpack-Android-Starter/issues) — search for the exact diagnostic and relevant plugin version before filing an issue
