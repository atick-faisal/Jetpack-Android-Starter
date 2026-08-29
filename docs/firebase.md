# Firebase Setup

This project uses three independent Firebase modules: `firebase:auth` (Credential Manager +
Firebase Authentication), `firebase:firestore` (two-way data sync), and `firebase:analytics`
(Crashlytics crash reporting — **not** Firebase Analytics event tracking, despite the module
name). All three build and run out of the box against a template `google-services.json`; none of
them are functional until you configure your own Firebase project.

## Prerequisites

- A Google account and access to the [Firebase Console](https://console.firebase.google.com)
- Android Studio, with this project already cloned (see [Getting Started](getting-started.md))

## Firebase Console Setup

1. Create a project in the [Firebase Console](https://console.firebase.google.com).
2. Add an Android app registered under this project's `applicationId`,
   `dev.atick.compose` (`app/build.gradle.kts:54`).
3. Get the debug SHA-1 by running the "Signing Report" configuration from Android Studio's run
   configurations dropdown, then add it to **Project Settings → Your apps → SHA certificate
   fingerprints**.
4. Under **Authentication**, enable the **Google** and **Email/Password** sign-in methods.
5. Under **Firestore Database**, create a database (production mode).

## Local Project Setup

Download `google-services.json` from the console and replace the template copy in `app/`. Stop
Git from tracking your real file first:

```bash
git update-index --skip-worktree app/google-services.json
```

> [!WARNING]
> Never commit your real `google-services.json`. The template file exists only so a fresh clone
> builds; it does not enable any Firebase service.

Build and run, then try signing in with Google to confirm the console is wired up correctly.

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

> [!TIP]
> `setFilterByAuthorizedAccounts` is the entire sign-in/register contract in one flag. `true`
> restricts the account picker to Google accounts already linked to this app — used for sign-in,
> where an unlinked account should fail rather than silently register. `false` offers every Google
> account on the device — used for register, where any account is a valid choice. This is why the
> two flows need separate request builders instead of one shared function.

`Config.WEB_CLIENT_ID` (`firebase/auth/.../config/Config.kt`) reads `BuildConfig.GOOGLE_WEB_CLIENT_ID`,
which is sourced from the OAuth 2.0 web client ID Firebase generates alongside your
`google-services.json` — see [Build & Tooling](build-and-tooling.md) for how build-time secrets
are wired in.

## Firestore Security Rules

Sync (`firebase/firestore/.../FirebaseDataSourceImpl.kt`) writes to
`/dev.atick.jetpack/{userId}/jetpacks/{jetpackId}`, one document per `FirebaseJetpack`
(`id`, `name`, `price`, `userId`, `lastUpdated`, `lastSynced`, `deleted` — 7 fields, see
[Data](data.md) for the sync pattern these fields support). Set these rules under Firestore
Database → Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isUserOwner(userId) {
      return request.auth != null && request.auth.uid == userId;
    }

    function isValidJetpack(jetpack) {
      return jetpack.size() == 7
        && jetpack.id is string
        && jetpack.name is string
        && jetpack.price is number
        && jetpack.userId is string
        && jetpack.lastUpdated is number
        && jetpack.lastSynced is number
        && jetpack.deleted is bool;
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

Test rule changes in the Firebase Console's Rules Playground before deploying — a bad rule fails
silently as a permission-denied error at sync time, not at deploy time.

## Crashlytics

`firebase:analytics` is Crashlytics-only: no Firebase Analytics events are logged anywhere in this
template. Reporting goes through one abstraction
(`firebase/analytics/.../CrashReporter.kt`, implemented by `FirebaseCrashReporter.kt`):

```kotlin
override fun reportException(throwable: Throwable) {
    crashlytics.recordException(throwable)
}
```

Crashlytics is enabled automatically once `google-services.json` is in place; no extra console
step is needed beyond the project setup above. For release builds, `FirebaseConventionPlugin`
(see [Build & Tooling](build-and-tooling.md)) uploads ProGuard/R8 mapping files so stack traces
de-obfuscate in the console. To confirm reporting works end-to-end, call `reportException` (or
throw uncaught) from a debug build and check the Crashlytics console a few minutes later.

## Further Reading

- **[Troubleshooting](troubleshooting.md)** — Firebase setup and runtime symptom → fix entries
- **[Build & Tooling](build-and-tooling.md)** — `FirebaseConventionPlugin`, CI secrets
- **[Data](data.md)** — the two-way sync pattern Firestore participates in
- **[firebase:auth](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/firebase/auth/README.md)**, **[firebase:firestore](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/firebase/firestore/README.md)**,
  **[firebase:analytics](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/main/firebase/analytics/README.md)** — module references
