# Module :firebase:auth

**Purpose:** Email/password and Google sign-in via Android's Credential Manager API, backed by
Firebase Authentication.

## Key APIs

| API | What it does |
|---|---|
| `AuthDataSource` / `AuthDataSourceImpl` | `signInWithEmailAndPassword`, `registerWithEmailAndPassword`, `signInWithGoogle(activity)`, `registerWithGoogle(activity)`, `signOut()` |
| `getSignInRequest()` / `registerWithGoogleRequest()` | Two separate `GetCredentialRequest` builders — see the Gotcha below |

```kotlin
// firebase/auth/src/main/kotlin/dev/atick/firebase/auth/data/AuthDataSourceImpl.kt
private fun getSignInRequest(): GetCredentialRequest {
    val getGoogleIdOption = GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(true)
        .setServerClientId(Config.WEB_CLIENT_ID).setAutoSelectEnabled(false).build()
    // ...
}
```

## Gotchas

- `setFilterByAuthorizedAccounts(true)` for sign-in vs. `false` for register is the entire
  sign-in/register contract in one flag — `true` restricts the account picker to accounts already
  linked to this app, so an unlinked account fails rather than silently registering.

## Related Documentation

- [Firebase Setup § Credential Manager](../../docs/firebase.md#credential-manager-sign-in-vs-register) — the full sign-in-vs-register contract
- [Data Layer](../../docs/data.md) — where `AuthRepositoryImpl`/`ProfileRepositoryImpl` call this module
