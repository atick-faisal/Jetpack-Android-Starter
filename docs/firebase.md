# Firebase Setup

This project separates authentication (`firebase:auth`), Firestore access (`firebase:firestore`),
and crash reporting (`firebase:analytics`). The app's Firebase convention plugin also adds Analytics
and Performance SDK dependencies. The reporting module's name does not describe all app telemetry;
see [Crashlytics](#crashlytics) below.

The template configuration supports building the app. Use your own Firebase project to test services.

## Prerequisites

- A Google account and access to the [Firebase Console](https://console.firebase.google.com)
- A checkout that builds locally (see [Getting Started](getting-started.md))
- A device or emulator with Google Play services for Google Sign-In

## Firebase Console Setup

1. Create a Firebase project and register an Android app with the checkout's application ID,
    currently `dev.atick.compose` in `app/build.gradle.kts`.

2. Run `./gradlew signingReport` (or the **Signing Report** IDE configuration). Add the SHA-1
    fingerprint for the variant you will test under **Project Settings → Your apps → SHA certificate
    fingerprints**. Register the release certificate too when testing a signed release; for Play
    installs, use the Play App Signing certificate rather than only the upload certificate.

3. Under **Authentication → Sign-in method**, enable **Google** and **Email/Password**. Complete
    Google's project support-email settings when prompted.

4. Create a Cloud Firestore database in production mode. The app uses the default database;
    publish the rules below before testing sync.

5. After enabling Google and registering fingerprints, download the updated `google-services.json`
    for this Android app from Project Settings.

If you replace the template application ID, register that exact new ID and its signing fingerprints,
then download the matching configuration. Changing a Kotlin package or namespace alone does not
change the application ID. A separate Firebase project/build variant for each environment requires
adopter configuration; the template does not provide those environments.

## Local Project Setup

1. Replace `app/google-services.json` with the downloaded file. Its Android client must match the
    application's ID and Firebase project.

2. In the same Google Cloud project, open **APIs & Services → Credentials** and find the OAuth 2.0
    client whose application type is **Web application**. The Google provider's **Web SDK
    configuration** in Firebase Authentication also exposes its web client ID. Use that client ID,
    not the Android OAuth client ID.

3. Add the following to root `local.properties`, preserving its existing `sdk.dir` entry:

    ```properties
    GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
    ```

    The Secrets Gradle plugin in `firebase/auth/build.gradle.kts` reads this ignored file and generates
    `BuildConfig.GOOGLE_WEB_CLIENT_ID`; `Config.WEB_CLIENT_ID` supplies it to Credential Manager.
    `secrets.defaults.properties` supplies a buildable fallback when a local value is absent. That
    fallback is not configuration for your Firebase project. Downloading the JSON does not set this
    separately consumed property. See the [official Google authentication setup](https://firebase.google.com/docs/auth/android/google-signin).

4. Rebuild and run with `./gradlew installDebug`, then launch the app. Follow the verification steps
    after the rules section.

### Keeping local configuration out of commits

`local.properties` is ignored. The template `app/google-services.json` is already tracked, so the
ignore rule for that filename does not prevent changes to it from being committed. Keep your
project-specific replacement out of commits to this starter repository.

If you use an index hint to reduce local diff noise, set it **before** replacing the template:

```bash
git update-index --skip-worktree app/google-services.json
```

This leaves the file tracked. It is neither a security control nor protection against every Git
operation. Inspect the flag (an `S` prefix means skip-worktree) and undo it with:

```bash
git ls-files -v -- app/google-services.json
git update-index --no-skip-worktree app/google-services.json
```

Review configuration changes before staging; do not rely on the hint to hide them permanently.

## Credential Manager: Sign-In vs. Register

Google auth goes through Android's Credential Manager API
(`firebase/auth/.../AuthDataSourceImpl.kt`), which builds two different requests depending on
whether the user is signing in or registering:

```kotlin
private fun getSignInRequest(): GetCredentialRequest {
    val getPasswordOption = GetPasswordOption()
    val getGoogleIdOption = GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(true)
        .setServerClientId(Config.WEB_CLIENT_ID).setAutoSelectEnabled(false).build()
    return GetCredentialRequest
        .Builder()
        .addCredentialOption(getPasswordOption)
        .addCredentialOption(getGoogleIdOption)
        .build()
}

private fun registerWithGoogleRequest(): GetCredentialRequest {
    val signInRequestOptions = GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false)
        .setServerClientId(Config.WEB_CLIENT_ID).setAutoSelectEnabled(false).build()
    return GetCredentialRequest.Builder().addCredentialOption(signInRequestOptions).build()
}
```

The sign-in request includes saved passwords and filters Google choices to previously authorized
accounts. The registration request removes that Google-account filter. This controls which
credentials Credential Manager offers, not whether a Firebase user already exists. Both Google
paths pass the selected token to `FirebaseAuth.signInWithCredential`, which can create a Firebase
user on first successful sign-in. Email/password registration instead calls
`createUserWithEmailAndPassword` explicitly.

See the [Credential Manager account-filter contract](https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation)
and [Firebase's first-sign-in behavior](https://firebase.google.com/docs/auth/android/google-signin#next_steps).
For a first Google authorization, use the app's registration flow; the sign-in request does not
implement an automatic retry with the filter disabled.

## Firestore Security Rules

Sync (`firebase/firestore/.../FirebaseDataSourceImpl.kt`) writes to
`/dev.atick.jetpack/{userId}/jetpacks/{jetpackId}`, one document per `FirebaseJetpack`
(`id`, `name`, `price`, `userId`, `lastUpdated`, `lastSynced`, `serverUpdatedAt`, `deleted` —
8 fields). The mapper leaves `serverUpdatedAt` null on writes so Firestore assigns a server
timestamp. See [Data](data.md) for the sync algorithm. Review this example under **Firestore
Database → Rules**, test it, and explicitly **Publish** it for your project:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isUserOwner(userId) {
      return request.auth != null && request.auth.uid == userId;
    }

    function isValidJetpack(jetpack) {
      return jetpack.size() == 8
        && jetpack.id is string
        && jetpack.name is string
        && jetpack.price is number
        && jetpack.userId is string
        && jetpack.lastUpdated is number
        && jetpack.lastSynced is number
        && jetpack.deleted is bool
        // Require the timestamp assigned by Firestore for this write.
        && jetpack.serverUpdatedAt == request.time;
    }

    match /dev.atick.jetpack/{userId}/jetpacks/{jetpackId} {
      allow read: if isUserOwner(userId);
      allow create: if isUserOwner(userId) && isValidJetpack(request.resource.data);
      allow update: if isUserOwner(userId) && isValidJetpack(request.resource.data)
        && request.resource.data.id == resource.data.id;
      allow delete: if isUserOwner(userId);
    }

    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

Editing this Markdown example does not deploy rules. Use the Rules Playground to check an
owner's read/create/update/delete requests and denied unauthenticated or other-user requests before
publishing. A rule that deploys successfully can still deny the app's requests at runtime. The
example checks ownership by path and the stated field shape; assess any additional application
validation before using it beyond this demonstration.

Deletion is soft in local Room data, then sync calls Firestore's hard-delete operation. It does not
publish a tombstone: another device's existing local copy is not removed by the incremental query
merely because the remote document is absent.

### Existing installs and documents

> [!WARNING]
> This branch changes Room schema 1 → 2 without a migration and enables destructive fallback.
> Upgrading an existing install can erase local rows, including edits or deletions not yet uploaded.
> Re-fetching remote data cannot restore changes that never reached the server.

The new pull query filters on `serverUpdatedAt`; legacy documents missing that field are excluded,
even on a first pull with a zero cursor. The eight-field rules also reject older writers that omit
that timestamp. Existing deployments need a separately planned migration/backfill and coordinated
client/rules rollout. This guide does not supply that compatibility procedure; do not assume an
existing dataset can be upgraded by replacing configuration files alone.

### Verify a configured project

1. Rebuild after setting the JSON and web client ID. Register a Google account through the app, or
    register with email/password, and confirm the user appears in Firebase Authentication.

2. With network access, add an item and check for a document at
    `dev.atick.jetpack/{Firebase user UID}/jetpacks/{item ID}` in Firestore. Edit it and confirm the
    document changes. Local UI updates alone do not prove a successful remote write.

3. Delete the test item and confirm the document is removed remotely. This checks the current
    single-client path, not cross-device delete propagation or conflict handling.

4. If requests fail, check provider enablement, application ID, the tested build's fingerprint,
    web client ID, and published rules. Use [Troubleshooting](troubleshooting.md) for diagnostics.

The sync implementation has known concurrency and delivery limitations; these smoke checks do not
establish lossless synchronization. See [Data](data.md) for the current implementation.

## Crashlytics

`firebase:analytics` exposes `CrashReporter`, implemented by `FirebaseCrashReporter`, which forwards
non-fatal exceptions to `FirebaseCrashlytics.recordException`. The app applies
`FirebaseConventionPlugin`, which enables Google Services and Crashlytics plugins and adds the
Analytics, Crashlytics, and Performance SDKs. No custom Analytics event API or Performance trace
instrumentation is implemented here, and the Performance Gradle plugin is not applied. See the official
[Analytics](https://firebase.google.com/docs/analytics/get-started?platform=android) and
[Performance Monitoring](https://firebase.google.com/docs/perf-mon/get-started-android) guides for
SDK-provided collection and the setup required for additional instrumentation.

For your configured Firebase app, follow the console's Crashlytics onboarding and the
[official test procedure](https://firebase.google.com/docs/crashlytics/android/test-implementation).
In a disposable debug test, trigger a deliberate uncaught exception, then **restart the app** so it
can send the crash report. Remove temporary crash code afterward. To exercise the wrapper instead,
call `reportException` and inspect non-fatal reports. If a test crash is still absent after five
minutes, use the official debug-logging instructions to check configuration and upload errors;
delay alone does not identify the cause.

Release builds enable R8 minification. The applied Crashlytics plugin enables mapping-file upload
by default for obfuscated variants; this project does not override `mappingFileUploadEnabled`.
Successful upload still depends on a configured app and access to Firebase. See
[readable crash reports](https://firebase.google.com/docs/crashlytics/android/get-deobfuscated-reports)
for mapping-upload settings and [Build & Tooling](build-and-tooling.md) for CI configuration.

## Further Reading

- **[Troubleshooting](troubleshooting.md)** — Firebase setup and runtime symptom → fix entries
- **[Build & Tooling](build-and-tooling.md)** — `FirebaseConventionPlugin`, CI secrets
- **[Data](data.md)** — the two-way sync pattern Firestore participates in
- **[firebase:auth](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/firebase/auth/README.md)**, **[firebase:firestore](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/firebase/firestore/README.md)**,
  **[firebase:analytics](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/firebase/analytics/README.md)** — module references
