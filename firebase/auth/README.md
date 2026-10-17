# Module :firebase:auth

**Purpose:** Firebase Authentication for email/password and Google credentials, with Credential
Manager for saved credentials and Google account selection.

## Key APIs

| API | What it does |
|---|---|
| `AuthDataSource` / `AuthDataSourceImpl` | `signInWithEmailAndPassword`, `registerWithEmailAndPassword`, `signInWithSavedCredentials(activity)`, `signInWithGoogle(activity)`, `registerWithGoogle(activity)`, `signOut()` |
| `getSignInRequest()` / `registerWithGoogleRequest()` | Two separate `GetCredentialRequest` builders — see the Gotcha below |

Private builder excerpt; other options and the returned request are omitted.

```kotlin
// firebase/auth/src/main/kotlin/dev/atick/firebase/auth/data/AuthDataSourceImpl.kt
private fun getSignInRequest(): GetCredentialRequest {
    val getGoogleIdOption = GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(true)
        .setServerClientId(Config.WEB_CLIENT_ID).setAutoSelectEnabled(false).build()
    // ...
}
```

## Gotchas

- The sign-in request offers saved passwords and previously authorized Google accounts. The
  registration request removes the Google-account filter. Both Google paths use Firebase
  `signInWithCredential`, which can create a Firebase user on first success; filtering is not a
  Firebase account-existence check. Sign-in does not automatically retry with the filter disabled.
- Configure Firebase, signing fingerprints and `GOOGLE_WEB_CLIENT_ID` as described in the setup
  guide before using Google credentials. The private request builders above are implementation
  details; consumers use `AuthDataSource`.

## Related Documentation

- [Firebase Setup § Credential Manager](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/firebase.md#credential-manager-sign-in-vs-register) — account filtering and Firebase first-sign-in behavior
- [Data Layer](https://github.com/atick-faisal/Jetpack-Android-Starter/blob/fa19cbbd83ae609dcc0886a1b6b37379e91a12cc/docs/data.md) — where `AuthRepositoryImpl`/`ProfileRepositoryImpl` call this module
